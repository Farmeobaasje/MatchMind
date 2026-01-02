package com.Lyno.matchmindai.domain.usecase

import com.Lyno.matchmindai.domain.model.*
import com.Lyno.matchmindai.domain.repository.MatchRepository
import com.Lyno.matchmindai.data.remote.DeepSeekApi
import com.Lyno.matchmindai.data.ai.PromptBuilder
import com.Lyno.matchmindai.data.cache.LLMCache
import com.Lyno.matchmindai.data.cache.PromptHashGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * LLMGRADE Analysis Use Case - Advanced LLM integration for context factor extraction
 * and outlier scenario detection.
 * 
 * This use case analyzes unstructured data (news, social media) to identify
 * qualitative factors that traditional models might miss.
 */
class LLMGradeAnalysisUseCase(
    private val matchRepository: MatchRepository,
    private val deepSeekApi: DeepSeekApi,
    private val promptBuilder: PromptBuilder,
    private val apiKeyStorage: com.Lyno.matchmindai.data.local.ApiKeyStorage
) {
    
    private val jsonParser = Json { ignoreUnknownKeys = true }
    private val llmCache = LLMCache()
    
    init {
        // Configure cache with optimal settings for LLM responses
        llmCache.configure(
            maxSize = 30,  // Keep 30 most recent analyses
            ttlHours = 12  // Cache for 12 hours (news becomes stale quickly)
        )
    }
    
    /**
     * Perform LLMGRADE analysis on a match.
     * 
     * @param fixtureId Match fixture ID
     * @param oracleAnalysis Optional Oracle analysis for context
     * @param tesseractResult Optional Tesseract result for context
     * @param forceRefresh If true, clear cache and fetch fresh data
     * @return Result containing LLMGradeEnhancement with context factors and outlier scenarios
     */
    suspend operator fun invoke(
        fixtureId: Int,
        oracleAnalysis: OracleAnalysis? = null,
        tesseractResult: TesseractResult? = null,
        forceRefresh: Boolean = false
    ): Result<LLMGradeEnhancement> = withContext(Dispatchers.Default) {
        try {
            // 🔒 CACHE NUKE: Clear cache if forceRefresh is true
            if (forceRefresh) {
                println("🔒 LLMGRADE CACHE NUKE: Clearing caches for fixture $fixtureId")
                matchRepository.clearCache().onFailure { error ->
                    println("⚠️  Cache clear failed: ${error.message}")
                }
                // Also clear LLM cache for this fixture
                clearCacheForFixture(fixtureId)
            }
            
            // Generate prompt hash for cache key
            val promptHash = generatePromptHash(fixtureId, oracleAnalysis, tesseractResult)
            
            // Try to get from cache first (unless forceRefresh)
            if (!forceRefresh) {
                val cachedEnhancement = llmCache.getIfAvailable(fixtureId, promptHash)
                if (cachedEnhancement != null) {
                    println("✅ LLMGRADE cache HIT for fixture $fixtureId")
                    return@withContext Result.success(cachedEnhancement)
                }
            }
            
            println("❌ LLMGRADE cache MISS for fixture $fixtureId, performing full analysis...")
            
            // STEP 1: Fetch match details
            var matchDetail: MatchDetail? = null
            var fetchError: Exception? = null
            
            matchRepository.getMatchDetails(fixtureId).collect { resource ->
                when (resource) {
                    is com.Lyno.matchmindai.common.Resource.Success -> {
                        matchDetail = resource.data
                    }
                    is com.Lyno.matchmindai.common.Resource.Error -> {
                        fetchError = Exception(resource.message ?: "Failed to fetch match details")
                    }
                    else -> {
                        // Loading state, continue waiting
                    }
                }
            }
            
            if (fetchError != null) {
                return@withContext Result.failure(fetchError!!)
            }
            
            if (matchDetail == null) {
                return@withContext Result.failure(Exception("No match details available for fixture $fixtureId"))
            }
            
            val matchDetailNonNull = matchDetail!!
            
            // STEP 2: Search for news (Tavily)
            val searchQuery = "${matchDetailNonNull.homeTeam} vs ${matchDetailNonNull.awayTeam} ${matchDetailNonNull.league} news injuries"
            val newsResult = matchRepository.searchInternet(searchQuery)
            
            if (newsResult.isFailure) {
                return@withContext Result.failure(
                    newsResult.exceptionOrNull() ?: Exception("Failed to search for news")
                )
            }
            
            val agentResponse = newsResult.getOrThrow()
            val newsItems = extractNewsItemsFromAgentResponse(agentResponse)
            
            // STEP 2.5: Fetch injuries data
            val injuriesResult = matchRepository.getInjuries(fixtureId)
            val injuries = if (injuriesResult.isSuccess) {
                injuriesResult.getOrNull() ?: emptyList()
            } else {
                println("⚠️  Failed to fetch injuries: ${injuriesResult.exceptionOrNull()?.message}")
                emptyList()
            }
            
            // STEP 3: Build LLMGRADE prompt
            val oraclePrediction = oracleAnalysis?.prediction
            val tesseractPrediction = tesseractResult?.mostLikelyScore
            
            val prompt = promptBuilder.buildLLMGradeContextExtractionPrompt(
                matchDetail = matchDetailNonNull,
                newsItems = newsItems,
                oraclePrediction = oraclePrediction,
                tesseractPrediction = tesseractPrediction,
                injuries = injuries
            )
            
            // STEP 4: Call DeepSeek API
            val apiKey = apiKeyStorage.getDeepSeekApiKey()
            if (apiKey.isNullOrEmpty()) {
                println("⚠️  No DeepSeek API key found for LLMGRADE analysis")
                return@withContext Result.failure(
                    Exception("No DeepSeek API key found. Please add your API key in settings.")
                )
            }
            
            val messages = listOf(
                com.Lyno.matchmindai.data.dto.DeepSeekMessage(
                    role = "user",
                    content = prompt
                )
            )
            val request = deepSeekApi.createAgenticRequest(
                messages = messages,
                includeTools = false,
                temperature = 0.5,
                responseFormat = com.Lyno.matchmindai.data.dto.ResponseFormat(type = "json_object"),
                model = "deepseek-chat"
            )
            val llmResponse = runCatching<com.Lyno.matchmindai.data.dto.DeepSeekResponse> { 
                deepSeekApi.getPrediction(apiKey, request) 
            }
            
            if (llmResponse.isFailure) {
                return@withContext Result.failure(
                    llmResponse.exceptionOrNull() ?: Exception("DeepSeek API call failed")
                )
            }
            
            val deepSeekResponse = llmResponse.getOrThrow()
            val responseText = deepSeekResponse.choices.firstOrNull()?.message?.content
                ?: return@withContext Result.failure(Exception("No content in DeepSeek response"))
            
            // STEP 5: Parse LLM response
            val enhancement = parseLLMGradeResponse(
                responseText = responseText,
                matchDetail = matchDetailNonNull,
                oracleAnalysis = oracleAnalysis,
                tesseractResult = tesseractResult
            )
            
            // Cache the enhancement
            llmCache.put(fixtureId, promptHash, enhancement)
            println("💾 Cached LLMGRADE enhancement for fixture $fixtureId")
            
            Result.success(enhancement)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Extract context factors and outlier scenarios from LLM response.
     */
    private fun parseLLMGradeResponse(
        responseText: String,
        matchDetail: MatchDetail,
        oracleAnalysis: OracleAnalysis?,
        tesseractResult: TesseractResult?
    ): LLMGradeEnhancement {
        return try {
            // Extract JSON from response (might have markdown formatting)
            val jsonText = extractJsonFromResponse(responseText)
            
            if (jsonText == null) {
                println("⚠️  Could not extract JSON from LLM response")
                return LLMGradeEnhancementFactory.createEmpty()
            }
            
            val jsonObject = jsonParser.decodeFromString<JsonObject>(jsonText)
            
            // Parse context factors
            val contextFactors = parseContextFactors(jsonObject)
            
            // Parse outlier scenarios
            val outlierScenarios = parseOutlierScenarios(jsonObject, matchDetail)
            
            // Parse enhanced reasoning
            val enhancedReasoning = jsonObject["enhanced_reasoning"]?.jsonPrimitive?.content
                ?: "LLM analyse voltooid. Contextfactoren en uitschieterscenario's geïdentificeerd."
            
            // Create enhancement
            LLMGradeEnhancementFactory.createFullEnhancement(
                contextFactors = contextFactors,
                outlierScenarios = outlierScenarios,
                enhancedReasoning = enhancedReasoning
            )
            
        } catch (e: Exception) {
            println("❌ Failed to parse LLM response: ${e.message}")
            LLMGradeEnhancementFactory.createEmpty()
        }
    }
    
    /**
     * Extract JSON from LLM response (might be wrapped in markdown).
     */
    private fun extractJsonFromResponse(responseText: String): String? {
        // Try to find JSON object
        val jsonStart = responseText.indexOf('{')
        val jsonEnd = responseText.lastIndexOf('}')
        
        if (jsonStart == -1 || jsonEnd == -1 || jsonEnd <= jsonStart) {
            return null
        }
        
        return responseText.substring(jsonStart, jsonEnd + 1)
    }
    
    /**
     * Parse context factors from JSON response.
     */
    private fun parseContextFactors(jsonObject: JsonObject): List<ContextFactor> {
        val contextFactors = mutableListOf<ContextFactor>()
        
        val factorsArray = jsonObject["context_factors"]?.jsonArray
        if (factorsArray == null) {
            println("⚠️  No context_factors found in LLM response")
            return emptyList()
        }
        
        for (factorJson in factorsArray) {
            try {
                val factorObject = factorJson.jsonObject
                
                val typeStr = factorObject["type"]?.jsonPrimitive?.content
                val score = factorObject["score"]?.jsonPrimitive?.content?.toIntOrNull()
                val description = factorObject["description"]?.jsonPrimitive?.content
                val weight = factorObject["weight"]?.jsonPrimitive?.content?.toDoubleOrNull()
                
                if (typeStr == null || score == null || description == null) {
                    println("⚠️  Missing required fields in context factor: $factorJson")
                    continue
                }
                
                val type = try {
                    ContextFactorType.valueOf(typeStr)
                } catch (e: IllegalArgumentException) {
                    println("⚠️  Invalid context factor type: $typeStr")
                    continue
                }
                
                val contextFactor = ContextFactor(
                    type = type,
                    score = score,
                    description = description,
                    weight = weight ?: type.defaultWeight()
                )
                
                contextFactors.add(contextFactor)
                
            } catch (e: Exception) {
                println("⚠️  Failed to parse context factor: ${e.message}")
            }
        }
        
        return contextFactors
    }
    
    /**
     * Parse outlier scenarios from JSON response.
     */
    private fun parseOutlierScenarios(
        jsonObject: JsonObject,
        matchDetail: MatchDetail
    ): List<OutlierScenario> {
        val outlierScenarios = mutableListOf<OutlierScenario>()
        
        val scenariosArray = jsonObject["outlier_scenarios"]?.jsonArray
        if (scenariosArray == null) {
            println("⚠️  No outlier_scenarios found in LLM response")
            return emptyList()
        }
        
        for (scenarioJson in scenariosArray) {
            try {
                val scenarioObject = scenarioJson.jsonObject
                
                val description = scenarioObject["description"]?.jsonPrimitive?.content
                val probability = scenarioObject["probability"]?.jsonPrimitive?.content?.toDoubleOrNull()
                val impactScore = scenarioObject["impact_score"]?.jsonPrimitive?.content?.toIntOrNull() ?: 5
                
                if (description == null || probability == null) {
                    println("⚠️  Missing required fields in outlier scenario: $scenarioJson")
                    continue
                }
                
                // Parse supporting factors
                val supportingFactors = mutableListOf<String>()
                val supportingArray = scenarioObject["supporting_factors"]?.jsonArray
                if (supportingArray != null) {
                    for (factorJson in supportingArray) {
                        factorJson.jsonPrimitive?.content?.let { supportingFactors.add(it) }
                    }
                }
                
                // Parse historical precedents
                val historicalPrecedents = mutableListOf<String>()
                val historicalArray = scenarioObject["historical_precedents"]?.jsonArray
                if (historicalArray != null) {
                    for (precedentJson in historicalArray) {
                        precedentJson.jsonPrimitive?.content?.let { historicalPrecedents.add(it) }
                    }
                }
                
                val outlierScenario = OutlierScenario(
                    description = description,
                    probability = probability,
                    supportingFactors = supportingFactors,
                    historicalPrecedents = historicalPrecedents,
                    impactScore = impactScore
                )
                
                outlierScenarios.add(outlierScenario)
                
            } catch (e: Exception) {
                println("⚠️  Failed to parse outlier scenario: ${e.message}")
            }
        }
        
        return outlierScenarios
    }
    
    /**
     * Extract news items from AgentResponse.
     */
    private fun extractNewsItemsFromAgentResponse(agentResponse: AgentResponse): List<NewsItemData> {
        // TODO: Implement proper extraction from AgentResponse
        // For now, return empty list
        return emptyList()
    }
    
    /**
     * Perform quick LLMGRADE analysis (cached version).
     * Uses cached data if available, otherwise performs full analysis.
     */
    suspend fun quickAnalysis(
        fixtureId: Int,
        oracleAnalysis: OracleAnalysis? = null,
        tesseractResult: TesseractResult? = null
    ): Result<LLMGradeEnhancement> {
        // TODO: Implement caching logic
        return invoke(fixtureId, oracleAnalysis, tesseractResult, forceRefresh = false)
    }
    
    /**
     * Perform outlier-only analysis (focused on detecting unexpected outcomes).
     */
    suspend fun outlierAnalysis(
        fixtureId: Int,
        basePrediction: String,
        contextFactors: List<ContextFactor>
    ): Result<List<OutlierScenario>> = withContext(Dispatchers.Default) {
        try {
            // Fetch match details
            var matchDetail: MatchDetail? = null
            
            matchRepository.getMatchDetails(fixtureId).collect { resource ->
                if (resource is com.Lyno.matchmindai.common.Resource.Success) {
                    matchDetail = resource.data
                }
            }
            
            if (matchDetail == null) {
                return@withContext Result.failure(Exception("No match details available"))
            }
            
            // Build historical data summary
            val historicalData = getHistoricalDataSummary(fixtureId)
            
            // Build prompt
            val contextFactorDescriptions = contextFactors.map { 
                "${it.type.dutchDescription()}: ${it.score}/10 - ${it.description}"
            }
            
            val prompt = promptBuilder.buildLLMGradeOutlierDetectionPrompt(
                matchDetail = matchDetail!!,
                historicalData = historicalData,
                basePrediction = basePrediction,
                contextFactors = contextFactorDescriptions
            )
            
            // Call DeepSeek API
            val apiKey = apiKeyStorage.getDeepSeekApiKey()
            if (apiKey.isNullOrEmpty()) {
                println("⚠️  No DeepSeek API key found for outlier analysis")
                return@withContext Result.failure(
                    Exception("No DeepSeek API key found. Please add your API key in settings.")
                )
            }
            
            val messages = listOf(
                com.Lyno.matchmindai.data.dto.DeepSeekMessage(
                    role = "user",
                    content = prompt
                )
            )
            val request = deepSeekApi.createAgenticRequest(
                messages = messages,
                includeTools = false,
                temperature = 0.5,
                responseFormat = com.Lyno.matchmindai.data.dto.ResponseFormat(type = "json_object"),
                model = "deepseek-chat"
            )
            val llmResponse = runCatching<com.Lyno.matchmindai.data.dto.DeepSeekResponse> { 
                deepSeekApi.getPrediction(apiKey, request) 
            }
            
            if (llmResponse.isFailure) {
                return@withContext Result.failure(
                    llmResponse.exceptionOrNull() ?: Exception("DeepSeek API call failed")
                )
            }
            
            val deepSeekResponse = llmResponse.getOrThrow()
            val responseText = deepSeekResponse.choices.firstOrNull()?.message?.content
                ?: return@withContext Result.failure(Exception("No content in DeepSeek response"))
            val jsonText = extractJsonFromResponse(responseText)
            
            if (jsonText == null) {
                return@withContext Result.failure(Exception("Could not extract JSON from LLM response"))
            }
            
            val jsonObject = jsonParser.decodeFromString<JsonObject>(jsonText)
            val outlierScenarios = parseOutlierScenarios(jsonObject, matchDetail!!)
            
            Result.success(outlierScenarios)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get historical data summary for a fixture.
     */
    private suspend fun getHistoricalDataSummary(fixtureId: Int): List<String> {
        // TODO: Implement historical data fetching and summarization
        return emptyList()
    }
    
    /**
     * Generate prompt hash for cache key.
     */
    private fun generatePromptHash(
        fixtureId: Int,
        oracleAnalysis: OracleAnalysis?,
        tesseractResult: TesseractResult?
    ): String {
        val oraclePrediction = oracleAnalysis?.prediction ?: "unknown"
        val oracleConfidence = oracleAnalysis?.confidence ?: 0
        val tesseractScore = tesseractResult?.mostLikelyScore ?: "unknown"
        
        val combinedString = "${fixtureId}_${oraclePrediction}_${oracleConfidence}_${tesseractScore}"
        return PromptHashGenerator.generateHash(combinedString)
    }
    
    /**
     * Clear cache for a specific fixture.
     */
    private suspend fun clearCacheForFixture(fixtureId: Int) {
        // Generate a generic prompt hash to clear all variations for this fixture
        val genericHash = PromptHashGenerator.generateHash(fixtureId.toString())
        llmCache.remove(fixtureId, genericHash)
        println("🗑️ Cleared LLM cache for fixture $fixtureId")
    }
    
    /**
     * Get cache statistics for monitoring.
     */
    suspend fun getCacheStats(): LLMCache.CacheStats {
        return llmCache.getStats()
    }
    
    /**
     * Clean up expired cache entries.
     */
    suspend fun cleanupCache(): Int {
        return llmCache.cleanup()
    }
}

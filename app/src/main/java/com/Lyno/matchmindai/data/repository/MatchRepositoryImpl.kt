package com.Lyno.matchmindai.data.repository

import android.util.Log
import com.Lyno.matchmindai.data.dto.DeepSeekMessage
import com.Lyno.matchmindai.data.dto.DeepSeekRequest
import com.Lyno.matchmindai.data.dto.DeepSeekResponse
import com.Lyno.matchmindai.data.dto.MatchPredictionResponse
import com.Lyno.matchmindai.data.dto.ResponseFormat
import com.Lyno.matchmindai.data.local.ApiKeyStorage
import com.Lyno.matchmindai.data.remote.DeepSeekApi
import com.Lyno.matchmindai.data.remote.DeepSeekApiException
import com.Lyno.matchmindai.data.remote.scraper.WebScraper
import com.Lyno.matchmindai.domain.model.MatchPrediction
import com.Lyno.matchmindai.domain.repository.MatchRepository
import com.Lyno.matchmindai.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * Custom exception thrown when the API key is missing.
 * This allows the UI to handle this specific error gracefully (redirect to settings).
 */
class ApiKeyMissingException : Exception("API key is missing. Please set your API key in settings.")

/**
 * Implementation of MatchRepository that coordinates between local storage and remote API.
 * Follows the "User-Managed Security" principle by checking for API key before making requests.
 * Now includes RAG (Retrieval Augmented Generation) functionality via web scraping.
 */
class MatchRepositoryImpl(
    private val apiKeyStorage: ApiKeyStorage,
    private val deepSeekApi: DeepSeekApi,
    private val webScraper: WebScraper,
    private val settingsRepository: SettingsRepository
) : MatchRepository {

    override suspend fun getPrediction(homeTeam: String, awayTeam: String): Result<MatchPrediction> {
        return try {
            // Step 1: Get API key from local storage
            val apiKey = apiKeyStorage.getKey().first()
            
            // Step 2: Check if API key is present
            if (apiKey.isNullOrBlank()) {
                return Result.failure(ApiKeyMissingException())
            }

            // Step 3: Get user preferences for live data setting
            val prefs = settingsRepository.getPreferences().first()
            
            // Step 4: Scrape live match context from the web (if enabled)
            val scrapedInfo = if (prefs.useLiveData) {
                webScraper.scrapeMatchContext(homeTeam, awayTeam)
            } else {
                "Live data uitgeschakeld door gebruiker."
            }
            
            // Log the scraped context for debugging
            Log.d("MatchRepository", "=== SCRAPED CONTEXT for $homeTeam vs $awayTeam ===")
            Log.d("MatchRepository", scrapedInfo)
            Log.d("MatchRepository", "=== END SCRAPED CONTEXT ===")
            
            // Step 4: Build enriched prompt with scraped context
            val enrichedPrompt = buildEnrichedPrompt(homeTeam, awayTeam, scrapedInfo)
            
            // Step 5: Create custom request with enriched prompt
            val request = DeepSeekRequest(
                messages = listOf(
                    DeepSeekMessage(
                        role = "user",
                        content = enrichedPrompt
                    )
                ),
                responseFormat = ResponseFormat(type = "json_object")
            )
            
            // Step 6: Call DeepSeek API with the enriched prompt
            val response = deepSeekApi.getPrediction(apiKey, request)
            
            // Step 7: Convert DTO to domain model
            val predictionResponse = response.toMatchPrediction()
            val domainPrediction = predictionResponse.toDomainModel(homeTeam, awayTeam)
            
            // Log the final reasoning to verify if news is being picked up
            Log.d("MatchRepository", "=== FINAL REASONING ===")
            Log.d("MatchRepository", "Winner: ${domainPrediction.winner}")
            Log.d("MatchRepository", "Reasoning: ${domainPrediction.reasoning}")
            Log.d("MatchRepository", "Key Factor: ${domainPrediction.keyFactor}")
            Log.d("MatchRepository", "=== END REASONING ===")
            
            Result.success(domainPrediction)
        } catch (e: ApiKeyMissingException) {
            Result.failure(e)
        } catch (e: DeepSeekApiException) {
            Result.failure(Exception("API error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }

    /**
     * Alternative implementation using Flow for reactive programming.
     */
    fun getPredictionFlow(homeTeam: String, awayTeam: String) = flow {
        emit(Result.runCatching {
            // Same logic as above but in a Flow
            val apiKey = apiKeyStorage.getKey().first()
            
            if (apiKey.isNullOrBlank()) {
                throw ApiKeyMissingException()
            }

            // Get user preferences for live data setting
            val prefs = settingsRepository.getPreferences().first()
            
            // Scrape live match context from the web (if enabled)
            val scrapedInfo = if (prefs.useLiveData) {
                webScraper.scrapeMatchContext(homeTeam, awayTeam)
            } else {
                "Live data uitgeschakeld door gebruiker."
            }
            
            // Log the scraped context for debugging
            Log.d("MatchRepository", "=== SCRAPED CONTEXT for $homeTeam vs $awayTeam ===")
            Log.d("MatchRepository", scrapedInfo)
            Log.d("MatchRepository", "=== END SCRAPED CONTEXT ===")
            
            // Build enriched prompt with scraped context
            val enrichedPrompt = buildEnrichedPrompt(homeTeam, awayTeam, scrapedInfo)
            
            // Create custom request with enriched prompt
            val request = DeepSeekRequest(
                messages = listOf(
                    DeepSeekMessage(
                        role = "user",
                        content = enrichedPrompt
                    )
                ),
                responseFormat = ResponseFormat(type = "json_object")
            )
            
            val response = deepSeekApi.getPrediction(apiKey, request)
            val predictionResponse = response.toMatchPrediction()
            val domainPrediction = predictionResponse.toDomainModel(homeTeam, awayTeam)
            
            // Log the final reasoning to verify if news is being picked up
            Log.d("MatchRepository", "=== FINAL REASONING ===")
            Log.d("MatchRepository", "Winner: ${domainPrediction.winner}")
            Log.d("MatchRepository", "Reasoning: ${domainPrediction.reasoning}")
            Log.d("MatchRepository", "Key Factor: ${domainPrediction.keyFactor}")
            Log.d("MatchRepository", "=== END REASONING ===")
            
            domainPrediction
        })
    }
}

/**
 * Extension function to convert DTO to domain model.
 */
private fun MatchPredictionResponse.toDomainModel(
    homeTeam: String,
    awayTeam: String
): MatchPrediction {
    return MatchPrediction(
        homeTeam = homeTeam,
        awayTeam = awayTeam,
        winner = winner,
        confidenceScore = confidenceScore,
        riskLevel = MatchPrediction.RiskLevel.valueOf(riskLevel.name),
        reasoning = reasoning,
        keyFactor = keyFactor
    )
}

/**
 * Builds an enriched prompt with live match context from web scraping.
 * Uses a more powerful template that encourages the AI to be a journalist, not a robot.
 */
private fun buildEnrichedPrompt(homeTeam: String, awayTeam: String, scrapedContext: String): String {
    return """
        Analyseer de wedstrijd $homeTeam vs $awayTeam.
        
        === LIVE NIEUWS & STATS (VAN HET WEB) ===
        $scrapedContext
        =========================================
        
        INSTRUCTIES VOOR ANALYSE:
        1. Scan de bovenstaande tekst op cruciale details: Blessures, Vorm (W/V/G), en recente opstootjes/nieuws.
        2. Gebruik deze details EXPLICIET in je onderbouwing. (Bijv: "Omdat speler X geblesseerd is...").
        3. Als de web-info leeg of niet relevant is, val terug op je algemene kennis.
        
        OUTPUT EISEN:
        - Taal: Nederlands.
        - Format: JSON (zoals eerder gedefinieerd).
        - Reasoning: Max 3 zinnen, maar rijk aan detail.
        - Key Factor: Moet gebaseerd zijn op het nieuws (indien beschikbaar).
        
        OUTPUT (JSON):
        {
          "winner": "Team Naam",
          "confidence_score": 0-100,
          "risk_level": "LOW/MEDIUM/HIGH",
          "reasoning": "Korte analyse (max 3 zinnen).",
          "key_factor": "Eén kernzin (max 5 woorden)."
        }
    """.trimIndent()
}

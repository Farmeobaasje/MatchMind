package com.Lyno.matchmindai.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Lyno.matchmindai.data.repository.OracleRepositoryImpl
import com.Lyno.matchmindai.domain.model.OracleAnalysis
import com.Lyno.matchmindai.domain.model.OracleAdjustment
import com.Lyno.matchmindai.domain.service.NewsImpactAnalyzer
import com.Lyno.matchmindai.domain.usecase.GetOraclePredictionUseCase
import com.Lyno.matchmindai.domain.usecase.OraclePredictionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Oracle Engine predictions with AI Intelligence Layer (Phase 3).
 * Provides deterministic, fact-based predictions with AI validation.
 */
class PredictionViewModel(
    private val oracleRepository: OracleRepositoryImpl,
    private val getOraclePredictionUseCase: GetOraclePredictionUseCase,
    private val newsImpactAnalyzer: NewsImpactAnalyzer,
    private val getHybridPredictionUseCase: com.Lyno.matchmindai.domain.usecase.GetHybridPredictionUseCase,
    private val apiKeyStorage: com.Lyno.matchmindai.data.local.ApiKeyStorage
) : ViewModel() {

    companion object {
        private const val TAG = "PredictionViewModel"
    }

    private val _predictionState = MutableStateFlow<PredictionState>(PredictionState.Idle)
    val predictionState: StateFlow<PredictionState> = _predictionState.asStateFlow()

    private val _aiValidationState = MutableStateFlow<AiValidationState>(AiValidationState.Idle)
    val aiValidationState: StateFlow<AiValidationState> = _aiValidationState.asStateFlow()

    private val _matchScenarioState = MutableStateFlow<MatchScenarioState>(MatchScenarioState.Idle)
    val matchScenarioState: StateFlow<MatchScenarioState> = _matchScenarioState.asStateFlow()

    // ChiChi refresh loading state
    private val _isChiChiRefreshing = MutableStateFlow(false)
    val isChiChiRefreshing: StateFlow<Boolean> = _isChiChiRefreshing.asStateFlow()

    // Mastermind analysis loading state
    private val _isMastermindAnalyzing = MutableStateFlow(false)
    val isMastermindAnalyzing: StateFlow<Boolean> = _isMastermindAnalyzing.asStateFlow()

    /**
     * Load Oracle prediction with AI Intelligence Layer validation (Phase 3).
     * This operation provides:
     * 1. Immediate base prediction (0-1s)
     * 2. AI validation with news analysis (1-3s)
     * 3. Final validated/adjusted prediction
     *
     * @param leagueId The league ID (e.g., 39 for Premier League)
     * @param season The season year (e.g., 2024)
     * @param homeTeamId The home team ID
     * @param awayTeamId The away team ID
     * @param homeTeamName The home team name (for news search)
     * @param awayTeamName The away team name (for news search)
     * @param fixtureId Optional fixture ID for LLMGRADE analysis
     */
    fun loadPredictionWithAI(
        leagueId: Int,
        season: Int,
        homeTeamId: Int,
        awayTeamId: Int,
        homeTeamName: String,
        awayTeamName: String,
        fixtureId: Int? = null
    ) {
        Log.d(TAG, "Loading AI-validated prediction for $homeTeamName vs $awayTeamName (fixtureId: $fixtureId)")
        
        // Reset states
        _predictionState.update { PredictionState.Loading }
        _aiValidationState.update { AiValidationState.Idle }
        
        viewModelScope.launch {
            getOraclePredictionUseCase(
                leagueId = leagueId,
                season = season,
                homeTeamId = homeTeamId,
                awayTeamId = awayTeamId,
                homeTeamName = homeTeamName,
                awayTeamName = awayTeamName,
                fixtureId = fixtureId
            ).collect { oracleState ->
                when (oracleState) {
                    is OraclePredictionState.Loading -> {
                        Log.d(TAG, "Oracle prediction flow: Loading")
                        _predictionState.update { PredictionState.Loading }
                    }
                    
                    is OraclePredictionState.BaseLoaded -> {
                        Log.d(TAG, "Oracle prediction flow: Base loaded - ${oracleState.data.prediction}")
                        _predictionState.update { PredictionState.Success(oracleState.data) }
                        _aiValidationState.update { AiValidationState.AnalyzingNews }
                    }
                    
                    is OraclePredictionState.AnalyzingNews -> {
                        Log.d(TAG, "Oracle prediction flow: Analyzing news")
                        _predictionState.update { PredictionState.Success(oracleState.baseData) }
                        _aiValidationState.update { AiValidationState.AnalyzingNews }
                    }
                    
                    is OraclePredictionState.Success -> {
                        Log.d(TAG, "Oracle prediction flow: Success - ${oracleState.data.prediction}")
                        _predictionState.update { PredictionState.Success(oracleState.data) }
                        _aiValidationState.update { 
                            AiValidationState.Complete(
                                adjustment = oracleState.adjustment,
                                finalAnalysis = oracleState.data
                            )
                        }
                    }
                    
                    is OraclePredictionState.Error -> {
                        Log.e(TAG, "Oracle prediction flow: Error - ${oracleState.message}")
                        _predictionState.update { 
                            PredictionState.Error(
                                message = oracleState.message
                            )
                        }
                        _aiValidationState.update { AiValidationState.Error(oracleState.message) }
                    }
                }
            }
        }
    }

    /**
     * Load basic Oracle prediction (legacy, without AI validation).
     * Use this for backward compatibility or when AI validation is not needed.
     */
    fun loadPrediction(
        leagueId: Int,
        season: Int,
        homeTeamId: Int,
        awayTeamId: Int,
        fixtureId: Int? = null
    ) {
        Log.d(TAG, "Loading basic prediction for league=$leagueId, season=$season, home=$homeTeamId, away=$awayTeamId (fixtureId: $fixtureId)")
        
        _predictionState.update { PredictionState.Loading }
        _aiValidationState.update { AiValidationState.Idle }
        
        viewModelScope.launch {
            try {
                val analysis = oracleRepository.getOracleAnalysis(
                    leagueId = leagueId,
                    season = season,
                    homeTeamId = homeTeamId,
                    awayTeamId = awayTeamId,
                    fixtureId = fixtureId
                )
                
                Log.d(TAG, "Prediction loaded successfully: ${analysis.prediction} (confidence: ${analysis.confidence}%)")
                _predictionState.update { PredictionState.Success(analysis) }
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load prediction", e)
                _predictionState.update { 
                    PredictionState.Error(
                        message = "Kon voorspelling niet laden: ${e.message ?: "Onbekende fout"}"
                    )
                }
            }
        }
    }

    /**
     * Generate a compelling match scenario narrative in Dutch.
     * This should be called after the prediction is loaded.
     * 
     * @param fixtureId The fixture ID
     * @param matchDetail The match details
     */
    fun generateMatchScenario(
        fixtureId: Int,
        matchDetail: com.Lyno.matchmindai.domain.model.MatchDetail
    ) {
        Log.d(TAG, "Generating match scenario for fixture $fixtureId: ${matchDetail.homeTeam} vs ${matchDetail.awayTeam}")
        
        _matchScenarioState.update { MatchScenarioState.Loading }
        
        viewModelScope.launch {
            try {
                // Get API key for DeepSeek
                val apiKey = apiKeyStorage.getDeepSeekApiKey()
                if (apiKey.isNullOrEmpty()) {
                    Log.w(TAG, "No DeepSeek API key found, using fallback scenario")
                    val fallbackContext = newsImpactAnalyzer.generateFallbackScenario(fixtureId, matchDetail)
                    val scenario = buildScenarioNarrative(fallbackContext, matchDetail)
                    _matchScenarioState.update { MatchScenarioState.Success(scenario) }
                    return@launch
                }
                
                val result = newsImpactAnalyzer.generateMatchScenario(
                    fixtureId = fixtureId,
                    matchDetail = matchDetail,
                    apiKey = apiKey
                )
                
                if (result.isSuccess) {
                    val simulationContext = result.getOrThrow()
                    // Convert SimulationContext to a narrative string for display
                    val scenario = buildScenarioNarrative(simulationContext, matchDetail)
                    Log.d(TAG, "Match scenario generated successfully")
                    _matchScenarioState.update { MatchScenarioState.Success(scenario) }
                } else {
                    val error = result.exceptionOrNull()
                    Log.e(TAG, "Failed to generate match scenario", error)
                    _matchScenarioState.update { 
                        MatchScenarioState.Error(
                            message = "Kon scenario niet genereren: ${error?.message ?: "Onbekende fout"}"
                        )
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate match scenario", e)
                _matchScenarioState.update { 
                    MatchScenarioState.Error(
                        message = "Kon scenario niet genereren: ${e.message ?: "Onbekende fout"}"
                    )
                }
            }
        }
    }
    
    /**
     * Build a narrative scenario from SimulationContext.
     */
    private fun buildScenarioNarrative(
        simulationContext: com.Lyno.matchmindai.domain.model.SimulationContext,
        matchDetail: com.Lyno.matchmindai.domain.model.MatchDetail
    ): String {
        val homeTeam = matchDetail.homeTeam
        val awayTeam = matchDetail.awayTeam
        
        return buildString {
            appendLine("⚽ Match Scenario: $homeTeam vs $awayTeam")
            appendLine()
            
            // Trinity metrics analysis
            appendLine("📊 Trinity Metrics Analysis:")
            appendLine("• Fatigue Score: ${simulationContext.fatigueScore}/100 (${if (simulationContext.hasHighFatigue) "⚠️ High" else "OK"})")
            appendLine("• Style Matchup: ${String.format("%.2f", simulationContext.styleMatchup)} (${getStyleMatchupDescription(simulationContext.styleMatchup)})")
            appendLine("• Lineup Strength: ${simulationContext.lineupStrength}/100 (${if (simulationContext.hasWeakLineup) "⚠️ Weak" else "Strong"})")
            appendLine()
            
            // Overall analysis
            appendLine("📈 Match Dynamics:")
            val homeAdvantage = when {
                simulationContext.fatigueScore < 30 && simulationContext.lineupStrength > 80 -> "Sterk thuisvoordeel"
                simulationContext.fatigueScore > 70 || simulationContext.lineupStrength < 50 -> "Zwak thuisvoordeel"
                else -> "Normaal thuisvoordeel"
            }
            val awayChallenge = when {
                simulationContext.fatigueScore > 70 && simulationContext.lineupStrength < 50 -> "Uitploeg in crisis"
                simulationContext.fatigueScore < 30 && simulationContext.lineupStrength > 80 -> "Uitploeg in topvorm"
                else -> "Standaard uitdaging"
            }
            
            appendLine("• $homeTeam: $homeAdvantage")
            appendLine("• $awayTeam: $awayChallenge")
            appendLine()
            
            // Key factors
            appendLine("🔑 Sleutelfactoren:")
            if (simulationContext.hasHighFatigue) {
                appendLine("• Beide teams hebben last van vermoeidheid")
            }
            if (simulationContext.hasWeakLineup) {
                appendLine("• Zwakke opstelling door blessures/schorsingen")
            }
            if (simulationContext.hasStyleAdvantage) {
                appendLine("• Stijlvoordeel voor het thuisteam")
            }
            if (simulationContext.hasStyleDisadvantage) {
                appendLine("• Stijlnadeel voor het thuisteam")
            }
            
            // Add reasoning if available
            if (simulationContext.reasoning.isNotBlank()) {
                appendLine()
                appendLine("💡 AI Analysis:")
                appendLine(simulationContext.reasoning)
            }
        }
    }
    
    /**
     * Returns a description of the style matchup.
     */
    private fun getStyleMatchupDescription(styleMatchup: Double): String {
        return when {
            styleMatchup > 1.2 -> "Strong Advantage"
            styleMatchup > 1.1 -> "Advantage"
            styleMatchup > 0.9 -> "Neutral"
            styleMatchup > 0.8 -> "Disadvantage"
            else -> "Strong Disadvantage"
        }
    }

    /**
     * Save the current prediction to history (user-driven).
     * 
     * @param fixtureId The actual fixture ID from the match
     * @param homeTeamId Home team ID
     * @param awayTeamId Away team ID
     * @param homeTeamName Home team name
     * @param awayTeamName Away team name
     * @param predictedScore The predicted score (e.g., "1-3")
     * @param confidence Prediction confidence percentage (0-100)
     * @return Boolean indicating success
     */
    fun savePrediction(
        fixtureId: Int,
        homeTeamId: Int,
        awayTeamId: Int,
        homeTeamName: String,
        awayTeamName: String,
        predictedScore: String,
        confidence: Int
    ) {
        Log.d(TAG, "Saving prediction to history for fixture $fixtureId")
        
        viewModelScope.launch {
            try {
                val currentState = _predictionState.value
                if (currentState !is PredictionState.Success) {
                    Log.e(TAG, "Cannot save prediction: No successful prediction available")
                    return@launch
                }
                
                val analysis = currentState.data
                
                // Create a new OracleAnalysis with the provided data to ensure all fields are set
                val predictionAnalysis = com.Lyno.matchmindai.domain.model.OracleAnalysis(
                    prediction = predictedScore,
                    confidence = confidence,
                    reasoning = analysis.reasoning,
                    homePowerScore = analysis.homePowerScore,
                    awayPowerScore = analysis.awayPowerScore,
                    tesseract = analysis.tesseract,
                    simulationContext = analysis.simulationContext,
                    llmGradeEnhancement = analysis.llmGradeEnhancement,
                    mastermindSignal = analysis.mastermindSignal
                )
                
                val success = oracleRepository.savePrediction(
                    fixtureId = fixtureId,
                    homeTeamId = homeTeamId,
                    awayTeamId = awayTeamId,
                    homeTeamName = homeTeamName,
                    awayTeamName = awayTeamName,
                    analysis = predictionAnalysis
                )
                
                if (success) {
                    Log.d(TAG, "✅ Prediction saved successfully to history")
                    // TODO: Show snackbar/feedback in UI
                } else {
                    Log.e(TAG, "❌ Failed to save prediction to history")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error saving prediction to history", e)
            }
        }
    }

    /**
     * Refresh ChiChi simulation context for the current match.
     * This triggers a new news analysis and updates the SimulationContext in the OracleAnalysis.
     *
     * @param fixtureId The fixture ID
     * @param matchDetail The match details
     */
    fun refreshSimulationContext(
        fixtureId: Int,
        matchDetail: com.Lyno.matchmindai.domain.model.MatchDetail
    ) {
        Log.d(TAG, "Refreshing ChiChi simulation context for fixture $fixtureId")
        Log.d("DEBUG_CHICHI", "ViewModel refreshSimulationContext called for ${matchDetail.homeTeam} vs ${matchDetail.awayTeam}")
        
        _isChiChiRefreshing.update { true }
        
        viewModelScope.launch {
            try {
                // Get API key for DeepSeek
                val apiKey = apiKeyStorage.getDeepSeekApiKey()
                if (apiKey.isNullOrEmpty()) {
                    Log.w(TAG, "No DeepSeek API key found, using fallback scenario")
                    val fallbackContext = newsImpactAnalyzer.generateFallbackScenario(fixtureId, matchDetail)
                    // Update the current OracleAnalysis with the fallback context
                    val currentState = _predictionState.value
                    if (currentState is PredictionState.Success) {
                        val updatedAnalysis = currentState.data.copy(simulationContext = fallbackContext)
                        _predictionState.update { PredictionState.Success(updatedAnalysis) }
                        Log.d(TAG, "✅ ChiChi simulation context refreshed with fallback (no API key)")
                        Log.d("DEBUG_CHICHI", "✅ UI updated with fallback simulation context")
                    }
                    return@launch
                }
                
                Log.d("DEBUG_CHICHI", "Calling newsImpactAnalyzer.generateMatchScenario")
                val result = newsImpactAnalyzer.generateMatchScenario(
                    fixtureId = fixtureId,
                    matchDetail = matchDetail,
                    apiKey = apiKey
                )
                
                if (result.isSuccess) {
                    val newContext = result.getOrThrow()
                    Log.d("DEBUG_CHICHI", "NewsImpactAnalyzer returned success: fatigueScore=${newContext.fatigueScore}, styleMatchup=${newContext.styleMatchup}, lineupStrength=${newContext.lineupStrength}")
                    
                    // Update the current OracleAnalysis with the new SimulationContext
                    val currentState = _predictionState.value
                    if (currentState is PredictionState.Success) {
                        val updatedAnalysis = currentState.data.copy(simulationContext = newContext)
                        _predictionState.update { PredictionState.Success(updatedAnalysis) }
                        Log.d(TAG, "✅ ChiChi simulation context refreshed successfully")
                        Log.d("DEBUG_CHICHI", "✅ UI updated with new simulation context")
                    } else {
                        Log.w(TAG, "Cannot update simulation context: No successful prediction available")
                        Log.d("DEBUG_CHICHI", "⚠️ No successful prediction available to update")
                    }
                } else {
                    val error = result.exceptionOrNull()
                    Log.e(TAG, "Failed to refresh simulation context", error)
                    Log.e("DEBUG_CHICHI", "❌ NewsImpactAnalyzer failed: ${error?.message}")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing simulation context", e)
                Log.e("DEBUG_CHICHI", "❌ Exception in refreshSimulationContext: ${e.message}")
            } finally {
                _isChiChiRefreshing.update { false }
                Log.d("DEBUG_CHICHI", "Refresh completed, isChiChiRefreshing set to false")
            }
        }
    }

    /**
     * Generate Mastermind verdict using Oracle and Tesseract data.
     * This triggers the MastermindEngine to analyze the data and produce a MastermindSignal.
     */
    fun generateMastermindVerdict() {
        Log.d(TAG, "Generating Mastermind verdict")
        
        val currentState = _predictionState.value
        if (currentState !is PredictionState.Success) {
            Log.w(TAG, "Cannot generate Mastermind verdict: No successful prediction available")
            return
        }
        
        val analysis = currentState.data
        val oracleData = analysis
        val tesseractData = analysis.tesseract
        
        if (tesseractData == null) {
            Log.w(TAG, "Cannot generate Mastermind verdict: Tesseract data is missing")
            return
        }
        
        _isMastermindAnalyzing.update { true }
        
        viewModelScope.launch {
            try {
                // Create MastermindEngine instance (in real app, this would be injected)
                val mastermindEngine = com.Lyno.matchmindai.domain.mastermind.MastermindEngine()
                
                // Analyze with MastermindEngine
                val verdict = mastermindEngine.analyze(
                    oracleAnalysis = oracleData,
                    tesseractResult = tesseractData
                )
                
                // Update OracleAnalysis with mastermindSignal
                val updatedAnalysis = analysis.copy(mastermindSignal = verdict)
                _predictionState.update { PredictionState.Success(updatedAnalysis) }
                Log.d(TAG, "✅ Mastermind verdict generated successfully")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error generating Mastermind verdict", e)
            } finally {
                _isMastermindAnalyzing.update { false }
            }
        }
    }

    /**
     * Clear all prediction and AI validation states.
     */
    fun clearState() {
        _predictionState.update { PredictionState.Idle }
        _aiValidationState.update { AiValidationState.Idle }
        _matchScenarioState.update { MatchScenarioState.Idle }
        _isChiChiRefreshing.update { false }
        _isMastermindAnalyzing.update { false }
    }
}

/**
 * UI state for Oracle prediction screen.
 */
sealed interface PredictionState {
    data object Idle : PredictionState
    data object Loading : PredictionState
    data class Success(val data: OracleAnalysis) : PredictionState
    data class Error(val message: String) : PredictionState
}

/**
 * UI state for AI Intelligence Layer validation (Phase 3).
 */
sealed interface AiValidationState {
    data object Idle : AiValidationState
    data object AnalyzingNews : AiValidationState
    data class Complete(
        val adjustment: OracleAdjustment?,
        val finalAnalysis: OracleAnalysis
    ) : AiValidationState
    data class Error(val message: String) : AiValidationState
}

/**
 * UI state for match scenario generation.
 */
sealed interface MatchScenarioState {
    data object Idle : MatchScenarioState
    data object Loading : MatchScenarioState
    data class Success(val scenario: String) : MatchScenarioState
    data class Error(val message: String) : MatchScenarioState
}

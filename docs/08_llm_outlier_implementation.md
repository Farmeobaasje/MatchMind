# 🧠 LLM-Enhanced Outlier Detection Implementation Plan

## 📋 Overzicht

**LLM-Enhanced Outlier Detection** is een nieuwe feature die DeepSeek LLM gebruikt om onverwachte wedstrijduitslagen te identificeren die traditionele modellen (Oracle, Tesseract) missen. Deze feature combineert kwantitatieve voorspellingen met kwalitatieve contextuele analyse.

## 🎯 Doelstellingen

### Primair Doel
Identificeer "uitschieter-scenario's" (onverwachte resultaten) die:
1. **Statistisch onwaarschijnlijk** zijn volgens Oracle/Tesseract
2. **Contextueel plausibel** zijn gebaseerd op kwalitatieve factoren
3. **Waarschijnlijkheid > 10%** hebben volgens LLM-analyse

### Secundaire Doelen
- Verbeter voorspellingsnauwkeurigheid met 5-10%
- Reduceer "bad beats" (verloren wedstrijden met dominante statistieken)
- Bied transparante uitleg voor onverwachte voorspellingen

## 🏗️ Architectuur

### High-Level Data Flow
```
Match Context
    ↓
Oracle Analysis (2-1, 75% confidence)
    ↓
Tesseract Simulation (10k Monte Carlo)
    ↓
Context Data Collection (News, Social, History)
    ↓
DeepSeek LLM Analysis
    ↓
Outlier Detection Engine
    ├─→ Standard Predictions (Oracle + Tesseract)
    └─→ Outlier Scenarios (LLM-enhanced)
        ↓
Enhanced Mastermind Decision
```

### Clean Architecture Layers

#### 1. Domain Layer (Pure Kotlin)
```
com.Lyno.matchmindai.domain.outlier/
├── model/
│   ├── OutlierAnalysis.kt
│   ├── OutlierScenario.kt
│   └── OutlierConfidence.kt
├── repository/
│   └── OutlierRepository.kt
└── usecase/
    └── DetectOutliersUseCase.kt
```

#### 2. Data Layer (Implementation)
```
com.Lyno.matchmindai.data.outlier/
├── repository/
│   └── OutlierRepositoryImpl.kt
├── dto/
│   └── DeepSeekOutlierResponse.kt
└── prompt/
    └── OutlierPromptBuilder.kt
```

#### 3. Presentation Layer (UI)
```
com.Lyno.matchmindai.presentation.components.outlier/
├── OutlierDetectionCard.kt
├── OutlierScenarioItem.kt
└── OutlierConfidenceBadge.kt
```

## 📦 Domain Models

### OutlierAnalysis
```kotlin
/**
 * Complete outlier analysis combining standard prediction with outlier scenarios.
 */
data class OutlierAnalysis(
    // Standard prediction (from Oracle/Tesseract)
    val standardPrediction: String,          // "2-1"
    val standardConfidence: Int,             // 75%
    val standardReasoning: String,           // "Based on power difference..."
    
    // Outlier scenarios identified by LLM
    val outlierScenarios: List<OutlierScenario>,
    
    // Overall confidence in outlier detection
    val outlierConfidence: OutlierConfidence,
    
    // Context summary
    val contextSummary: String,              // "High distraction in home team..."
    
    // Timestamp
    val analyzedAt: Long = System.currentTimeMillis()
)

/**
 * Individual outlier scenario with probability and explanation.
 */
data class OutlierScenario(
    val id: String = UUID.randomUUID().toString(),
    val predictedScore: String,              // "3-0", "0-2", "1-3"
    val probability: Double,                 // 0.15 (15%)
    val explanation: String,                 // "Regen verwacht, Team X slecht in natte omstandigheden"
    
    // Supporting evidence
    val evidence: List<OutlierEvidence>,
    
    // Risk level
    val riskLevel: OutlierRiskLevel,         // LOW, MEDIUM, HIGH
    
    // Impact on betting markets
    val bettingImplications: List<String>    // ["Over 3.5 goals", "BTTS No"]
)

/**
 * Evidence supporting an outlier scenario.
 */
data class OutlierEvidence(
    val type: EvidenceType,                  // NEWS, SOCIAL, HISTORICAL, WEATHER
    val source: String,                      // "BBC Sport", "Twitter", "Historical Database"
    val content: String,                     // "Team X has lost 3 of last 4 rainy matches"
    val relevanceScore: Double               // 0.0-1.0
)

/**
 * Confidence level in outlier analysis.
 */
enum class OutlierConfidence(val value: Int, val color: Color) {
    HIGH(85, Color.Green),      // Strong evidence
    MEDIUM(65, Color.Yellow),   // Moderate evidence  
    LOW(40, Color.Orange),      // Weak evidence
    VERY_LOW(20, Color.Red)     // Speculative
}

/**
 * Risk level of outlier scenario.
 */
enum class OutlierRiskLevel {
    LOW,        // Minor deviation (1-0 → 2-0)
    MEDIUM,     // Moderate deviation (2-1 → 3-0)
    HIGH,       // Major deviation (1-1 → 0-3)
    EXTREME     // Complete reversal (2-0 → 0-3)
}
```

## 🔧 Use Case Implementation

### DetectOutliersUseCase
```kotlin
class DetectOutliersUseCase(
    private val outlierRepository: OutlierRepository,
    private val newsRepository: NewsRepository,
    private val matchRepository: MatchRepository
) {
    /**
     * Detect outlier scenarios for a match.
     */
    suspend operator fun invoke(
        matchContext: MatchContext,
        oracleAnalysis: OracleAnalysis,
        tesseractResult: TesseractResult
    ): Result<OutlierAnalysis> = withContext(Dispatchers.IO) {
        return@withContext try {
            // 1. Collect contextual data
            val contextData = collectContextData(matchContext)
            
            // 2. Analyze with LLM
            val outlierAnalysis = outlierRepository.detectOutliers(
                matchContext = matchContext,
                oracleAnalysis = oracleAnalysis,
                tesseractResult = tesseractResult,
                contextData = contextData
            )
            
            // 3. Validate and filter scenarios
            val validatedAnalysis = validateOutliers(
                outlierAnalysis,
                matchContext,
                oracleAnalysis
            )
            
            Result.success(validatedAnalysis)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun collectContextData(
        matchContext: MatchContext
    ): ContextData {
        return ContextData(
            // News articles
            news = newsRepository.fetchTeamNews(
                teamA = matchContext.homeTeam,
                teamB = matchContext.awayTeam
            ),
            
            // Historical anomalies
            historicalAnomalies = matchRepository.getHistoricalUpsets(
                homeTeam = matchContext.homeTeam,
                awayTeam = matchContext.awayTeam
            ),
            
            // Weather forecast (if available)
            weather = getWeatherForecast(matchContext),
            
            // Team-specific context
            teamContext = getTeamContext(matchContext)
        )
    }
    
    private fun validateOutliers(
        analysis: OutlierAnalysis,
        matchContext: MatchContext,
        oracleAnalysis: OracleAnalysis
    ): OutlierAnalysis {
        // Filter out unrealistic scenarios
        val validScenarios = analysis.outlierScenarios.filter { scenario ->
            // Minimum probability threshold
            scenario.probability >= 0.10 &&
            
            // Maximum deviation from standard prediction
            getScoreDeviation(scenario.predictedScore, oracleAnalysis.prediction) <= 3 &&
            
            // Has supporting evidence
            scenario.evidence.isNotEmpty() &&
            scenario.evidence.any { it.relevanceScore >= 0.5 }
        }
        
        // Recalculate confidence based on filtered scenarios
        val newConfidence = calculateConfidence(validScenarios)
        
        return analysis.copy(
            outlierScenarios = validScenarios,
            outlierConfidence = newConfidence
        )
    }
}
```

## 📡 Data Layer Implementation

### OutlierRepositoryImpl
```kotlin
class OutlierRepositoryImpl(
    private val deepSeekApi: DeepSeekApi,
    private val promptBuilder: PromptBuilder
) : OutlierRepository {
    
    override suspend fun detectOutliers(
        matchContext: MatchContext,
        oracleAnalysis: OracleAnalysis,
        tesseractResult: TesseractResult,
        contextData: ContextData
    ): OutlierAnalysis {
        // 1. Build prompt for LLM
        val prompt = promptBuilder.buildOutlierDetectionPrompt(
            matchContext = matchContext,
            oracleAnalysis = oracleAnalysis,
            tesseractResult = tesseractResult,
            contextData = contextData
        )
        
        // 2. Call DeepSeek API with structured JSON output
        val response = deepSeekApi.analyzeWithJsonOutput(
            prompt = prompt,
            responseFormat = mapOf("type" to "json_object")
        )
        
        // 3. Parse response
        return parseOutlierResponse(response, matchContext)
    }
    
    private fun parseOutlierResponse(
        response: DeepSeekResponse,
        matchContext: MatchContext
    ): OutlierAnalysis {
        return try {
            // Parse JSON response
            val json = Json.parseToJsonElement(response.content)
            
            OutlierAnalysis(
                standardPrediction = json["standard_prediction"]?.jsonPrimitive?.content ?: "",
                standardConfidence = json["standard_confidence"]?.jsonPrimitive?.int ?: 0,
                standardReasoning = json["standard_reasoning"]?.jsonPrimitive?.content ?: "",
                
                outlierScenarios = parseScenarios(json["outlier_scenarios"]),
                
                outlierConfidence = parseConfidence(json["outlier_confidence"]),
                
                contextSummary = json["context_summary"]?.jsonPrimitive?.content ?: ""
            )
        } catch (e: Exception) {
            // Fallback to empty analysis
            createEmptyAnalysis(matchContext)
        }
    }
}
```

### Prompt Builder
```kotlin
object OutlierPromptBuilder {
    
    fun buildOutlierDetectionPrompt(
        matchContext: MatchContext,
        oracleAnalysis: OracleAnalysis,
        tesseractResult: TesseractResult,
        contextData: ContextData
    ): String {
        return """
        # OUTLIER DETECTION ANALYSIS - FOOTBALL MATCH
        
        ## MATCH DETAILS
        ${matchContext.homeTeam} vs ${matchContext.awayTeam}
        League: ${matchContext.league}
        Date: ${matchContext.date}
        
        ## STANDARD PREDICTIONS
        Oracle Prediction: ${oracleAnalysis.prediction} (${oracleAnalysis.confidence}% confidence)
        Tesseract Simulation: ${tesseractResult.mostLikelyScore}
        - Home Win: ${tesseractResult.homeWinPercentage}%
        - Draw: ${tesseractResult.drawPercentage}%
        - Away Win: ${tesseractResult.awayWinPercentage}%
        
        ## CONTEXTUAL DATA
        ${formatContextData(contextData)}
        
        ## ANALYSIS INSTRUCTIONS
        1. Identify potential OUTLIER SCENARIOS (unexpected results) that traditional models might miss
        2. Consider: weather conditions, team psychology, historical anomalies, recent news
        3. For each outlier scenario, provide:
           - Predicted score (e.g., "3-0", "0-2")
           - Probability (0.10 to 0.30)
           - Explanation (specific, evidence-based)
           - Supporting evidence from context data
           - Risk level (LOW, MEDIUM, HIGH)
           - Betting implications
        
        4. Focus on SCENARIOS WITH >10% PROBABILITY
        5. Maximum 3 outlier scenarios
        6. Provide overall confidence in outlier detection
        
        ## OUTPUT FORMAT (JSON)
        {
          "standard_prediction": "${oracleAnalysis.prediction}",
          "standard_confidence": ${oracleAnalysis.confidence},
          "standard_reasoning": "Brief reasoning for standard prediction",
          
          "outlier_scenarios": [
            {
              "predicted_score": "3-0",
              "probability": 0.15,
              "explanation": "Team X performs poorly in rainy conditions...",
              "evidence": [
                {
                  "type": "WEATHER",
                  "source": "Weather forecast",
                  "content": "Heavy rain expected during match",
                  "relevance_score": 0.8
                }
              ],
              "risk_level": "MEDIUM",
              "betting_implications": ["Over 2.5 goals", "Home team to win to nil"]
            }
          ],
          
          "outlier_confidence": "MEDIUM",
          "context_summary": "Summary of key contextual factors..."
        }
        """.trimIndent()
    }
}
```

## 🎨 UI Components

### OutlierDetectionCard
```kotlin
@Composable
fun OutlierDetectionCard(
    outlierAnalysis: OutlierAnalysis,
    modifier: Modifier = Modifier,
    onScenarioClick: (OutlierScenario) -> Unit = {}
) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🔮 ENHANCED PREDICTIE",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.PrimaryNeon
                    )
                    Text(
                        text = "LLM Outlier Detection",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.TextMedium
                    )
                }
                
                OutlierConfidenceBadge(confidence = outlierAnalysis.outlierConfidence)
            }
            
            Divider(color = MaterialTheme.colorScheme.SurfaceCard.copy(alpha = 0.3f))
            
            // Standard Prediction
            StandardPredictionSection(
                prediction = outlierAnalysis.standardPrediction,
                confidence = outlierAnalysis.standardConfidence,
                reasoning = outlierAnalysis.standardReasoning
            )
            
            // Outlier Scenarios
            if (outlierAnalysis.outlierScenarios.isNotEmpty()) {
                OutlierScenariosSection(
                    scenarios = outlierAnalysis.outlierScenarios,
                    onScenarioClick = onScenarioClick
                )
            } else {
                NoOutliersState()
            }
            
            // Context Summary
            ContextSummarySection(summary = outlierAnalysis.contextSummary)
        }
    }
}

@Composable
private fun OutlierScenariosSection(
    scenarios: List<OutlierScenario>,
    onScenarioClick: (OutlierScenario) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "🎲 POTENTIËLE UITSCHIETERS:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.TextHigh
        )
        
        scenarios.forEach { scenario ->
            OutlierScenarioItem(
                scenario = scenario,
                onClick = { onScenarioClick(scenario) }
            )
        }
    }
}

@Composable
private fun OutlierScenarioItem(
    scenario: OutlierScenario,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (scenario.riskLevel) {
                OutlierRiskLevel.LOW -> MaterialTheme.colorScheme.SurfaceCard
                OutlierRiskLevel.MEDIUM -> Color.Yellow.copy(alpha = 0.1f)
                OutlierRiskLevel.HIGH -> Color.Orange.copy(alpha = 0.1f)
                OutlierRiskLevel.EXTREME -> Color.Red.copy(alpha = 0.1f)
            }
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = scenario.predictedScore,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.TextHigh
                )
                
                // Probability badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(
                            color = getProbabilityColor(scenario.probability),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${(scenario.probability * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
            
            // Explanation
            Text(
                text = scenario.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.TextMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            // Evidence indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                scenario.evidence.take(3).forEach { evidence ->
                    EvidenceChip(evidence = evidence)
                }
            }
        }
    }
}
```

## 🔗 Integration Points

### 1. MatchDetailScreen Integration
```kotlin
@Composable
fun MatchDetailScreen(
    viewModel: MatchDetailViewModel = hiltViewModel(),
    navController: NavHostController
) {
    // ... existing code ...
    
    // Add new tab for Outlier Detection
    val tabs = listOf("Live", "Details", "Oracle", "Tips", "Analyse", "Mastermind", "Verslag", "Outliers")
    
    // In PredictionTab or new OutlierTab
    LaunchedEffect(viewModel.predictionState.value) {
        val prediction = viewModel.predictionState.value
        if (prediction is Resource.Success) {
            // Trigger outlier detection
            viewModel.detectOutliers(prediction.data)
        }
    }
    
    // Show OutlierDetectionCard when available

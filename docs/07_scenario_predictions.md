# 07. Scenario-Based Match Predictions & Score Forecasting

## 🎯 Overzicht: Enhanced Prediction System

MatchMind AI 2.0 introduceert een geavanceerd scenario-based prediction systeem dat verder gaat dan traditionele voorspellingen. In plaats van één enkele uitkomst, genereert het systeem **3-5 scenario's** met concrete score voorspellingen, waarschijnlijkheden en impact analyses.

---

## 1. SCENARIO MODEL ARCHITECTURE

### 1.1 Enhanced MatchReport.kt - Scenario Integration
```kotlin
@Serializable
data class MatchReport(
    // ... bestaande fields
    
    // Enhanced scenario system
    val scenarios: List<MatchScenario> = emptyList(),
    val primaryScenario: MatchScenario? = null,
    val scorePredictions: ScorePredictionMatrix? = null,
    
    // Interactive elements
    val scenarioEvolution: ScenarioEvolution? = null,
    val userScenarioPreferences: Map<String, Double> = emptyMap()
)

/**
 * Individual match scenario with probability and impact analysis.
 */
@Serializable
data class MatchScenario(
    val id: String,                    // Unique scenario identifier
    val title: String,                 // Short descriptive title
    val description: String,           // Detailed narrative description
    val predictedScore: String,        // Concrete score prediction (e.g., "2-1")
    val probability: Int,              // 0-100% likelihood
    val confidence: Int,               // 0-100% confidence in this prediction
    
    // Impact analysis
    val chaosImpact: Int,              // How this scenario affects chaos (0-100)
    val atmosphereImpact: Int,         // How this affects atmosphere (0-100)
    val bettingValue: Double,          // Betting value score (0.0-1.0)
    
    // Key factors
    val keyFactors: List<String>,      // What drives this scenario
    val triggerEvents: List<String>,   // What could trigger this scenario
    val timeline: List<ScenarioTimelineEvent>, // How scenario unfolds
    
    // Data sources
    val dataSources: List<String>,     // Which data supports this scenario
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Timeline event within a scenario.
 */
@Serializable
data class ScenarioTimelineEvent(
    val minute: Int,                   // Match minute
    val eventType: ScenarioEventType,  // Type of event
    val description: String,           // What happens
    val impact: Int                    // Impact on scenario (0-100)
)

/**
 * Types of events in scenario timelines.
 */
enum class ScenarioEventType {
    GOAL,              // Goal scored
    RED_CARD,          // Red card
    INJURY,           // Key player injury
    TACTICAL_CHANGE,   // Tactical substitution/change
    MOMENTUM_SHIFT,    // Momentum change
    CONTROVERSY,       // Controversial decision
    WEATHER_CHANGE,    // Weather impact
    CROWD_INFLUENCE    // Crowd influence
}

/**
 * Score prediction matrix with probabilities.
 */
@Serializable
data class ScorePredictionMatrix(
    val mostLikelyScore: String,       // Most probable score (e.g., "2-1")
    val mostLikelyProbability: Int,    // Probability of most likely score
    
    // Score ranges
    val homeWinScores: List<ScoreProbability>,    // All home win scores
    val drawScores: List<ScoreProbability>,       // All draw scores
    val awayWinScores: List<ScoreProbability>,    // All away win scores
    
    // Aggregated probabilities
    val homeWinProbability: Int,       // Probability of home win
    val drawProbability: Int,          // Probability of draw
    val awayWinProbability: Int,       // Probability of away win
    
    // Expected goals
    val expectedHomeGoals: Double,     // xG for home team
    val expectedAwayGoals: Double,     // xG for away team
    val goalExpectationRange: String   // Range (e.g., "2.5-3.5 goals")
)

/**
 * Individual score with probability.
 */
@Serializable
data class ScoreProbability(
    val score: String,     // e.g., "2-1"
    val probability: Int,  // 0-100%
    val confidence: Int    // 0-100% confidence
)

/**
 * Scenario evolution over time.
 */
@Serializable
data class ScenarioEvolution(
    val preMatchScenarios: List<MatchScenario>,    // Before match
    val liveScenarios: List<MatchScenario>,        // During match (updated)
    val postMatchAnalysis: ScenarioAnalysis?,      // After match
    
    // Evolution tracking
    val scenarioChanges: List<ScenarioChange>,     // How scenarios changed
    val accuracyScore: Int? = null                 // How accurate predictions were
)

/**
 * Analysis of scenario accuracy post-match.
 */
@Serializable
data class ScenarioAnalysis(
    val actualScore: String,                       // Actual match score
    val closestScenario: MatchScenario?,           // Which scenario was closest
    val accuracyPercentage: Int,                   // How accurate overall
    val lessonsLearned: List<String>               // Insights for future
)
```

### 1.2 Scenario Generation Engine
```kotlin
class ScenarioEngine(
    private val matchRepository: MatchRepository,
    private val newsImpactAnalyzer: NewsImpactAnalyzer,
    private val expectedGoalsService: ExpectedGoalsService
) {
    /**
     * Generate 3-5 scenarios for a match.
     */
    suspend fun generateScenarios(
        matchDetail: MatchDetail,
        hybridPrediction: HybridPrediction
    ): Result<List<MatchScenario>>
    
    /**
     * Calculate score prediction matrix.
     */
    suspend fun calculateScoreMatrix(
        matchDetail: MatchDetail,
        scenarios: List<MatchScenario>
    ): Result<ScorePredictionMatrix>
    
    /**
     * Update scenarios based on live match events.
     */
    suspend fun updateScenariosForLiveMatch(
        fixtureId: Int,
        liveEvents: List<LiveMatchEvent>
    ): Result<List<MatchScenario>>
}
```

---

## 2. SCENARIO TYPES & TEMPLATES

### 2.1 Scenario Categories
```kotlin
enum class ScenarioCategory {
    DOMINANT_HOME_WIN,      // Home team dominates
    NARROW_HOME_WIN,        // Close home win
    HIGH_SCORING_DRAW,      // Entertaining draw
    LOW_SCORING_DRAW,       // Tactical stalemate
    AWAY_TEAM_SURPRISE,     // Away team upset
    GOAL_FEST,              // Many goals expected
    DEFENSIVE_BATTLE,       // Few goals expected
    CONTROVERSIAL_OUTCOME,  // Referee/controversy impact
    LATE_DRAMA,             // Late goals/comeback
    ONE_SIDED_AFFAIR        // Complete domination
}
```

### 2.2 Scenario Templates (ReportTemplates.kt Extensie)
```kotlin
object ScenarioTemplates {
    
    fun getScenarioTitle(category: ScenarioCategory, homeTeam: String, awayTeam: String): String {
        return when (category) {
            ScenarioCategory.DOMINANT_HOME_WIN -> "🏠 $homeTeam Domineert Thuis"
            ScenarioCategory.NARROW_HOME_WIN -> "⚔️ Krappe Zege voor $homeTeam"
            ScenarioCategory.HIGH_SCORING_DRAW -> "🤝 Goalrijke Gelijkmaker"
            ScenarioCategory.LOW_SCORING_DRAW -> "🛡️ Tactische Patstelling"
            ScenarioCategory.AWAY_TEAM_SURPRISE -> "🎯 $awayTeam Verrast Uit"
            ScenarioCategory.GOAL_FEST -> "🎆 Goal Festival"
            ScenarioCategory.DEFENSIVE_BATTLE -> "🛡️ Verdedigingsslag"
            ScenarioCategory.CONTROVERSIAL_OUTCOME -> "⚖️ Controversiële Uitslag"
            ScenarioCategory.LATE_DRAMA -> "⌛ Laat Drama"
            ScenarioCategory.ONE_SIDED_AFFAIR -> "💥 Eenzijdige Affaire"
        }
    }
    
    fun getScenarioDescription(
        category: ScenarioCategory,
        homeTeam: String,
        awayTeam: String,
        predictedScore: String
    ): String {
        return when (category) {
            ScenarioCategory.DOMINANT_HOME_WIN ->
                "$homeTeam controleert de wedstrijd van begin tot eind en wint overtuigend met $predictedScore. " +
                "Het thuisvoordeel is beslissend en de kwaliteitsverschillen zijn duidelijk zichtbaar."
                
            ScenarioCategory.NARROW_HOME_WIN ->
                "Een nek-aan-nek race die $homeTeam nipt wint met $predictedScore. " +
                "Een individuele actie of set-piece maakt het verschil in deze evenwichtige confrontatie."
                
            // ... templates voor alle categories
        }
    }
    
    fun getTimelineEvents(
        category: ScenarioCategory,
        homeTeam: String,
        awayTeam: String
    ): List<ScenarioTimelineEvent> {
        return when (category) {
            ScenarioCategory.DOMINANT_HOME_WIN -> listOf(
                ScenarioTimelineEvent(15, ScenarioEventType.GOAL, 
                    "$homeTeam scoort vroeg en neemt controle", 70),
                ScenarioTimelineEvent(40, ScenarioEventType.GOAL, 
                    "2-0 voor rust bevestigt dominantie", 85),
                ScenarioTimelineEvent(75, ScenarioEventType.TACTICAL_CHANGE, 
                    "$awayTeam wisselt offensief maar zonder effect", 40)
            )
            // ... timelines voor alle categories
        }
    }
}
```

---

## 3. INTERACTIVE UI COMPONENTS

### 3.1 ScenarioExplorer.kt - Main Interactive Component
```kotlin
@Composable
fun ScenarioExplorer(
    matchReport: MatchReport,
    onScenarioSelected: (MatchScenario) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Scenario selector
        ScenarioSelector(
            scenarios = matchReport.scenarios,
            selectedScenario = matchReport.primaryScenario,
            onScenarioSelected = onScenarioSelected
        )
        
        // Score prediction matrix
        matchReport.scorePredictions?.let { matrix ->
            ScorePredictionMatrixView(
                matrix = matrix,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Scenario timeline
        matchReport.primaryScenario?.let { scenario ->
            ScenarioTimelineView(
                timeline = scenario.timeline,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // What-if analysis
        WhatIfAnalysisPanel(
            matchReport = matchReport,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
```

### 3.2 ScenarioSelector.kt - Visual Scenario Cards
```kotlin
@Composable
fun ScenarioSelector(
    scenarios: List<MatchScenario>,
    selectedScenario: MatchScenario?,
    onScenarioSelected: (MatchScenario) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(scenarios) { scenario ->
            ScenarioCard(
                scenario = scenario,
                isSelected = selectedScenario?.id == scenario.id,
                onClick = { onScenarioSelected(scenario) }
            )
        }
    }
}

@Composable
fun ScenarioCard(
    scenario: MatchScenario,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.width(280.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryNeon.copy(alpha = 0.1f) 
                           else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) BorderStroke(2.dp, PrimaryNeon) else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with probability
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = scenario.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Badge(
                    containerColor = when {
                        scenario.probability >= 60 -> Color.Green.copy(alpha = 0.2f)
                        scenario.probability >= 40 -> Color.Yellow.copy(alpha = 0.2f)
                        else -> Color.Red.copy(alpha = 0.2f)
                    },
                    contentColor = when {
                        scenario.probability >= 60 -> Color.Green
                        scenario.probability >= 40 -> Color.Yellow
                        else -> Color.Red
                    }
                ) {
                    Text(
                        text = "${scenario.probability}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Predicted score
            Text(
                text = "📊 ${scenario.predictedScore}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryNeon
            )
            
            // Short description
            Text(
                text = scenario.description.take(100) + if (scenario.description.length > 100) "..." else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            
            // Key factors
            if (scenario.keyFactors.isNotEmpty()) {
                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = "• ${scenario.keyFactors.first()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
```

### 3.3 ScorePredictionMatrixView.kt - Visual Probability Matrix
```kotlin
@Composable
fun ScorePredictionMatrixView(
    matrix: ScorePredictionMatrix,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "🎯 Score Voorspelling Matrix",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryNeon
            )
            
            // Most likely score
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "MEEST WAARSCHIJNLIJK",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = matrix.mostLikelyScore,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNeon
                    )
                    Text(
                        text = "${matrix.mostLikelyProbability}% kans",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Win probabilities
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProbabilityBar(
                        label = "Thuis Wint",
                        probability = matrix.homeWinProbability,
                        color = Color.Green
                    )
                    ProbabilityBar(
                        label = "Gelijk",
                        probability = matrix.drawProbability,
                        color = Color.Yellow
                    )
                    ProbabilityBar(
                        label = "Uit Wint",
                        probability = matrix.awayWinProbability,
                        color = Color.Red
                    )
                }
            }
            
            // Expected goals
            Divider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Verwachte Goals",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = matrix.goalExpectationRange,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "xG Thuis",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "%.1f".format(matrix.expectedHomeGoals),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Green
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "xG Uit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "%.1f".format(matrix.expectedAwayGoals),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }
            }
        }
    }
}

/**
 * Probability bar for win/draw/loss probabilities.
 */
@Composable
private fun ProbabilityBar(
    label: String,
    probability: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = probability / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
        Text(
            text = "$probability%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
```

### 3.4 ScenarioTimelineView.kt - Visual Timeline
```kotlin
@Composable
fun ScenarioTimelineView(
    timeline: List<ScenarioTimelineEvent>,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Text(
                text = "⏰ Scenario Verloop",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryNeon
            )
            
            // Timeline events
            timeline.forEach { event ->
                TimelineEventItem(event = event)
            }
        }
    }
}

@Composable
private fun TimelineEventItem(event: ScenarioTimelineEvent) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Minute badge
        Badge(
            containerColor = PrimaryNeon.copy(alpha = 0.1f),
            contentColor = PrimaryNeon
        ) {
            Text(
                text = "${event.minute}'",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Event icon
        Text(
            text = when (event.eventType) {
                ScenarioEventType.GOAL -> "⚽"
                ScenarioEventType.RED_CARD -> "🟥"
                ScenarioEventType.INJURY -> "🤕"
                ScenarioEventType.TACTICAL_CHANGE -> "🔄"
                ScenarioEventType.MOMENTUM_SHIFT -> "📈"
                ScenarioEventType.CONTROVERSY -> "⚖️"
                ScenarioEventType.WEATHER_CHANGE -> "🌧️"
                ScenarioEventType.CROWD_INFLUENCE -> "👥"
            },
            fontSize = 20.sp
        )
        
        // Event description
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Impact indicator
            if (event.impact >= 70) {
                Text(
                    text = "Hoog impact",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Red,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}
```

### 3.5 WhatIfAnalysisPanel.kt - Interactive Analysis
```kotlin
@Composable
fun WhatIfAnalysisPanel(
    matchReport: MatchReport,
    modifier: Modifier = Modifier
) {
    var showWhatIfDialog by remember { mutableStateOf(false) }
    
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🤔 What-If Analyse",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNeon
                )
                
                Button(
                    onClick = { showWhatIfDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryNeon.copy(alpha = 0.1f),
                        contentColor = PrimaryNeon
                    ),
                    border = BorderStroke(1.dp, PrimaryNeon.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Test Scenario",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            
            Text(
                text = "Wat gebeurt er als...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // What-if options
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                WhatIfOption(
                    text = "Blessure sleutelspeler",
                    onClick = { /* Update scenarios with injury */ }
                )
                WhatIfOption(
                    text = "Rode kaart",
                    onClick = { /* Update scenarios with red card */ }
                )
                WhatIfOption(
                    text = "Vroeg doelpunt",
                    onClick = { /* Update scenarios with early goal */ }
                )
            }
        }
    }
    
    // What-if dialog
    if (showWhatIfDialog) {
        WhatIfDialog(
            onDismiss = { showWhatIfDialog = false },
            matchReport = matchReport
        )
    }
}

@Composable
private fun WhatIfOption(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
```

---

## 4. SCENARIO EVOLUTION SYSTEM

### 4.1 Live Scenario Updates
```kotlin
class LiveScenarioUpdater(
    private val scenarioEngine: ScenarioEngine,
    private val matchRepository: MatchRepository
) {
    /**
     * Subscribe to live match events and update scenarios.
     */
    suspend fun subscribeToLiveMatch(
        fixtureId: Int,
        onScenariosUpdated: (List<MatchScenario>) -> Unit
    ): Job {
        return coroutineScope {
            launch {
                matchRepository.getLiveMatchEvents(fixtureId).collect { events ->
                    val updatedScenarios = scenarioEngine.updateScenariosForLiveMatch(
                        fixtureId = fixtureId,
                        liveEvents = events
                    )
                    updatedScenarios.onSuccess { scenarios ->
                        onScenariosUpdated(scenarios)
                    }
                }
            }
        }
    }
    
    /**
     * Calculate scenario impact of a specific event.
     */
    fun calculateEventImpact(
        event: LiveMatchEvent,
        currentScenarios: List<MatchScenario>
    ): Map<String, Int> {
        // Calculate how each scenario probability changes
        return currentScenarios.associate { scenario ->
            scenario.id to calculateScenarioImpact(scenario, event)
        }
    }
}
```

### 4.2 Time-Based Scenario Evolution
```kotlin
data class ScenarioEvolutionTracker(
    val fixtureId: Int,
    val timeline: List<ScenarioEvolutionSnapshot>,
    val accuracyMetrics: EvolutionAccuracyMetrics
)

data class ScenarioEvolutionSnapshot(
    val timestamp: Long,
    val scenarios: List<MatchScenario>,
    val triggerEvent: String?,
    val confidenceChange: Double
)

data class EvolutionAccuracyMetrics(
    val preMatchAccuracy: Double,
    val liveAccuracy: Double,
    val finalAccuracy: Double,
    val learningPoints: List<String>
)
```

---

## 5. USER EXPERIENCE FLOW

### 5.1 Pre-Match Analysis Flow
```
1. USER: Opent match detail
2. SYSTEM: Laadt hybrid prediction + Mastermind analysis
3. SYSTEM: Genereert 3-5 scenario's met scores
4. UI: Toont ScenarioExplorer met:
   - Scenario cards (horizontale scroll)
   - Score prediction matrix
   - Most likely score highlight
5. USER: Selecteert scenario voor details
6. UI: Toont timeline + what-if analysis
```

### 5.2 Live Match Experience
```
1. SYSTEM: Subscribe to live events
2. ON EVENT: Update scenario probabilities
3. UI: Animate probability changes
4. USER: Ziet real-time scenario evolutie
5. SYSTEM: Track accuracy voor learning
```

### 5.3 Post-Match Analysis
```
1. SYSTEM: Vergelijk voorspellingen met werkelijke uitslag
2. UI: Toont accuracy score + lessons learned
3. SYSTEM: Update AI model met nieuwe data
4. USER: Krijgt feedback over prediction kwaliteit
```

---

## 6. IMPLEMENTATION ROADMAP

### 6.1 Phase 1: Core Scenario Model (Week 1)
- [ ] Extend MatchReport.kt with scenario fields
- [ ] Create MatchScenario data classes
- [ ] Implement ScenarioEngine basic generation
- [ ] Add ScenarioTemplates for 3 main categories

### 6.2 Phase 2: UI Components (Week 2)
- [ ] Create ScenarioExplorer composable
- [ ] Implement ScenarioSelector with cards
- [ ] Build ScorePredictionMatrixView
- [ ] Add ScenarioTimelineView

### 6.3 Phase 3: Interactive Features (Week 3)
- [ ] Implement WhatIfAnalysisPanel
- [ ] Add live scenario updates
- [ ] Create scenario evolution tracking
- [ ] Build accuracy analytics

### 6.4 Phase 4: Polish & Optimization (Week 4)
- [ ] Performance optimization
- [ ] Animation and transitions
- [ ] User testing and feedback
- [ ] Documentation and examples

---

## 7. SUCCESS METRICS

### 7.1 Technical Metrics
- **Scenario Generation Time**: < 2 seconds
- **Live Update Latency**: < 1 second
- **UI Responsiveness**: 60 FPS animations
- **Memory Usage**: < 50MB per match analysis

### 7.2 User Experience Metrics
- **Scenario Accuracy**: > 65% correct predictions
- **User Engagement**: > 2 minutes per scenario exploration
- **What-If Usage**: > 30% of users interact with what-if
- **Satisfaction Score**: > 4.0/5.0

### 7.3 Business Metrics
- **Retention Increase**: +15% users return for scenario updates
- **Session Length**: +25% longer sessions
- **Feature Adoption**: > 40% of users use scenario features
- **Monetization Potential**: Premium scenario features

---

## 8. CONCLUSION

Het scenario-based prediction systeem transformeert MatchMind AI van een statische voorspeller naar een dynamische, interactieve analyse tool. Gebruikers krijgen niet één antwoord, maar een spectrum van mogelijkheden met concrete scores, waarschijnlijkheden en impact analyses.

### **Key Value Propositions:**
1. **Rich Context**: 3-5 scenario's geven completer beeld dan één voorspelling
2. **Concrete Scores**: Specifieke score voorspellingen met percentages
3. **Interactive Exploration**: What-if analysis voor begrip van variabelen
4. **Live Evolution**: Real-time updates tijdens wedstrijden
5. **Learning System**: Accuracy tracking voor continue verbetering

### **Competitive Advantage:**
- **Unieke Multi-Scenario Approach**: Geen andere app biedt dit niveau van detail
- **Interactive Timeline**: Visual verloop van hoe scenario's zich ontwikkelen
- **Live Adaptation**: Scenario's die meebewegen met de wedstrijd
- **Educational Value**: Gebruikers leren voetbal door scenario's te begrijpen

Deze implementatie positioneert MatchMind AI als de meest geavanceerde voetbal analyse tool op de markt, met een focus op educatie, interactie en accurate voorspellingen.

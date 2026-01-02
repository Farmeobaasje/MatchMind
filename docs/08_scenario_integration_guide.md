# Scenario Integration Guide - MatchMind AI 2.0

**Status**: Phase 2 - Scenario Engine Implementation ✅  
**Last Updated**: 21/12/2025  
**Author**: Senior Android Architect

## 📋 Executive Summary

The Scenario Engine is now successfully integrated into MatchMind AI 2.0. This document provides a comprehensive guide to the implementation, current status, and next steps for completing the scenario prediction functionality.

## 🏗️ Architecture Overview

### Current Implementation Status

```
✅ COMPLETED:
├── ScenarioEngine.kt - Core scenario generation logic
├── ScenarioTemplates.kt - Template system for scenarios
├── MatchScenario.kt - Data model for scenarios
├── ScorePredictionMatrix.kt - Score prediction data structure
├── NewsImpactAnalyzer.kt - AI-powered news impact analysis
├── AppContainer.kt - Dependency injection setup
└── Build System - Successful compilation & deployment

🔄 IN PROGRESS:
├── UI Integration - Scenario display in MatchReportCard
├── Data Flow - Scenario generation in ViewModel
└── End-to-End Testing - Full scenario workflow
```

## 🔧 Technical Implementation

### 1. Core Components

#### ScenarioEngine (`domain/service/ScenarioEngine.kt`)
- **Purpose**: Generates 3-5 match scenarios with probabilities
- **3-Layer Approach**:
  1. **Base Scenarios**: From templates based on team strengths
  2. **Data-Driven Modifiers**: Stats, injuries, news impact
  3. **AI Enhancement**: Context-aware adjustments
- **Constructor**: `ScenarioEngine(matchRepository, newsImpactAnalyzer, expectedGoalsService)`

#### NewsImpactAnalyzer (`domain/service/NewsImpactAnalyzer.kt`)
- **Purpose**: Analyzes news impact using AI feature engineering
- **Key Features**:
  - Uses DeepSeek for quantitative modifier generation
  - Focuses on feature engineering, not direct prediction
  - Generates structured JSON with team strength modifiers
- **Constructor**: `NewsImpactAnalyzer(matchRepository)`

### 2. Data Models

#### MatchScenario (`domain/model/MatchScenario.kt`)
```kotlin
data class MatchScenario(
    val id: String,
    val title: String,
    val description: String,
    val predictedScore: String,
    val probability: Int, // 0-100%
    val confidence: Int, // 0-100%
    val chaosImpact: Int,
    val atmosphereImpact: Int,
    val bettingValue: String,
    val keyFactors: List<String>,
    val triggerEvents: List<String>,
    val timeline: List<String>,
    val dataSources: List<String>
)
```

#### ScorePredictionMatrix (`domain/model/ScorePredictionMatrix.kt`)
```kotlin
data class ScorePredictionMatrix(
    val mostLikelyScore: String,
    val mostLikelyProbability: Int,
    val homeWinScores: List<ScoreProbability>,
    val drawScores: List<ScoreProbability>,
    val awayWinScores: List<ScoreProbability>,
    val homeWinProbability: Int,
    val drawProbability: Int,
    val awayWinProbability: Int,
    val expectedHomeGoals: Double,
    val expectedAwayGoals: Double,
    val goalExpectationRange: String
)
```

### 3. Dependency Injection

#### AppContainer (`di/AppContainer.kt`)
```kotlin
// News Impact Analyzer
val newsImpactAnalyzer: NewsImpactAnalyzer by lazy {
    NewsImpactAnalyzer(matchRepository)
}

// Expected Goals Service  
val expectedGoalsService: ExpectedGoalsService by lazy {
    ExpectedGoalsService()
}

// Scenario Engine
val scenarioEngine: ScenarioEngine by lazy {
    ScenarioEngine(
        matchRepository = matchRepository,
        newsImpactAnalyzer = newsImpactAnalyzer,
        expectedGoalsService = expectedGoalsService
    )
}

// Match Report Generator
val matchReportGenerator: MatchReportGenerator by lazy {
    MatchReportGenerator(scenarioEngine = scenarioEngine)
}
```

## 🚀 Current Status

### ✅ What Works
1. **Compilation**: Build successful with `./gradlew assembleDebug`
2. **Dependency Injection**: All components properly initialized
3. **Core Logic**: Scenario generation algorithms implemented
4. **Data Models**: Complete set of scenario-related data classes
5. **Templates**: ScenarioTemplates with Dutch content

### ⚠️ What's Missing
1. **UI Integration**: Scenarios not displayed in MatchReportCard
2. **ViewModel Integration**: Scenario generation not triggered in ViewModel
3. **Data Flow**: MatchReportGenerator not receiving scenario data
4. **Testing**: Unit tests need updating for new constructor

## 🔍 Debugging Guide

### How to Verify Scenario Generation

#### 1. Check Logs
```kotlin
// Add to ScenarioEngine.kt
android.util.Log.d("ScenarioEngine", "Generating scenarios for ${matchDetail.homeTeam} vs ${matchDetail.awayTeam}")
```

#### 2. Test Directly
```kotlin
// Create test function
suspend fun testScenarioGeneration() {
    val scenarios = scenarioEngine.generateScenarios(matchDetail, hybridPrediction)
    if (scenarios.isSuccess) {
        val scenarioList = scenarios.getOrThrow()
        android.util.Log.d("ScenarioTest", "Generated ${scenarioList.size} scenarios")
        scenarioList.forEach { scenario ->
            android.util.Log.d("ScenarioTest", "${scenario.title}: ${scenario.probability}%")
        }
    }
}
```

#### 3. Verify Data Flow
- Check if `MatchReportGenerator` receives `scenarioEngine` parameter
- Verify `MatchDetailViewModel` calls scenario generation
- Confirm UI components receive scenario data

## 📈 Next Steps

### Phase 2.1: UI Integration (HIGH PRIORITY)

#### 1. Update MatchReportGenerator
```kotlin
// Ensure scenarioEngine is properly passed
val matchReportGenerator = MatchReportGenerator(scenarioEngine = scenarioEngine)
```

#### 2. Update MatchDetailViewModel
```kotlin
// Add scenario generation to report creation
suspend fun generateMatchReport(): MatchReport {
    val hybridPrediction = getHybridPredictionUseCase(fixtureId)
    val matchDetail = getMatchDetails(fixtureId)
    
    // Generate scenarios
    val scenarios = scenarioEngine.generateScenarios(matchDetail, hybridPrediction)
    
    // Create report with scenarios
    return matchReportGenerator.generateReport(hybridPrediction, matchDetail)
}
```

#### 3. Update MatchReportCard UI
```kotlin
// Add scenario display section
@Composable
fun ScenarioSection(scenarios: List<MatchScenario>) {
    if (scenarios.isNotEmpty()) {
        Column {
            Text("📊 Scenario's", style = MaterialTheme.typography.h6)
            scenarios.forEach { scenario ->
                ScenarioCard(scenario)
            }
        }
    }
}
```

### Phase 2.2: Testing & Validation

#### 1. Update Unit Tests
```kotlin
class ScenarioEngineTest {
    @Test
    fun testScenarioGeneration() {
        // Update constructor calls
        val scenarioEngine = ScenarioEngine(
            mockMatchRepository,
            mockNewsImpactAnalyzer,
            mockExpectedGoalsService
        )
    }
}
```

#### 2. End-to-End Testing
- Test complete flow: MatchDetail → ScenarioEngine → UI
- Verify scenario data appears in MatchReportCard
- Test with different match types (future, live, past)

### Phase 2.3: Performance Optimization

#### 1. Caching
```kotlin
// Cache generated scenarios
private val scenarioCache = mutableMapOf<Int, List<MatchScenario>>()

suspend fun getCachedScenarios(fixtureId: Int): List<MatchScenario> {
    return scenarioCache[fixtureId] ?: generateScenarios(fixtureId).also {
        scenarioCache[fixtureId] = it
    }
}
```

#### 2. Background Processing
```kotlin
// Generate scenarios in background
viewModelScope.launch(Dispatchers.Default) {
    val scenarios = scenarioEngine.generateScenarios(matchDetail, hybridPrediction)
    withContext(Dispatchers.Main) {
        _scenarios.value = scenarios
    }
}
```

## 🎯 Success Criteria

### Technical Requirements
- [ ] Scenarios appear in MatchReportCard UI
- [ ] Scenario generation triggered from ViewModel
- [ ] All unit tests pass with updated constructors
- [ ] No memory leaks in scenario generation
- [ ] Proper error handling for failed scenario generation

### User Experience Requirements
- [ ] Scenarios load within 2 seconds
- [ ] Clear visual distinction between scenarios
- [ ] Probability percentages clearly displayed
- [ ] Score predictions visible and understandable
- [ ] Smooth integration with existing report UI

## 📚 Related Documentation

- `docs/07_scenario_predictions.md` - Original specification
- `docs/03_ux_ui_design.md` - Cyber-Minimalist design guidelines
- `docs/01_architecture.md` - Clean Architecture principles
- `docs/02_tech_stack.md` - Approved libraries and tools

## 🆘 Troubleshooting

### Common Issues

#### 1. Scenarios Not Appearing
- Check if `scenarioEngine` is properly injected
- Verify `MatchReportGenerator` receives the engine
- Check ViewModel logs for scenario generation

#### 2. Build Errors
- Ensure all constructor parameters are provided
- Check import statements for domain classes
- Verify Kotlin version compatibility

#### 3. Performance Issues
- Implement caching for repeated scenario generation
- Use background threads for heavy processing
- Limit scenario count to 3-5 per match

### Support Contacts
- **Technical Lead**: Senior Android Architect
- **UI/UX**: MatchMind Design Team
- **QA**: Testing & Validation Team

---

**Document Version**: 1.0  
**Next Review**: 28/12/2025  
**Approved By**: Technical Architecture Board

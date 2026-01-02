# UI Integration Steps - Scenario Predictions

**Status**: Phase 2.1 - UI Implementation Required  
**Last Updated**: 21/12/2025  
**Author**: Senior Android Architect

## 🎯 Objective

Complete the UI integration for scenario predictions in MatchMind AI. This document provides step-by-step instructions to make scenarios visible in the MatchReportCard.

## 📊 Current UI State Analysis

### What's Working
- ✅ MatchReportCard displays basic match information
- ✅ VerslagTab shows match reports
- ✅ MatchDetailViewModel fetches match data
- ✅ Hybrid predictions are generated

### What's Missing
- ❌ Scenarios not displayed in MatchReportCard
- ❌ Scenario data not passed to UI components
- ❌ ViewModel doesn't trigger scenario generation
- ❌ No scenario-specific UI components

## 🛠️ Step-by-Step Implementation Guide

### Step 1: Verify Current Implementation

#### 1.1 Check MatchReportGenerator Integration
```kotlin
// Open AppContainer.kt
// Verify MatchReportGenerator has scenarioEngine parameter
val matchReportGenerator: MatchReportGenerator by lazy {
    MatchReportGenerator(scenarioEngine = scenarioEngine)  // ✅ Should have this
}
```

#### 1.2 Check MatchReport Model
```kotlin
// Open domain/model/MatchReport.kt
// Verify it has scenarios field
data class MatchReport(
    // ... other fields
    val scenarios: List<MatchScenario>,  // ✅ Should have this
    val primaryScenario: MatchScenario?,
    val scorePredictions: ScorePredictionMatrix?
)
```

### Step 2: Update MatchDetailViewModel

#### 2.1 Add Scenario Generation to ViewModel
```kotlin
// Open presentation/viewmodel/MatchDetailViewModel.kt
// Add scenario generation to report creation

class MatchDetailViewModel(
    private val getMatchDetails: GetMatchDetailsUseCase,
    private val getHybridPrediction: GetHybridPredictionUseCase,
    private val matchReportGenerator: MatchReportGenerator  // ✅ Add this
) : ViewModel() {
    
    private val _matchReport = MutableStateFlow<Resource<MatchReport>>(Resource.Loading())
    val matchReport: StateFlow<Resource<MatchReport>> = _matchReport
    
    suspend fun loadMatchReport(fixtureId: Int) {
        _matchReport.value = Resource.Loading()
        
        try {
            // Get match details
            val matchDetail = getMatchDetails(fixtureId).getOrNull()
            if (matchDetail == null) {
                _matchReport.value = Resource.Error("Match details not found")
                return
            }
            
            // Get hybrid prediction
            val hybridPrediction = getHybridPrediction(fixtureId).getOrNull()
            if (hybridPrediction == null) {
                _matchReport.value = Resource.Error("Prediction not available")
                return
            }
            
            // Generate match report WITH scenarios
            val report = matchReportGenerator.generateReport(hybridPrediction, matchDetail)
            
            _matchReport.value = Resource.Success(report)
            
        } catch (e: Exception) {
            _matchReport.value = Resource.Error(e.message ?: "Unknown error")
        }
    }
}
```

#### 2.2 Update ViewModel Factory
```kotlin
// Open presentation/AppViewModelProvider.kt
// Update MatchDetailViewModel factory

object AppViewModelProvider {
    // ... existing code
    
    class MatchDetailFactory(
        private val fixtureId: Int
    ) : ViewModelProvider.Factory {
        
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val appContainer = MatchMindApplication.appContainer
            
            return MatchDetailViewModel(
                getMatchDetails = GetMatchDetailsUseCase(appContainer.matchRepository),
                getHybridPrediction = GetHybridPredictionUseCase(appContainer.matchRepository),
                matchReportGenerator = appContainer.matchReportGenerator  // ✅ Add this
            ) as T
        }
    }
}
```

### Step 3: Update MatchReportCard UI

#### 3.1 Create ScenarioCard Component
```kotlin
// Create new file: presentation/components/detail/ScenarioCard.kt

@Composable
fun ScenarioCard(
    scenario: MatchScenario,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title and Probability
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = scenario.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Probability Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            color = when {
                                scenario.probability >= 60 -> Color(0xFF4CAF50)
                                scenario.probability >= 40 -> Color(0xFF2196F3)
                                else -> Color(0xFFF44336)
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${scenario.probability}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Predicted Score
            Text(
                text = "Voorspelling: ${scenario.predictedScore}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Description
            Text(
                text = scenario.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Key Factors
            if (scenario.keyFactors.isNotEmpty()) {
                Text(
                    text = "Belangrijke factoren:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                scenario.keyFactors.forEach { factor ->
                    Text(
                        text = "• $factor",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            // Confidence Indicator
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { scenario.confidence / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = when {
                    scenario.confidence >= 70 -> Color(0xFF4CAF50)
                    scenario.confidence >= 40 -> Color(0xFF2196F3)
                    else -> Color(0xFFFF9800)
                },
                trackColor = MaterialTheme.colorScheme.surface
            )
            
            Text(
                text = "Vertrouwen: ${scenario.confidence}%",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.End),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

#### 3.2 Update MatchReportCard
```kotlin
// Open presentation/components/detail/MatchReportCard.kt
// Add scenario section

@Composable
fun MatchReportCard(
    matchReport: MatchReport,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Existing content...
            // ... title, introduction, situation analysis, etc.
            
            // ===== ADD THIS SECTION =====
            // Scenario Predictions Section
            if (matchReport.scenarios.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = stringResource(R.string.scenario_predictions),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Text(
                    text = stringResource(R.string.scenario_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Display scenarios
                matchReport.scenarios.forEach { scenario ->
                    ScenarioCard(scenario = scenario)
                }
                
                // Score Predictions Matrix
                matchReport.scorePredictions?.let { scoreMatrix ->
                    Spacer(modifier = Modifier.height(16.dp))
                    ScorePredictionSection(scoreMatrix)
                }
            }
            // ===== END OF NEW SECTION =====
            
            // Existing conclusion section...
        }
    }
}

@Composable
fun ScorePredictionSection(scoreMatrix: ScorePredictionMatrix) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.score_predictions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Most likely score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.most_likely_score),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${scoreMatrix.mostLikelyScore} (${scoreMatrix.mostLikelyProbability}%)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Outcome probabilities
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.home_win),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${scoreMatrix.homeWinProbability}%",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.draw),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${scoreMatrix.drawProbability}%",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.away_win),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${scoreMatrix.awayWinProbability}%",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Expected goals
            Text(
                text = stringResource(R.string.expected_goals),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = stringResource(
                    R.string.expected_goals_format,
                    String.format("%.1f", scoreMatrix.expectedHomeGoals),
                    String.format("%.1f", scoreMatrix.expectedAwayGoals)
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = stringResource(
                    R.string.goal_expectation_range,
                    scoreMatrix.goalExpectationRange
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

### Step 4: Add String Resources

#### 4.1 Update strings.xml
```xml
<!-- Open app/src/main/res/values/strings.xml -->
<!-- Add these strings -->

<!-- Scenario Predictions -->
<string name="scenario_predictions">📊 Scenario Voorspellingen</string>
<string name="scenario_intro">Mastermind AI analyseert verschillende mogelijke uitkomsten voor deze wedstrijd:</string>

<!-- Score Predictions -->
<string name="score_predictions">Score Voorspellingen</string>
<string name="most_likely_score">Meest waarschijnlijke score:</string>
<string name="home_win">Thuis Winst:</string>
<string name="draw">Gelijk:</string>
<string name="away_win">Uit Winst:</string>
<string name="expected_goals">Verwachte Goals</string>
<string name="expected_goals_format">Thuis: %1$s - Uit: %2$s</string>
<string name="goal_expectation_range">Verwacht totaal: %1$s</string>
```

### Step 5: Update VerslagTab

#### 5.1 Ensure VerslagTab Uses Updated ViewModel
```kotlin
// Open presentation/components/detail/VerslagTab.kt
// Verify it uses MatchDetailViewModel with scenario support

@Composable
fun VerslagTab(fixtureId: Int) {
    val viewModel: MatchDetailViewModel = viewModel(
        factory = AppViewModelProvider.MatchDetailFactory(fixtureId)
    )
    
    val matchReport by viewModel.matchReport.collectAsStateWithLifecycle()
    
    LaunchedEffect(fixtureId) {
        viewModel.loadMatchReport(fixtureId)
    }
    
    when (matchReport) {
        is Resource.Loading -> {
            // Show loading
        }
        is Resource.Success -> {
            val report = (matchReport as Resource.Success).data
            MatchReportCard(matchReport = report)
        }
        is Resource.Error -> {
            // Show error
        }
    }
}
```

## 🧪 Testing Steps

### Test 1: Verify Scenario Generation
```kotlin
// Add test function to verify scenarios are generated
@Test
fun testScenarioGenerationInViewModel() {
    runTest {
        val viewModel = MatchDetailViewModel(
            mockGetMatchDetails,
            mockGetHybridPrediction,
            mockMatchReportGenerator
        )
        
        viewModel.loadMatchReport(123)
        
        // Verify matchReport contains scenarios
        val report = viewModel.matchReport.value
        assertTrue(report is Resource.Success)
        val matchReport = (report as Resource.Success).data
        assertTrue(matchReport.scenarios.isNotEmpty())
    }
}
```

### Test 2: Verify UI Display
```kotlin
// UI test to verify scenarios appear
@Test
fun testScenarioDisplayInUI() {
    composeTestRule.setContent {
        MatchMindAppTheme {
            val testReport = createTestMatchReportWithScenarios()
            MatchReportCard(matchReport = testReport)
        }
    }
    
    // Verify scenario titles appear
    composeTestRule.onNodeWithText("Scenario 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("75%").assertIsDisplayed()
}
```

## 🐛 Common Issues & Solutions

### Issue 1: Scenarios Not Appearing
**Solution**: Check these points:
1. Verify `MatchReportGenerator` receives `scenarioEngine` in AppContainer
2. Check `MatchReport` model has `scenarios` field
3. Verify ViewModel calls `matchReportGenerator.generateReport()`
4. Check UI composable receives `matchReport.scenarios`

### Issue 2: Build Errors After Changes
**Solution**:
1. Clean project: `./gradlew clean`
2. Rebuild: `./gradlew assembleDebug`
3. Check import statements for new components
4. Verify string resources exist

### Issue 3: Performance Issues
**Solution**:
1. Add caching to scenario generation
2. Use `LaunchedEffect` with `Dispatchers.Default` for heavy processing
3. Limit scenario display to 3-5 items
4. Implement lazy loading for scenario details

## 📈 Success Verification

### Manual Testing Checklist
- [ ] Launch app and navigate to match detail
- [ ] Select "Verslag" tab
- [ ] Verify "Scenario Voorspellingen" section appears
- [ ] Verify 3-5 scenarios displayed with probabilities
- [ ] Verify score predictions section appears
- [ ] Verify all text is in Dutch
- [ ] Verify "Cyber-Minimalist" design is maintained

### Automated Testing Checklist
- [ ] Unit tests for ScenarioEngine pass
- [ ] UI tests for MatchReportCard pass
- [ ] Integration tests for complete flow pass
- [ ] Performance tests show <2s load time

## 🔄 Rollback Plan

If issues occur, revert these changes:

1. **Remove ScenarioCard.kt** if created
2. **Revert MatchReportCard.kt** to previous version
3. **Remove new string resources**
4. **Revert ViewModel changes** to use old constructor

## 📞 Support

For implementation issues:
- **Technical Lead**: Check dependency injection in AppContainer
- **UI/UX**: Verify design follows Cyber-Minimalist guidelines
- **Testing**: Run unit tests to identify breaking changes

---

**Document Version**: 1.0  
**Implementation Deadline**: 22/12/2025  
**Next Phase**: Phase 2.2 - Testing & Optimization

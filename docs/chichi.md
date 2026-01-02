# 🧠 Project ChiChi - Team Psychological Analysis

## 📋 Overview
Project ChiChi is a psychological analysis feature that visualizes team mental and physical factors in the Match Detail screen. It analyzes RSS news to generate quantitative distraction and fitness scores for both teams, providing deeper insights beyond traditional statistics.

## 🎯 Goals
1. **Psychological Insights:** Analyze team mental states (distraction, focus) from news
2. **Physical Analysis:** Assess team fitness levels from injury reports and news
3. **Visual Integration:** Seamlessly integrate with existing Kaptigun analysis
4. **Actionable Intelligence:** Provide clear, actionable insights for users

## 🏗️ Architecture

### Data Flow
```
RSS News → NewsImpactAnalyzer → SimulationContext → TeamAnalysisCard → AnalysisTab
```

### Components

#### 1. Domain Layer - SimulationContext Model
**File:** `domain/model/SimulationContext.kt`

**Purpose:** Model for team psychological and physical factors

**Key Properties:**
- `homeDistraction: Int` (0-100): How distracted is the home team?
- `awayDistraction: Int` (0-100): How distracted is the away team?
- `homeFitness: Int` (0-100): How fit is the home team?
- `awayFitness: Int` (0-100): How fit is the away team?

**Helper Functions:**
- `hasMeaningfulData()`: Checks if scores are not all zero
- `hasHighDistraction(team: String)`: Checks if team has high distraction
- `hasLowFitness(team: String)`: Checks if team has low fitness
- Extension functions for color coding and descriptions

#### 2. NewsImpactAnalyzer Enhancement
**File:** `domain/service/NewsImpactAnalyzer.kt`

**Purpose:** Generate SimulationContext from RSS news using AI analysis

**Key Functions:**
- `generateMatchScenario()`: Generates SimulationContext from RSS news
- `parseSimulationContext()`: Parses AI response to SimulationContext
- `extractDistractionFromReasoning()`: Extracts distraction scores from AI reasoning
- `extractFitnessFromReasoning()`: Extracts fitness scores from AI reasoning
- `createDefaultSimulationContext()`: Fallback for errors

**AI Prompt:** `SIMULATION_CONTEXT_SYSTEM_PROMPT`
- Instructs DeepSeek to analyze news for psychological and physical factors
- Requests distraction scores (0-100) and fitness scores (0-100)
- Requires JSON response format

#### 3. UI Components - TeamAnalysisCard
**File:** `presentation/components/detail/TeamAnalysisCard.kt`

**Purpose:** Main component for team psychological analysis visualization

**Features:**
- Header: "TEAM ANALYSE (CHICHI)" with subtitle
- TeamAnalysisRow for each team with emoji (🏠 Thuisteam, ✈️ Uitteam)
- ScoreProgressBar for Distraction Index (AFLEIDING) and Fitness Level (FITHEID)
- Color coding based on score severity
- Contextual descriptions for each score range
- AnalysisSummary with key insights based on score differences

**Helper Functions:**
- `getDistractionColor(score: Int)`: Returns color based on distraction score
- `getDistractionDescription(score: Int)`: Returns description for distraction score
- `getFitnessColor(score: Int)`: Returns color based on fitness score
- `getFitnessDescription(score: Int)`: Returns description for fitness score

#### 4. AnalysisTab Integration
**File:** `presentation/components/detail/AnalysisTab.kt`

**Purpose:** Integrate TeamAnalysisCard into existing Kaptigun analysis

**Integration Points:**
- State management for SimulationContext
- `LaunchedEffect` for automatic loading
- `loadSimulationContext()` function for data fetching
- Conditional rendering based on data availability
- Loading and error states

## 🎨 UI Design

### Color Coding System

#### Distraction Index (0-100)
- **0-20: EXCELLENT** (PrimaryNeon) - No distraction, full focus
- **21-40: Good** (ConfidenceHigh) - Minimal distraction
- **41-60: Average** (ConfidenceMedium) - Media attention
- **61-80: High** (ConfidenceLow) - Manager issues, scandals
- **81-100: Catastrophic** (Error) - Crisis, protests

#### Fitness Level (0-100)
- **0-20: Catastrophic** (Error) - Multiple key players injured
- **21-40: Very low** (ConfidenceLow) - Many injuries
- **41-60: Average** (ConfidenceMedium) - Normal injuries
- **61-80: Good** (ConfidenceHigh) - Few injuries, good condition
- **81-100: Excellent** (PrimaryNeon) - Full squad available

### Visual Elements
1. **Progress Bars:** Horizontal bars showing scores with color coding
2. **Team Rows:** Each team displayed with emoji and scores
3. **Descriptions:** Contextual text explaining what scores mean
4. **Key Insights:** Automatically generated insights based on score differences
5. **Loading States:** Circular progress indicator while fetching data
6. **Error States:** Error card with description on failure

## 🔧 Technical Implementation

### Score Generation Logic

#### Distraction Index Factors
1. **Manager Issues:** Manager under pressure, rumors of sacking
2. **Player Scandals:** Off-field issues, controversies
3. **Media Attention:** Excessive media scrutiny
4. **Fan Protests:** Fan unrest, protests
5. **Transfer Rumors:** Key players linked with moves away
6. **Contract Disputes:** Players in contract negotiations

#### Fitness Level Factors
1. **Injury Reports:** Number and importance of injured players
2. **Squad Depth:** Availability of backup players
3. **Fixture Congestion:** Recent match schedule
4. **Travel Fatigue:** Recent travel requirements
5. **Training Reports:** Training ground incidents
6. **Medical Updates:** Player recovery timelines

### Error Handling Strategy
1. **Network Errors:** Graceful degradation, shows error card
2. **AI Failures:** Uses default SimulationContext with neutral scores
3. **Missing Data:** Shows loading state, then error if timeout
4. **Parsing Errors:** Logs error, uses fallback values

### Performance Considerations
1. **Caching:** SimulationContext cached for 5 minutes
2. **Parallel Loading:** Loads alongside Kaptigun analysis
3. **Lazy Loading:** Only loads when AnalysisTab is visible
4. **Memory Management:** Proper state cleanup on screen exit

## 📊 Integration Points

### With Existing Features
1. **Kaptigun Analysis:** Shows below traditional statistical analysis
2. **NewsImpactAnalyzer:** Reuses existing RSS news infrastructure
3. **AppContainer:** Uses existing dependency injection
4. **MatchDetailScreen:** No changes needed - uses existing AnalysisTab

### Data Flow Integration
```
MatchDetailScreen → AnalysisTab → loadSimulationContext() → NewsImpactAnalyzer
                                                          → SimulationContext
                                                          → TeamAnalysisCard
```

## 🧪 Testing

### Unit Tests
1. **SimulationContext Tests:**
   - Score range validation (0-100)
   - Helper function correctness
   - Color coding accuracy

2. **NewsImpactAnalyzer Tests:**
   - AI prompt generation
   - Response parsing
   - Error handling

3. **TeamAnalysisCard Tests:**
   - UI rendering with various scores
   - Color coding correctness
   - Description accuracy

### Integration Tests
1. **End-to-End Flow:** RSS → AI → UI
2. **Error Scenarios:** Network failures, AI errors
3. **Performance:** Loading times, memory usage

### Manual Tests
1. **UI Responsiveness:** Different screen sizes
2. **Data Accuracy:** Compare scores with actual news
3. **User Experience:** Clarity of insights

## 📈 Benefits

### For Users
1. **Deeper Insights:** Psychological factors beyond traditional statistics
2. **Visual Clarity:** Color-coded progress bars for easy understanding
3. **Actionable Information:** Key insights highlight important differences
4. **Integrated Experience:** Seamlessly integrated with existing analysis

### For Developers
1. **Clean Architecture:** Follows existing patterns and separation of concerns
2. **Reusable Components:** TeamAnalysisCard can be used elsewhere
3. **Error Resilient:** Graceful degradation on failures
4. **Maintainable:** Well-documented with helper functions

## 🔮 Future Enhancements

### Phase 2: Advanced Psychological Factors
1. **Team Morale:** Based on recent results and performances
2. **Manager Pressure:** Manager job security metrics
3. **Fan Sentiment:** Social media analysis
4. **Derby Impact:** Rivalry intensity factors

### Phase 3: Predictive Analytics
1. **Impact on Performance:** Correlation with match outcomes
2. **Trend Analysis:** Historical psychological patterns
3. **Early Warning System:** Detect psychological issues early
4. **Recommendation Engine:** Suggest tactical adjustments

### Phase 4: Expanded Integration
1. **Mastermind Analysis:** Integrate with Mastermind scenarios
2. **Oracle Predictions:** Influence Oracle prediction confidence
3. **Smart Odds:** Adjust betting recommendations
4. **History Analysis:** Retrospective psychological analysis

## 📝 Implementation Notes

### Key Design Decisions
1. **Score Range (0-100):** Provides granularity while being intuitive
2. **Color Coding:** Uses existing theme colors for consistency
3. **Dutch Localization:** All UI texts in Dutch per project guidelines
4. **Cyber-Minimalist Design:** Follows project design principles

### Technical Constraints
1. **AI Dependency:** Requires DeepSeek API for analysis
2. **News Availability:** Dependent on RSS feed quality and timeliness
3. **Response Time:** AI analysis adds to loading time
4. **Error Rate:** AI may produce inconsistent results

### Success Metrics
1. **User Engagement:** Time spent on AnalysisTab
2. **Feature Usage:** Percentage of users viewing TeamAnalysisCard
3. **Accuracy:** Correlation between scores and actual match outcomes
4. **Performance:** Loading times under 3 seconds

## 🚀 Deployment

### Build Status
- ✅ Code Compilation: All files compile successfully
- ✅ Gradle Build: Build successful
- ✅ Architecture Compliance: Follows Clean Architecture
- ✅ Dutch Localization: All UI texts in Dutch
- ✅ Error Handling: Graceful degradation implemented

### Monitoring
1. **Error Rates:** Track AI and network failures
2. **Performance:** Monitor loading times
3. **Usage:** Track feature adoption
4. **Feedback:** Collect user feedback

### Rollout Strategy
1. **Beta Testing:** Limited user group for initial feedback
2. **A/B Testing:** Compare with/without ChiChi analysis
3. **Full Rollout:** Release to all users
4. **Iterative Improvement:** Continuous enhancement based on feedback

## 📚 References

### Related Documentation
1. **Fasetracker:** `docs/00_fasetracker.md` - Phase 12: Project ChiChi
2. **Project Log:** `docs/05_project_log.md` - Implementation details
3. **Architecture:** `docs/01_architecture.md` - Clean Architecture guidelines
4. **UI Design:** `docs/03_ux_ui_design.md` - Cyber-Minimalist design

### Source Code
1. **SimulationContext:** `domain/model/SimulationContext.kt`
2. **NewsImpactAnalyzer:** `domain/service/NewsImpactAnalyzer.kt`
3. **TeamAnalysisCard:** `presentation/components/detail/TeamAnalysisCard.kt`
4. **AnalysisTab:** `presentation/components/detail/AnalysisTab.kt`

---

**Last Updated:** 2025-12-24  
**Status:** ✅ Implemented  
**Version:** 1.0.0  
**Maintainer:** MatchMind AI Team

# DashX Implementation Plan - MatchMind AI Dashboard Refactoring

## Overzicht
DashX is een complete refactoring van het MatchMind AI dashboard om het te transformeren naar een state-of-the-art, hyper-gepersonaliseerde, interactieve en AI-gedreven ervaring. Dit document beschrijft de implementatie in drie fasen volgens de visie in `docs/files and info/dashx.md`.

## Huidige Status Analyse

### Bestaande Infrastructuur
1. **DashboardScreen.kt** - Functionele basis met curated feed
2. **DashboardViewModel.kt** - State management met date navigation
3. **MatchCuratorService.kt** - Match scoring en categorisatie
4. **CuratedFeed.kt** - Data model voor feed structuur
5. **FavoritesManager.kt** - Eenvoudige league favorieten systeem
6. **UserPreferences.kt** - Basis user preferences model
7. **NewsImpactAnalyzer.kt** - Geavanceerde AI analyse service
8. **MastermindEngine.kt** - AI beslissingsengine

### Gaps Identificatie
1. **Geen user profile systeem** - Geen onboarding of favoriete teams
2. **Geen uitgebreide personalisatie** - MatchCuratorService ondersteunt alleen league favorieten
3. **Geen interactieve features** - Geen live updates, expandable cards, video highlights
4. **Geen dashboard-specifieke AI insights** - AI services bestaan maar niet geïntegreerd in dashboard
5. **Geen finished matches sectie** - Dashboard toont alleen upcoming en live wedstrijden

## Fase 1: Hyper-Personalisatie (Het "Mijn" Dashboard)

### Doelstellingen
- User onboarding flow voor favoriete teams/competities
- Gepersonaliseerde "Must-Watch" algoritme
- Slimme feed filtering op basis van user preferences
- User profile data persistentie

### Technische Implementatie

#### 1.1 Nieuwe Data Modellen
```kotlin
// domain/model/UserProfile.kt
data class UserProfile(
    val userId: String = UUID.randomUUID().toString(),
    val favoriteTeamIds: List<Int> = emptyList(),
    val favoriteLeagueIds: List<Int> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
)

// domain/model/DashboardPreferences.kt
data class DashboardPreferences(
    val showOnlyFavorites: Boolean = false,
    val showFinishedMatches: Boolean = true,
    val autoRefreshLive: Boolean = true,
    val highlightFavoriteTeams: Boolean = true,
    val defaultView: DashboardView = DashboardView.CURATED
)

enum class DashboardView {
    CURATED, FAVORITES, ALL, LEAGUE_VIEW
}
```

#### 1.2 Nieuwe Repository Interfaces
```kotlin
// domain/repository/UserProfileRepository.kt
interface UserProfileRepository {
    suspend fun getUserProfile(): Result<UserProfile>
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit>
    suspend fun updateFavoriteTeams(teamIds: List<Int>): Result<Unit>
    suspend fun updateFavoriteLeagues(leagueIds: List<Int>): Result<Unit>
}

// domain/repository/DashboardPreferencesRepository.kt
interface DashboardPreferencesRepository {
    suspend fun getPreferences(): Result<DashboardPreferences>
    suspend fun savePreferences(prefs: DashboardPreferences): Result<Unit>
    suspend fun toggleShowOnlyFavorites(): Result<Unit>
    suspend fun toggleAutoRefreshLive(): Result<Unit>
}
```

#### 1.3 Nieuwe UI Componenten
1. **ProfileSetupScreen.kt** - Onboarding scherm voor favoriete teams/competities
2. **FavoriteTeamSelector.kt** - Component voor team selectie met search
3. **DashboardFilterBar.kt** - Filter bar voor "Toon Alles" vs "Mijn Favorieten"
4. **PersonalizedHeroCard.kt** - Hero card met "JOUW MUST-WATCH" label

#### 1.4 MatchCuratorService Uitbreiding
```kotlin
// domain/service/EnhancedMatchCuratorService.kt
class EnhancedMatchCuratorService(
    private val userProfileRepository: UserProfileRepository
) {
    fun curateMatchesWithPersonalization(
        fixtures: List<MatchFixture>,
        userProfile: UserProfile? = null
    ): PersonalizedCuratedFeed {
        // Bestaande scoring + personalization bonus
        val scoredMatches = fixtures.map { fixture ->
            val baseScore = calculateExcitementScore(fixture)
            val personalizationBonus = calculatePersonalizationBonus(fixture, userProfile)
            ScoredMatch(fixture, baseScore + personalizationBonus)
        }
        
        // ... rest van de logica
    }
    
    private fun calculatePersonalizationBonus(
        fixture: MatchFixture,
        userProfile: UserProfile?
    ): Int {
        if (userProfile == null) return 0
        
        var bonus = 0
        
        // Favorite team bonus (+2000)
        if (fixture.homeTeamId in userProfile.favoriteTeamIds || 
            fixture.awayTeamId in userProfile.favoriteTeamIds) {
            bonus += 2000
        }
        
        // Favorite league bonus (+500)
        if (fixture.leagueId in userProfile.favoriteLeagueIds) {
            bonus += 500
        }
        
        return bonus
    }
}
```

#### 1.5 Nieuwe CuratedFeed Uitbreiding
```kotlin
// domain/service/PersonalizedCuratedFeed.kt
data class PersonalizedCuratedFeed(
    val heroMatch: MatchFixture? = null,
    val liveMatches: List<MatchFixture> = emptyList(),
    val upcomingMatches: List<MatchFixture> = emptyList(),
    val finishedMatches: List<MatchFixture> = emptyList(),
    val favoriteMatches: List<MatchFixture> = emptyList(),
    val personalizedHeroReason: String? = null
) {
    // Helper methods voor filtering
    fun getFilteredFeed(showOnlyFavorites: Boolean): List<MatchFixture> {
        return if (showOnlyFavorites) {
            favoriteMatches
        } else {
            listOfNotNull(heroMatch) + liveMatches + upcomingMatches + finishedMatches
        }
    }
}
```

### Implementatie Volgorde
1. [ ] UserProfile en DashboardPreferences data modellen
2. [ ] UserProfileRepository en DashboardPreferencesRepository interfaces
3. [ ] Room entities en DAOs voor persistentie
4. [ ] Repository implementations met DataStore/Room
5. [ ] EnhancedMatchCuratorService met personalisatie
6. [ ] PersonalizedCuratedFeed data model
7. [ ] ProfileSetupScreen UI
8. [ ] DashboardFilterBar component
9. [ ] PersonalizedHeroCard component
10. [ ] DashboardViewModel uitbreiding met personalisatie
11. [ ] DashboardScreen uitbreiding met filters

## Fase 2: Interactieve & Immersive Ervaring

### Doelstellingen
- Live-updating match cards met WebSocket/polling
- Uitklapbare match cards met uitgebreide info
- Video highlights integratie
- Pull-to-refresh en swipe actions
- Micro-animaties en glasmorfische effecten

### Technische Implementatie

#### 2.1 Live Updates Infrastructuur
```kotlin
// domain/service/LiveMatchUpdateService.kt
class LiveMatchUpdateService(
    private val matchRepository: MatchRepository,
    private val webSocketClient: WebSocketClient
) {
    private val _liveUpdates = MutableSharedFlow<LiveMatchUpdate>()
    val liveUpdates: SharedFlow<LiveMatchUpdate> = _liveUpdates.asSharedFlow()
    
    suspend fun startLiveUpdates(fixtureIds: List<Int>) {
        // WebSocket verbinding voor real-time updates
        webSocketClient.connect()
        webSocketClient.subscribeToFixtures(fixtureIds)
        
        // Polling fallback voor belangrijke wedstrijden
        startPollingForImportantMatches(fixtureIds)
    }
    
    data class LiveMatchUpdate(
        val fixtureId: Int,
        val homeScore: Int,
        val awayScore: Int,
        val status: String,
        val events: List<MatchEvent>,
        val timestamp: Long
    )
}

// domain/model/MatchEvent.kt
data class MatchEvent(
    val type: EventType, // GOAL, YELLOW_CARD, RED_CARD, SUBSTITUTION
    val team: String,
    val player: String,
    val minute: Int,
    val description: String
)
```

#### 2.2 Uitklapbare Match Cards
```kotlin
// presentation/components/matches/ExpandableMatchCard.kt
@Composable
fun ExpandableMatchCard(
    match: MatchFixture,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    expandedContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onToggleExpanded,
        modifier = modifier
    ) {
        Column {
            // Ingeklapte content
            StandardMatchCardContent(match)
            
            // Uitgeklapte content met animatie
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                expandedContent()
            }
        }
    }
}

// presentation/components/matches/MiniPitchView.kt
@Composable
fun MiniPitchView(
    homeTeam: String,
    awayTeam: String,
    events: List<MatchEvent>,
    modifier: Modifier = Modifier
) {
    // Mini voetbalveld visualisatie met events
}
```

#### 2.3 Video Highlights Service
```kotlin
// data/remote/HighlightsService.kt
class HighlightsService(
    private val youtubeApi: YouTubeApi,
    private val cache: HighlightsCache
) {
    suspend fun getMatchHighlights(
        homeTeam: String,
        awayTeam: String,
        date: String
    ): Result<VideoHighlight> {
        // Zoek YouTube video's voor match highlights
        // Cache resultaten voor performance
    }
    
    data class VideoHighlight(
        val videoId: String,
        val title: String,
        val thumbnailUrl: String,
        val duration: String,
        val highlightMoments: List<HighlightMoment>
    )
}
```

#### 2.4 Swipe Actions en Pull-to-Refresh
```kotlin
// presentation/components/SwipeableMatchCard.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableMatchCard(
    match: MatchFixture,
    onSwipeRight: () -> Unit, // Add to favorites
    onSwipeLeft: () -> Unit,  // Hide match
    content: @Composable () -> Unit
) {
    var offset by remember { mutableFloatStateOf(0f) }
    
    Box(
        modifier = Modifier
            .swipeable(
                state = rememberSwipeableState(initialValue = 0),
                anchors = mapOf(
                    -100f to -1,
                    0f to 0,
                    100f to 1
                ),
                thresholds = { _, _ -> FractionalThreshold(0.5f) }
            )
    ) {
        // Background actions
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Swipe right background (favorite)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(80.dp)
                    .background(Color.Green.copy(alpha = 0.3f))
            ) {
                Icon(Icons.Default.Favorite, contentDescription = "Favorite")
            }
            
            // Swipe left background (hide)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(80.dp)
                    .background(Color.Red.copy(alpha = 0.3f))
            ) {
                Icon(Icons.Default.VisibilityOff, contentDescription = "Hide")
            }
        }
        
        // Draggable content
        content()
    }
}
```

### Implementatie Volgorde
1. [ ] LiveMatchUpdateService met WebSocket/polling
2. [ ] MatchEvent data model
3. [ ] ExpandableMatchCard component
4. [ ] MiniPitchView component
5. [ ] HighlightsService met YouTube API integratie
6. [ ] VideoHighlight data model
7. [ ] SwipeableMatchCard component
8. [ ] Pull-to-refresh in DashboardScreen
9. [ ] Micro-animaties voor score updates
10. [ ] Glassmorphic effects voor AI Tools cards

## Fase 3: AI-Gedreven Inzichten & Voorspellingen

### Doelstellingen
- Explainable AI voor hero match selectie
- Pre-match AI breakdown met winstkansen
- Live event impact analyse
- AI-gedreven betting tips in dashboard

### Technische Implementatie

#### 3.1 Explainable AI voor Hero Match
```kotlin
// domain/service/HeroMatchExplainer.kt
class HeroMatchExplainer(
    private val newsImpactAnalyzer: NewsImpactAnalyzer,
    private val mastermindEngine: MastermindEngine
) {
    suspend fun explainHeroMatchSelection(
        match: MatchFixture,
        curatedFeed: CuratedFeed
    ): HeroMatchExplanation {
        // Analyseer waarom deze match is geselecteerd als hero
        val analysis = newsImpactAnalyzer.analyzeNewsImpact(
            fixtureId = match.fixtureId ?: 0,
            matchDetail = mapToMatchDetail(match),
            basePrediction = getBasePrediction(match)
        )
        
        return HeroMatchExplanation(
            match = match,
            primaryReasons = extractPrimaryReasons(analysis),
            secondaryFactors = extractSecondaryFactors(match, curatedFeed),
            aiConfidence = analysis.confidence,
            bettingImplications = extractBettingImplications(analysis)
        )
    }
    
    data class HeroMatchExplanation(
        val match: MatchFixture,
        val primaryReasons: List<String>, // "Hoge historische rivaliteit", "Beide teams in topvorm"
        val secondaryFactors: List<String>,
        val aiConfidence: Double,
        val bettingImplications: String?
    )
}
```

#### 3.2 Pre-Match AI Breakdown
```kotlin
// presentation/components/dashboard/AiPredictionCard.kt
@Composable
fun AiPredictionCard(
    match: MatchFixture,
    aiAnalysis: AiAnalysisResult,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = SurfaceCard.copy(alpha = 0.8f)
        )
    ) {
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
                    text = "🧠 AI VOORSPELLING",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = PrimaryNeon
                )
                
                Text(
                    text = "${aiAnalysis.confidence.toInt()}% zeker",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMedium
                )
            }
            
            // Win probabilities
            ProbabilityBar(
                homeWin = aiAnalysis.homeWinProbability,
                draw = aiAnalysis.drawProbability,
                awayWin = aiAnalysis.awayWinProbability,
                homeTeam = match.homeTeam,
                awayTeam = match.awayTeam
            )
            
            // Predicted score
            Text(
                text = "Voorspelde score: ${aiAnalysis.predictedScore}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextHigh
            )
            
            // Key players to watch
            if (aiAnalysis.keyPlayers.isNotEmpty()) {
                Text(
                    text = "Spelers om in de gaten te houden:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMedium
                )
                aiAnalysis.keyPlayers.take(3).forEach { player ->
                    Text(
                        text = "• $player",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMedium
                    )
                }
            }
            
            // Quick summary
            Text(
                text = aiAnalysis.summary.take(100) + "...",
                style = MaterialTheme.typography.bodySmall,
                color = TextMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
```

#### 3.3 Live Event Impact Analyse
```kotlin
// domain/service/LiveEventAnalyzer.kt
class LiveEventAnalyzer(
    private val newsImpactAnalyzer: NewsImpactAnalyzer
) {
    suspend fun analyzeLiveEventImpact(
        fixtureId: Int,
        event: MatchEvent,
        currentPrediction: MatchPrediction
    ): LiveEventImpact {
        // Analyseer impact van live event op voorspelling
        val updatedAnalysis = newsImpactAnalyzer.analyzeLiveEvent(
            fixtureId = fixtureId,
            event = event,
            currentContext = getCurrentContext(fixtureId)
        )
        
        return LiveEventImpact(
            event = event,
            previousWinProbability = currentPrediction.homeWinProbability,
            updatedWinProbability = updatedAnalysis.homeWinProbability,
            probabilityChange = updatedAnalysis.homeWinProbability - currentPrediction.homeWinProbability,
            reasoning = updatedAnalysis.reasoning,
            recommendedBetAdjustment = calculateBetAdjustment(updatedAnalysis)
        )
    }
    
    data class LiveEventImpact(
        val event: MatchEvent,
        val previousWinProbability: Double,
        val updatedWinProbability: Double,
        val probabilityChange: Double,
        val reasoning: String,
        val recommendedBetAdjustment: String? // "Verhoog inzet", "Cash out", etc.
    )
}
```

#### 3.4 Dashboard AI Insights Integratie
```kotlin
// presentation/viewmodel/DashboardAiViewModel.kt
class DashboardAiViewModel(
    private val heroMatchExplainer: HeroMatchExplainer,
    private val

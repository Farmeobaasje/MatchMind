# 📋 Complete FavoX Hub Mapping

Ik heb de volledige FavoX hub in kaart gebracht. Hier is het complete overzicht van alle bestanden, componenten, wiring en functionaliteit:

## 📁 __PROJECTSTRUCTUUR__

### 1. __Domain Layer__ (Pure Kotlin - geen dependencies)

```javascript
domain/model/FavoriteTeamData.kt
├── Central data model voor favoriete teams
├── Properties:
│   ├── teamId: Int (required)
│   ├── teamName: String? (nullable)
│   ├── teamLogoUrl: String? (nullable)
│   ├── news: List<NewsItemData>? (nullable)
│   ├── nextMatch: MatchFixture? (nullable)
│   ├── standings: List<StandingRow>? (nullable)
│   ├── hasStandings: Boolean (flag voor friendlies)
│   ├── leagueId: Int? (nullable)
│   └── leagueName: String? (nullable)
├── Methods:
│   ├── getDisplayName() → String (fallback: "Team [ID]")
│   ├── hasNews() → Boolean
│   ├── getLimitedNews() → List<NewsItemData> (max 3)
│   ├── hasNextMatch() → Boolean
│   ├── hasValidStandings() → Boolean (checks hasStandings && !standings.isNullOrEmpty())
│   ├── withTeamName(name: String) → FavoriteTeamData (copy met naam)
│   ├── withTeamLogoUrl(logoUrl: String?) → FavoriteTeamData (copy met logo)
│   ├── withNews(newsList: List<NewsItemData>?) → FavoriteTeamData (copy met news)
│   ├── withNextMatch(match: MatchFixture?) → FavoriteTeamData (copy met match)
│   ├── withStandings(standingsList, leagueId, leagueName) → FavoriteTeamData (copy met stand)
│   └── asFriendliesLeague() → FavoriteTeamData (markeert als friendlies)
└── Extension Functions:
    ├── List<FavoriteTeamData>.toMapByTeamId() → Map<Int, FavoriteTeamData>
    ├── List<FavoriteTeamData>.getTeamIds() → List<Int>
    ├── List<FavoriteTeamData>.filterWithStandings() → List<FavoriteTeamData>
    ├── List<FavoriteTeamData>.filterWithNextMatches() → List<FavoriteTeamData>
    └── List<FavoriteTeamData>.filterWithNews() → List<FavoriteTeamData>
```

### 2. __Data Layer__

```javascript
domain/repository/MatchRepository.kt (Interface)
├── Favorites Hub Methods:
│   ├── getTeamNews(teamId: Int) → Result<List<NewsItemData>>
│   ├── getNextMatch(teamId: Int) → Result<MatchFixture?>
│   ├── getLeagueStandingsForTeam(teamId: Int) → Result<List<StandingRow>?>
│   ├── getFavoriteTeamsData(teamIds: List<Int>) → Result<List<FavoriteTeamData>>
│   ├── getTeamNameById(teamId: Int) → String?
│   ├── isFriendliesLeague(leagueId: Int) → Boolean
│   ├── clearTeamCache(teamId: Int) → Unit
│   ├── clearAllTeamCaches() → Unit
│   ├── searchTeams(apiKey: String, query: String) → List<TeamSelectionResult>
│   └── getApiSportsApiKey() → String
└── Implementation: data/repository/MatchRepositoryImpl.kt

data/cache/TeamCache.kt
├── In-memory cache voor team data
├── Opslag: Map<Int, String> (teamId → teamName)
└── Methods:
    ├── get(teamId: Int) → String?
    ├── put(teamId: Int, teamName: String) → Unit
    ├── remove(teamId: Int) → Unit
    └── clear() → Unit

data/local/FavoritesManager.kt
├── Beheert favoriete teams opslag
├── Gebruikt DataStore voor persistentie
└── Methods:
    ├── getFavoriteTeams() → Flow<Set<Int>>
    ├── addFavoriteTeam(teamId: Int) → Unit
    ├── removeFavoriteTeam(teamId: Int) → Unit
    └── clearAllFavorites() → Unit
```

### 3. __Presentation Layer__

#### A. __ViewModel__

```javascript
presentation/viewmodel/FavoritesViewModel.kt
├── Dependencies:
│   ├── MatchRepository
│   └── SettingsRepository
├── StateFlows:
│   ├── uiState: StateFlow<FavoritesUiState>
│   ├── teamNewsState: StateFlow<TeamNewsState>
│   ├── nextMatchState: StateFlow<NextMatchState>
│   ├── leagueStandingsState: StateFlow<LeagueStandingsState>
│   ├── favoriteMatches: Flow<List<MatchFixture>>
│   └── favoriteTeamIds: Flow<Set<Int>>
├── UI States:
│   ├── FavoritesUiState: Loading | Success(List<FavoriteTeamData>) | Error(String)
│   ├── TeamNewsState: Loading | NoTeamSelected | NoData | Success(List<NewsItemData>) | Error
│   ├── NextMatchState: Loading | NoTeamSelected | NoData | Success(MatchFixture) | Error
│   └── LeagueStandingsState: Loading | NoTeamSelected | NoData | Success(List<StandingRow>) | Error
├── Public Methods:
│   ├── loadFavoriteMatches() → Unit (paralleel laden van alle favoriete teams data)
│   ├── loadFavoriteTeamData() → Unit (centralized FavoriteTeamData aanpak)
│   ├── loadFavoriteTeamDataLegacy(favoriteTeamIds: Set<Int>) → Unit (fallback)
│   ├── toggleTeamFavorite(teamId: Int, teamName: String) → Unit (TODO)
│   └── refresh() → Unit (herlaadt alle data)
└── Key Features:
    ├── Parallel data loading voor meerdere favoriete teams
    ├── Centralized FavoriteTeamData aanpak (nieuwe methode)
    ├── Legacy fallback voor backward compatibility
    ├── Combine van settings preferences en cached fixtures
    └── Robuuste error handling met Result types
```

#### B. __UI Components__

```javascript
presentation/components/favorites/
├── FavoriteTeamHeader.kt
│   ├── Functie: Favoriet hub header met team info
│   ├── Composable: FavoriteTeamHeader(teamName, teamCount)
│   ├── Features:
│   │   ├── Team icon met gradient achtergrond
│   │   ├── Team naam en count display
│   │   ├── Stat items (Volgende Wedstrijd, Laatste Nieuws, Competitie Stand)
│   │   └── Fallback state: "Selecteer een team" als geen team geselecteerd
│   └── Helper: StatItem(title, value, color) → stats display
│
├── FavoriteTeamItem.kt
│   ├── Functie: Compleet favoriete team item met alle data
│   ├── Composable: FavoriteTeamItem(teamData, modifier, callbacks)
│   ├── Callbacks:
│   │   ├── onNewsItemClick: (NewsItemData) → Unit
│   │   ├── onNextMatchClick: (MatchFixture) → Unit
│   │   └── onStandingsClick: (List<StandingRow>) → Unit
│   ├── Sub-components:
│   │   ├── TeamHeader(teamData) → team logo + naam + league info
│   │   ├── TeamInitialsFallback(teamName) → fallback als geen logo
│   │   ├── SimpleNextMatchCard(match, onMatchClick) → volgende wedstrijd kaart
│   │   ├── SimpleTeamNewsFeed(newsItems, onNewsItemClick) → nieuws feed (max 3 items)
│   │   ├── SimpleNewsItem(newsItem, onClick) → individueel nieuws item
│   │   └── SimpleLeagueStandingsCard(standings, teamId, onStandingsClick) → stand kaart
│   ├── Helper Functions:
│   │   ├── formatSimpleMatchDate(match) → String
│   │   ├── formatSimpleMatchTime(match) → String
│   │   └── formatSimpleNewsDate(dateString) → String (relative dates)
│   └── Features:
│       ├── Team logo met AsyncImage van Coil
│       ├── Rounded corners (12dp)
│       ├── Empty state handling
│       ├── Material3 design (Card, icons, colors)
│       └── Previews voor testing
│
└── LeagueStandingsCard.kt
    ├── Functie: Competitie stand kaart met state management
    ├── Composable: LeagueStandingsCard(leagueStandingsState)
    ├── States Handled:
    │   ├── Loading → CircularProgressIndicator
    │   ├── NoTeamSelected → SportsSoccer icon + instructie
    │   ├── NoData → Info icon + bericht
    │   ├── Success(standings) → tabel met top 5 teams
    │   └── Error(message) → Warning icon + foutmelding
    ├── Sub-components:
    │   ├── LoadingContent() → loading indicator + tekst
    │   ├── NoTeamSelectedContent() → instructie om team te selecteren
    │   ├── NoDataContent() → geen stand beschikbaar bericht
    │   ├── StandingsContent(standings) → competitie stand tabel
    │   ├── StandingRowItem(team) → individuele rij in tabel
    │   ├── MatchPreviewItem(match) → match preview (niet gebruikt)
    │   └── ErrorContent(errorMessage) → error display
    └── Table Features:
        ├── # Positie kolom (24dp breedte)
        ├── Team naam kolom (flexibel gewicht)
        ├── Punten (P) kolom (24dp breedte)
        ├── Doelsaldo (GD) kolom (32dp breedte)
        ├── Groene kleur voor positief doelsaldo
        ├── Rode kleur voor negatief doelsaldo
        └── "+ X meer teams..." als meer dan 5 teams
```

#### C. __Screens__

```javascript
presentation/screens/
├── FavoritesScreen.kt (hoofd FavoX hub screen)
│   └── Gebruikt FavoritesViewModel + bovenstaande components
│
└── TeamSelectionScreen.kt
    └── Team selectie interface (nieuwe teams favoriet maken)
```

### 4. __Data Flow Diagram__

```javascript
User Action
    ↓
FavoritesViewModel
    ↓
SettingsRepository.getUserPreferences() → get selectedTeamIds
    ↓
MatchRepository.getFavoriteTeamsData(teamIds)
    ↓
Parallel Loading:
├── getTeamNews(teamId) → NewsItemData[]
├── getNextMatch(teamId) → MatchFixture?
└── getLeagueStandingsForTeam(teamId) → StandingRow[]?
    ↓
FavoriteTeamData (geaggregeerd object)
    ↓
ViewModel State Updates:
├── _uiState → FavoritesUiState.Success(List<FavoriteTeamData>)
├── _teamNewsState → TeamNewsState.Success(news)
├── _nextMatchState → NextMatchState.Success(match)
└── _leagueStandingsState → LeagueStandingsState.Success(standings)
    ↓
UI Components:
├── FavoriteTeamHeader (toont team count en info)
├── FavoriteTeamItem (toont volledige team data)
│   ├── SimpleNextMatchCard (volgende wedstrijd)
│   ├── SimpleTeamNewsFeed (nieuws items)
│   └── SimpleLeagueStandingsCard (stand)
└── LeagueStandingsCard (alternatieve stand kaart)
```

### 5. __Key Features & Functionaliteiten__

#### __Core Functionaliteiten:__

1. __Multi-team Support__ - Kan meerdere favoriete teams tegelijk weergeven
2. __Parallel Data Loading__ - Alle data (news, match, standings) parallel laden
3. __Centralized Data Model__ - FavoriteTeamData als single source of truth
4. __Friendlies Detection__ - Automatisch detecteren van competities zonder stand
5. __Team Caching__ - TeamCache voor snelle team naam lookups
6. __Null-safe Design__ - Alle nullable properties met fallbacks

#### __UI Features:__

1. __Cyber-Minimalist Design__ - Material3 met neon accents
2. __Empty States__ - Graceful fallbacks als geen data beschikbaar
3. __Loading States__ - Circular progress indicators
4. __Error States__ - Waarschuwings iconen met foutmeldingen
5. __Relative Dates__ - "Vandaag", "Gisteren", "X dagen geleden"
6. __Team Initials Fallback__ - Als geen logo URL beschikbaar
7. __Interactive Cards__ - Clickable items met navigatie

#### __Data Sources:__

1. __API-Sports API__ - Voor match data, fixtures, standings, news
2. __RSS Feeds__ - Voor nieuws items (via NewsRepository)
3. __Local Cache__ - Room database voor fixtures
4. __DataStore__ - Voor user preferences en favoriete teams
5. __In-Memory Cache__ - TeamCache voor team namen

### 6. __Dependency Injection__

```kotlin
// In AppContainer.kt
val favoritesViewModelFactory: FavoritesViewModel by lazy {
    factory {
        FavoritesViewModel(
            matchRepository = matchRepository,
            settingsRepository = settingsRepository
        )
    }
}
```

### 7. __Wire Summary__

```javascript
UI Layer
    ↓ observe StateFlow
FavoritesViewModel
    ↓ calls
Domain Layer (MatchRepository interface)
    ↓ implements
Data Layer (MatchRepositoryImpl)
    ↓ calls
    ├── Remote APIs (API-Sports, RSS)
    ├── Local Cache (TeamCache)
    └── Local Storage (DataStore FavoritesManager)
```

### 8. __Status & Open Issues__

✅ __Compleet geïmplementeerd:__

- Core data model (FavoriteTeamData)
- Repository methods voor alle data sources
- ViewModel met state management
- UI components (header, item, standings card)
- Multi-team support
- Parallel data loading
- Null-safe design

⚠️ __TODO/Open:__

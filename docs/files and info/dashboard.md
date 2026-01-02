# Dashboard Documentatie - MatchMind AI

## Overzicht
Het Dashboard is het hoofdscherm van de MatchMind AI applicatie. Het toont een "smart feed" van voetbalwedstrijden, georganiseerd in geprioriteerde categorieën voor optimale gebruikerservaring.

## Architectuur

### Data Flow
```
API Data → MatchRepository → MatchCuratorService → CuratedFeed → DashboardViewModel → DashboardScreen
```

### Belangrijke Componenten

#### 1. **DashboardScreen.kt** (`app/src/main/java/com/Lyno/matchmindai/presentation/screens/DashboardScreen.kt`)
- Hoofd UI component
- Toont de curated feed met verschillende secties
- Beheert UI states (loading, error, success, missing API key)

#### 2. **DashboardViewModel.kt** (`app/src/main/java/com/Lyno/matchmindai/presentation/viewmodel/DashboardViewModel.kt`)
- Beheert de UI state
- Coördineert data fetching via repositories
- Handelt gebruikersinteracties (datum navigatie, league expansion)

#### 3. **CuratedFeed.kt** (`app/src/main/java/com/Lyno/matchmindai/domain/service/CuratedFeed.kt`)
- Data class die de gestructureerde feed representeert
- Bevat:
  - `heroMatch: MatchFixture?` - Meest spannende wedstrijd van de dag
  - `liveMatches: List<MatchFixture>` - Wedstrijden die momenteel live zijn
  - `upcomingMatches: List<MatchFixture>` - Wedstrijden die nog moeten beginnen

#### 4. **MatchCuratorService.kt** (`app/src/main/java/com/Lyno/matchmindai/domain/service/MatchCuratorService.kt`)
- Pure Kotlin domain service
- Prioriteert en categoriseert wedstrijden
- Gebruikt een scoring algoritme voor excitement ranking

## UI Structuur

### Secties (van boven naar beneden)

#### 1. **Date Navigation Bar**
- Toont geselecteerde datum
- Knoppen voor vorige/volgende dag en "Vandaag"

#### 2. **Dashboard Header**
- App naam "MatchMind AI ⚽"
- Robot mascot icoon
- Subtitel "Smart feed voor echte fans"

#### 3. **Hero Match Section** (indien beschikbaar)
- Label: "🔥 MUST-WATCH"
- Toont de #1 geprioriteerde wedstrijd
- Gebruikt `HeroMatchCard` component

#### 4. **Live Matches Ticker** (indien beschikbaar)
- Label: "⚡ LIVE NU"
- Horizontale scrollbare lijst van live wedstrijden
- Toont aantal live wedstrijden
- Gebruikt `StandardMatchCard` component

#### 5. **Top Competities Section**
- Label: "📅 TOP COMPETITIES"
- Collapsible league sections via `ExpandableLeagueSection`
- Groepeert wedstrijden per league ID
- Top league IDs: 39 (PL), 88 (Eredivisie), 140 (La Liga), 78 (Bundesliga), 135 (Serie A), 61 (Ligue 1)

#### 6. **Komende Wedstrijden Section** (indien beschikbaar)
- Label: "📅 KOMENDE WEDSTRIJDEN"
- Wedstrijden van niet-top competities
- Gegroepeerd per league naam

#### 7. **AI Tools Section**
- Label: "🤖 AI TOOLS"
- Glassmorphic cards voor:
  - Chat Analist (naar chat scherm)
  - Instellingen (naar settings scherm)

### UI States

#### 1. **Loading State**
- Toont `CircularProgressIndicator`
- Tekst: "Smart feed laden..."

#### 2. **Success State**
- Toont de volledige curated feed
- Dynamische secties op basis van beschikbare data

#### 3. **Error State**
- Toont error icoon en bericht
- "Opnieuw proberen" knop

#### 4. **Missing API Key State**
- Toont settings icoon
- Bericht: "API Key vereist"
- "Naar Instellingen" knop

#### 5. **Empty State**
- Toont wanneer geen wedstrijden beschikbaar zijn
- "Naar Chat Analist" knop

## Match Categorisatie Logica

### Status Categorisatie
```kotlin
// Live statuses
val LIVE_STATUSES = setOf("1H", "2H", "HT", "ET", "PEN")

// Finished statuses (NIET GETOOND IN DASHBOARD - GAP IN FUNCTIONALITEIT)
val FINISHED_STATUSES = setOf("FT", "AET", "PEN")

// Problematic statuses
val PROBLEMATIC_STATUSES = setOf("PST", "SUSP", "INT", "CANC")
```

### Scoring Algoritme
Elke wedstrijd krijgt een excitement score:
1. **Basis score**: 0
2. **League bonus**: Gebaseerd op league ID
   - Premier League (39): +2000
   - La Liga (140): +1500
   - Eredivisie (88): +1500
   - KKD (89): +500
3. **Live bonus**: +100 voor live wedstrijden
4. **Sortering**: Wedstrijden gesorteerd op score (hoog naar laag)

### Categorisatie Flow
1. Alle wedstrijden scoren
2. Sorteren op score (descending)
3. Hero match = hoogste score
4. Overige wedstrijden categoriseren:
   - Live → `liveMatches`
   - Niet-live → `upcomingMatches`
   - **OPMERKING**: Finished wedstrijden worden momenteel NIET gecategoriseerd

## Data Modellen

### MatchFixture (`domain/model/MatchFixture.kt`)
```kotlin
data class MatchFixture(
    val fixtureId: Int?,
    val homeTeam: String,
    val awayTeam: String,
    val time: String?,
    val date: String?,
    val status: String?,  // "NS", "1H", "2H", "HT", "FT", etc.
    val league: String,
    val leagueId: Int?,
    val country: String?,
    // ... andere properties
)
```

### LeagueGroup (`domain/model/LeagueGroup.kt`)
```kotlin
data class LeagueGroup(
    val leagueId: Int,
    val leagueName: String,
    val country: String?,
    val matches: List<MatchFixture>,
    val isExpanded: Boolean = false
)
```

## Navigatie

### Bottom Navigation (van links naar rechts)
1. **Dashboard** (Home icoon) - Huidig scherm
2. **Favorieten** (Hart icoon) - Toekomstige functionaliteit
3. **Historie** (Lijst icoon) - Toont voorspellingsgeschiedenis
4. **Instellingen** (Instellingen icoon) - API key en app configuratie

### Vanuit Dashboard
- **Wedstrijd kaart klik** → `MatchDetailScreen`
- **Chat Analist knop** → `ChatScreen`
- **Instellingen knop** → `SettingsScreen`

### Andere Schermen (geen bottom navigation)
- **Wedstrijden** (`MatchesScreen`) - Volledige wedstrijdenlijst
- **Live Wedstrijden** (`LiveMatchesScreen`) - Live wedstrijden feed
- **Chat** (`ChatScreen`) - AI voetbalanalist
- **Match Detail** (`MatchDetailScreen`) - Gedetailleerde wedstrijdanalyse
- **Historie Detail** (`HistoryDetailScreen`) - Gedetailleerde voorspellingsgeschiedenis

### Naar Dashboard
- **Bottom Navigation Tab** (Home icoon)
- **Back navigatie** vanuit andere schermen

## Styling & Theming

### Kleuren (van `ui/theme/Color.kt`)
- `PrimaryNeon`: #00FF88 (cyber groen)
- `GradientStart`: #0A0A0A → `GradientEnd`: #1A1A1A (vertical gradient)
- `TextHigh`: #FFFFFF (hoog contrast tekst)
- `TextMedium`: #AAAAAA (secundaire tekst)
- `SurfaceCard`: #1E1E1E (kaart achtergrond)

### Typografie
- Headline: 32sp, bold
- Title: Medium/Large met bold
- Body: Regular weight
- Labels: Small/Medium

## Known Issues & Gaps

### 1. **Missing Finished Matches**
- **Probleem**: Dashboard toont alleen upcoming en live wedstrijden
- **Oorzaak**: `CuratedFeed` bevat geen `finishedMatches` property
- **Impact**: Gebruikers kunnen gespeelde wedstrijden niet zien in dashboard

### 2. **MatchCuratorService Categorisatie**
- **Probleem**: `isFinishedMatch()` methode bestaat maar wordt niet gebruikt in `curateMatches()`
- **Oplossing**: Finished wedstrijden moeten naar aparte lijst gaan i.p.v. `upcomingMatches`

### 3. **UI Sectie voor Finished Matches**
- **Probleem**: Geen UI sectie voor gespeelde wedstrijden
- **Suggestie**: Toevoegen "✅ GESPEELDE WEDSTRIJDEN" sectie

## Technische Details

### Dependencies
```kotlin
// ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

// Compose
implementation("androidx.compose.ui:ui:1.6.0")
implementation("androidx.compose.material3:material3:1.2.0")

// Navigation
implementation("androidx.navigation:navigation-compose:2.7.5")
```

### Performance Overwegingen
1. **Lazy Loading**: `LazyColumn` en `LazyRow` voor efficiënte scrolling
2. **State Management**: `collectAsState()` voor reactive updates
3. **Image Loading**: Coil/Glide voor efficiënte afbeeldingen (indien gebruikt)

## Test Coverage

### Unit Tests
- `MatchCuratorServiceTest`: Test scoring en categorisatie logica
- `DashboardViewModelTest`: Test state management en data flow

### UI Tests
- `DashboardScreenTest`: Test UI rendering en interacties
- `ErrorStateTest`: Test error handling UI

## Toekomstige Verbeteringen

### Gepland (Phase 4+)
1. **Favorite Teams**: Personalisatie op basis van favoriete teams
2. **Advanced Filtering**: Filter op league, team, tijd
3. **Push Notifications**: Live score updates
4. **Offline Mode**: Cached data voor offline toegang
5. **Analytics**: Gebruiksstatistieken en engagement metrics

### UI/UX Verbeteringen
1. **Pull-to-Refresh**: Handmatig vernieuwen van feed
2. **Swipe Actions**: Snel acties op match cards
3. **Dark/Light Mode**: Theming ondersteuning
4. **Accessibility**: Verbeterde screen reader ondersteuning

---

**Laatst bijgewerkt**: 25 december 2025  
**Documentatie Status**: Compleet voor huidige implementatie  
**Gap Identificatie**: Finished matches functionaliteit ontbreekt

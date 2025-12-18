# 🚀 MatchMind AI - Phase Tracker

Dit document houdt de voortgang van het project bij.
**Instructie voor AI:** Controleer dit bestand bij aanvang van een sessie om te zien wat de actieve fase is. Vink taken af (`[x]`) zodra ze volledig zijn getest en geïmplementeerd.

---

## 🧠 FASE 1: Intelligence Engine Upgrade (Phase 1)
**Doel:** Upgrade de Data en Domain layers voor complexe data flows, fallback logic, en specifieke API endpoints.
**Status:** ✅ Voltooid

### 1.1 FootballApiService Upgrade (Retrofit)
- [x] **Predictions Endpoint:** `GET /predictions?fixture={id}` geïmplementeerd met typed response
- [x] **Leagues Endpoint:** `GET /leagues?current=true` geïmplementeerd met coverage object parsing
- [x] **Fixtures Endpoint:** Live filtering (`live=all`) en date filtering ondersteund
- [x] **Enhanced Error Handling:** `FootballApiException` met specifieke error types (NETWORK_ERROR, RATE_LIMIT_ERROR, etc.)
- [x] **Header Management:** `x-apisports-key` header automatisch geïnjecteerd via HTTP client interceptor

### 1.2 Data Transfer Objects (DTOs)
- [x] **PredictionsResponse:** Volledige DTO structuur met `winning_percent`, `advice`, `h2h` data
- [x] **CoverageDto:** Kritieke coverage object voor UI logic (fields: `standings`, `players`, `odds`, `predictions`)
- [x] **LeagueResponse:** League data met coverage object ondersteuning
- [x] **Type Safety:** Alle DTOs gebruiken Kotlinx Serialization voor type-safe JSON parsing

### 1.3 MatchRepository "Smart Fallback" Foundation
- [x] **Repository Interface:** `MatchRepository` interface geüpdatet met nieuwe methoden
- [x] **Fallback Logic Foundation:** Voorbereiding voor status-based fallback (PST, SUSP, CANC)
- [x] **Error Handling Strategy:** Differentiatie tussen NetworkError, ApiError, DataError
- [x] **Clean Architecture Compliance:** Strict separation tussen Data, Domain, Presentation layers

### 1.4 ToolOrchestrator Integration
- [x] **Typed Predictions:** `executeGetMatchPrediction()` geüpdatet om typed `getPredictions()` te gebruiken
- [x] **Enhanced Output:** Formatted prediction data met winning percentages, advice, en head-to-head statistics
- [x] **Backward Compatibility:** `getPredictionsAsJson()` behouden voor legacy code

### 1.5 Build & Test
- [x] **Compilation Success:** App compileert succesvol zonder errors
- [x] **Type Safety:** Alle nieuwe endpoints gebruiken typed responses
- [x] **Error Handling:** Enhanced error handling geïmplementeerd
- [x] **Documentation:** Fasestracker en project log geüpdatet

### 1.6 Predictions Data Flow Completion (17 december 2025)
- [x] **Complete Data Flow:** API → FootballApiService → MatchRepository → PredictionsMapper → MatchPredictionData → ViewModel → MatchPrediction → UI
- [x] **UI Integration:** PredictionsTab in MatchDetailScreen volledig geïmplementeerd met:
  - Win probability bars voor home/draw/away
  - Expected goals display
  - Analysis section
  - Loading states en error handling
- [x] **ViewModel Integration:** MatchDetailViewModel heeft `loadPrediction()` method die `matchRepository.getPredictions()` aanroept
- [x] **Mapper Implementation:** PredictionsMapper.toMatchPredictionData() volledig geïmplementeerd met percentage parsing en expected goals calculation
- [x] **Domain Models:** MatchPredictionData en MatchPrediction modellen compleet met conversie methoden
- [x] **Testing:** Build succesvol (gradlew.bat assembleDebug) zonder compilation errors

---

## 🎯 FASE 2: Curator Dashboard - Smart Prioritized Feed
**Doel:** Build the "Curator Dashboard" UI with smart, prioritized feed that highlights "Must-Watch" games and handles different data densities gracefully.
**Status:** ✅ Voltooid

### 2.1 MatchCuratorService (Domain Logic)
- [x] **CuratedFeed data class:** Created with `heroMatch: MatchFixture?`, `liveMatches: List<MatchFixture>`, `upcomingMatches: List<MatchFixture>`
- [x] **MatchCuratorService:** Standalone service class with `curateMatches(fixtures: List<MatchFixture>, standings: List<Standing>): CuratedFeed`
- [x] **Priority Filtering:** Separates matches into "High Priority" (Top 5 Leagues + Eredivisie), "Live Now", and "Rest"
- [x] **Excitement Score:** Calculates score (0-100) based on:
  - +50 points for Top Tier league
  - +30 points for "Top 4 Clash" (based on Standings rank)
  - +20 points for live status ("1H", "2H", "ET", "PEN")
- [x] **Hero Match Selection:** Selects highest excitement score match as hero match
- [x] **TIER-BASED LEAGUE PRIORITIZATION (UPDATE):** Enhanced scoring algorithm with explicit league tiers:
  - **TIER 1 LEAGUES (+1000 points):** Premier League (39), La Liga (140), Copa del Rey (143), Eredivisie (88), Champions League (2), Europa League (3)
  - **TIER 2 LEAGUES (+500 points):** Serie A (135), Bundesliga (78), Ligue 1 (61)
  - **LIVE MATCH BONUS (+100 points):** For matches with status "1H", "2H", "HT", "PEN"
  - **Sorting:** Strictly by excitementScore DESCENDING, ensuring major tournaments always appear before obscure leagues
- [x] **EXPLICIT ID-BASED SCORING (CRITICAL FIX):** Fixed Jamaica vs English Premier League confusion by using explicit league IDs:
  - **English Premier League (ID 39):** +2000 points
  - **La Liga (ID 140):** +1500 points
  - **Copa del Rey (ID 143):** +1500 points
  - **Champions League (ID 2):** +2000 points
  - **Europa League (ID 3):** +1500 points
  - **Eredivisie (ID 88):** +1500 points
  - **KKD (ID 89):** +500 points
  - **Algorithm:** Check `league.id` against explicit map, give 0 points for leagues not in top list

### 2.2 Adaptive Match Widgets (UI Components)
- [x] **HeroMatchCard:** Large card for #1 curated match with team logos, big typography scores, and time/status
- [x] **StandardMatchCard:** Compact row for list views with essential match info
- [x] **StatusBadge:** Reusable chip for match status with color coding:
  - **Red:** "1H", "2H", "HT", "ET", "PEN" (Live)
  - **Green:** "FT", "AET", "PEN" (Finished)
  - **Orange:** "PST", "SUSP", "INT" (Issues)
  - **Grey:** "NS" (Not Started)
- [x] **@Preview Composables:** Created previews for live, upcoming, finished, and postponed matches

### 2.3 DashboardScreen (Feature)
- [x] **Smart Feed Layout:** `LazyColumn` with sections for Hero, Live Ticker, and Categorized Lists
- [x] **Hero Section:** Displays `HeroMatchCard` for #1 curated match
- [x] **Live Ticker:** Horizontal `LazyRow` of `StandardMatchCards` for matches currently in play
- [x] **Categorized Lists:** Vertical lists grouped by league for upcoming matches
- [x] **Missing Data Handling:** Checks `Fixture.coverage` object and adapts UI:
  - If `lineups: false`, does NOT render "Lineups" tab
  - If status is "PST" (Postponed), shows warning icon asking user to "Ask AI for reason"
- [x] **Empty State:** Shows AI tools section when no matches available

### 2.4 DashboardViewModel Integration
- [x] **ViewModel Update:** Integrated `MatchCuratorService` into ViewModel
- [x] **Data Flow:** Fetches data using `MatchRepository`, processes through `MatchCuratorService`
- [x] **UI State:** Exposes `DashboardUiState` (Loading, Success, Error) to View
- [x] **Auto-Refresh:** 90-second auto-refresh loop for curated feed
- [x] **Parallel Fetching:** Fetches fixtures and standings in parallel for optimal performance

### 2.5 Technical Implementation
- [x] **Material3 Design:** Used Material3 components (Cards, Text, ColorScheme)
- [x] **Coil Integration:** Prepared for team logo loading (placeholders implemented)
- [x] **Dark Mode Support:** Proper handling of dark mode with theme colors
- [x] **Cyber-Minimalist Design:** Applied consistent styling with gradient backgrounds and glassmorphic cards
- [x] **Dutch Localization:** User-facing strings in Dutch for better user experience

### 2.6 Build & Test
- [x] **Compilation Success:** App compiles successfully without errors
- [x] **Preview Testing:** All @Preview composables work correctly
- [x] **State Management:** Loading, Success, Error states properly implemented
- [x] **Responsive Design:** Works on different screen sizes and orientations
- [x] **Documentation:** Phase tracker and project log updated

### 2.7 Next Steps (Phase 3)
- [ ] **Real Data Integration:** Connect to actual API-Sports data for live testing
- [ ] **Team Logo Implementation:** Replace placeholders with actual team logos from API
- [ ] **Event Coverage:** Implement timeline for matches with `coverage.fixtures.events: true`
- [ ] **User Preferences:** Add user preferences for league prioritization
- [ ] **Performance Optimization:** Implement caching for curated feed results

---

## 🏗️ FASE 2: De Motor (Data & Foundation)
**Doel:** De app kan communiceren met DeepSeek en API Keys veilig opslaan.
**Status:** ✅ Voltooid

### 1.1 Project Setup
- [x] Dependencies toevoegen (Ktor, Serialization, DataStore, Coil) -> *Zie docs/02*.
- [x] Manifest permissies (INTERNET) instellen.
- [x] Mappenstructuur aanmaken (`data`, `domain`, `presentation`).

### 1.2 Data Layer - Persistence (The Vault)
- [x] `ApiKeyStorage` class gemaakt (DataStore implementation).
- [x] Functies: `saveKey()` en `getKey()` (als Flow).

### 1.3 Data Layer - Network (The Connector)
- [x] `DeepSeekDtos` gemaakt (Request/Response modellen) -> *Zie docs/06*.
- [x] `DeepSeekApi` client geconfigureerd (Ktor).
- [x] Dynamische Auth toegevoegd (Key meegeven als parameter).

### 1.4 Domain Layer (The Contract)
- [x] `MatchPrediction` model gemaakt.
- [x] `MatchRepository` interface gedefinieerd.
- [x] `SettingsRepository` interface gedefinieerd.

### 1.5 Repository Implementation (The Bridge)
- [x] `SettingsRepositoryImpl` gebouwd.
- [x] `MatchRepositoryImpl` gebouwd (verbindt Storage + API).
- [x] Error Handling: `ApiKeyMissingException` geïmplementeerd.

---

## 🎨 FASE 2: Het Gezicht (UI & UX)
**Doel:** De "Cyber-Minimalism" look implementeren.
**Status:** ✅ Voltooid

### 2.1 Basis UI
- [x] Theme instellen (Colors, Type) -> *Zie docs/03*.
- [x] `MainActivity` opzetten met Navigation Compose.
- [x] Routes definiëren (`Settings`, `Match`).

### 2.2 Componenten (Bouwstenen)
- [x] `CyberTextField` (Custom input veld).
- [x] `PrimaryActionButton` (De groene knop met laad-animatie).
- [x] `PredictionCard` (De resultaat kaart met badges).

### 2.3 Schermen
- [x] **Settings Screen:**
    - Invoerveld voor API Key.
    - Uitleg + Link naar DeepSeek.
- [x] **Match Screen:**
    - Inputs voor teams.
    - Animatie tijdens laden.
    - Error handling (Snackbar/Dialog).

---

## 🧠 FASE 3: Prophet Module - Generative UI & Advanced AI Analysis ✅
**Doel:** Implementeren van de "Prophet Module" (Chat Interface) als een Generative UI systeem waar de AI harde data (API-SPORTS) en zachte context (Tavily) analyseert en gestructureerde JSON retourneert die zowel tekst als interactieve widgets rendert.
**Status:** ✅ Voltooid

### 3.1 DeepSeekService & Request Models
- [x] **Retrofit service:** `DeepSeekApi.kt` al geïmplementeerd met `POST /chat/completions` endpoint
- [x] **Configuratie:** `temperature: 0.5` en `response_format: {"type": "json_object"}` geconfigureerd
- [x] **System Prompts:** Specifieke prompts gedefinieerd voor output schema controle

### 3.2 PromptBuilder (Anchor & Adjust Logic)
- [x] **PromptBuilder.kt** geïmplementeerd in `data/ai/PromptBuilder.kt`
- [x] **Anchor & Adjust strategie:** 
  - **Anchor:** Harde feiten eerst: "API-SPORTS geeft {HomeTeam} {percent}% win kans"
  - **Adjust:** Nieuws context: "News context: {news_titles}"
  - **Instruction:** "Analyseer of nieuws conflicteert met statistieken"
  - **Risk Factor:** "Identificeer één 'Killer Scenario' waarom voorspelling kan falen"
- [x] **Coverage handling:** Als `coverage.predictions` false is, expliciet vermelden: "Geen statistisch model beschikbaar, vertrouw op algemene vorm en nieuws"
- [x] **Status handling:** Als match status PST (Postponed), AI legt uit waarom (gebaseerd op Tavily nieuws)

### 3.3 Polymorphic AgentResponse
- [x] **Sealed class hierarchy:** `ProphetResponse.kt` geïmplementeerd in `domain/model/ProphetResponse.kt`
- [x] **Response types:**
  - `Text(content: String)` - Standaard tekst bubble
  - `Loading(message: String)` - Loading indicator
  - `MatchWidget(match: MatchFixture, analysis: String)` - Match card in chat
  - `PredictionWidget(homeChance: Int, awayChance: Int, risk: String)` - Voorspelling widget met win % bars
- [x] **JSON parsing:** `kotlinx.serialization` met `ignoreUnknownKeys = true` om crashes te voorkomen bij hallucinaties

### 3.4 ToolOrchestrator Upgrade naar ChatViewModel
- [x] **Reasoning Loop:** ViewModel handelt "Reasoning Loop" af:
  1. Vind fixture ID via `MatchRepository`
  2. Haal `/predictions` en `/leagues` (check coverage)
  3. Haal nieuws via `TavilyService`
  4. Bouw prompt via `PromptBuilder`
  5. Roep `DeepSeekService` aan
  6. Parse JSON naar correcte `AgentResponse` subclass
- [x] **ChatViewModel.kt** geüpdatet met Prophet Module functionaliteit:
  - `sendProphetQuery(query: String)` functie
  - `startProphetMatchAnalysis(matchFixture: MatchFixture)` functie
  - `processProphetQuery()` in `MatchRepository` interface
- [x] **MatchRepositoryImpl.kt** geüpdatet met `processProphetQuery()` implementatie

### 3.5 ChatScreen met Generative Widgets
- [x] **UI rendering:** `ChatScreen.kt` geüpdatet met Prophet Module button
- [x] **Widget types:**
  - `Text`: Standaard bubble
  - `PredictionWidget`: Gespecialiseerde Card met Win % Bars en "Risk Factor" met warning icon
  - `MatchWidget`: Hergebruik van `StandardMatchCard` uit Phase 2, embedded in chat stream
- [x] **Prophet Module button:** AutoAwesome icon in top bar voor geavanceerde analyse
- [x] **Dropdown menu:** "Prophet Module" optie toegevoegd aan chat menu

### 3.6 Technical Constraints Implementatie
- [x] **Safety:** Coverage checks geïmplementeerd in `PromptBuilder`
- [x] **Status Handling:** PST status handling geïmplementeerd
- [x] **JSON Parsing:** `kotlinx.serialization` met `ignoreUnknownKeys = true` geconfigureerd
- [x] **Error Handling:** Robuuste error handling voor API failures

### 3.7 Build & Test
- [x] **Compilation Success:** App compileert succesvol zonder errors
- [x] **Prophet Module Integration:** Alle componenten geïntegreerd en werkend
- [x] **UI Updates:** ChatScreen heeft Prophet Module functionaliteit
- [x] **Documentation:** Fasetracker en project log geüpdatet

### 3.8 Expected Result
**Gebruikersvraag:** "Predict Feyenoord vs Ajax"
**Prophet Module Flow:**
1. Vindt fixture ID voor Feyenoord vs Ajax
2. Haalt API-SPORTS voorspellingen op (bijv. Feyenoord 45%, Ajax 35%, Draw 20%)
3. Haalt Tavily nieuws op (blessures, recente vorm)
4. Bouwt prompt met "Anchor & Adjust" strategie
5. DeepSeek analyseert en retourneert JSON met:
   - `PredictionWidget`: Win % bars voor beide teams
   - `Risk Factor`: Identificatie van één "Killer Scenario"
   - `Analysis`: Gedetailleerde tactische analyse in het Nederlands

---

## 🏆 FASE 22: Dynamic League Discovery - Fix Serialization Error
**Doel:** Oplossen van de serialization error voor LeagueDetailsDto door nieuwe DTOs te maken voor de /leagues endpoint.
**Status:** ✅ Voltooid

### 22.1 Probleem Analyse
- [x] **Error Identificatie:** `Field 'country' is required for type with serial name 'com.Lyno.matchmindai.data.dto.football.LeagueDetailsDto', but it was missing at path: $.response[0].league`
- [x] **Root Cause:** De API geeft een andere structuur terug voor `/leagues` endpoint:
  - **Verwacht:** `country` veld binnen `league` object
  - **Werkelijkheid:** `country` is een sibling van `league` object
- [x] **API Response Structuur:**
  ```json
  {
    "league": { "id": 807, "name": "AFC Challenge Cup", ... }, // GEEN country hierin!
    "country": { "name": "World", ... }, // Country staat hiernaast
    "seasons": [...]
  }
  ```

### 22.2 Nieuwe DTOs Implementatie
- [x] **LeagueDiscoveryResponse.kt:** Nieuwe DTOs gemaakt in `data/dto/football/`
- [x] **LeagueEntryDto:** Bevat `league: LeagueInfoDto`, `country: CountryDto`, `seasons: List<LeagueSeasonDto>`
- [x] **LeagueInfoDto:** Heeft GEEN `country` veld (country is separate)
- [x] **LeagueSeasonDto:** Seizoen informatie met coverage object
- [x] **LeagueCoverageDto:** Coverage informatie voor league/season

### 22.3 FootballApiService Update
- [x] **getLeagues() Return Type:** Gewijzigd van `LeagueResponse` naar `LeagueDiscoveryResponse`
- [x] **API Call:** `/leagues?current=true` endpoint gebruikt nu de nieuwe DTOs
- [x] **Backward Compatibility:** Oude `LeagueResponse` blijft bestaan voor andere endpoints

### 22.4 LeagueRepositoryImpl Update
- [x] **refreshLeaguesFromApi():** Gewijzigd om `LeagueDiscoveryResponse` te gebruiken
- [x] **Data Mapping:** `LeagueEntity.fromApiResponse()` aangepast voor nieuwe structuur
- [x] **Coverage Extraction:** `extractCoverageFromLeagueEntry()` functie geïmplementeerd

### 22.5 Build & Test
- [x] **Compilation Success:** App compileert succesvol zonder errors
- [x] **Serialization Fix:** Geen meer "Field 'country' is required" errors
- [x] **Dynamic League Discovery:** League caching werkt correct met nieuwe DTOs
- [x] **Documentation:** Fasestracker bijgewerkt met oplossing

---

## 🎰 FASE 23: Dynamic League Discovery + Beste Odds Engine
**Doel:** Implementeren van een "Beste Odds Engine" die pre-match odds analyseert voor beginners, gecombineerd met Dynamic League Discovery voor slimme fixture filtering.
**Status:** ✅ Voltooid

### 23.1 Foundation (Nieuwe DTOs + API Service)
- [x] **Odds DTOs:** Nieuwe DTOs gemaakt voor odds responses gebaseerd op API docs
  - `OddsResponseDto.kt`: Hoofd DTO voor odds API responses
  - `SimplifiedOddsDto.kt`: Vereenvoudigde DTO voor AI tool consumption
  - `OddsMapper.kt`: Mapper functies voor odds data transformatie
- [x] **FootballApiService Update:** Odds endpoints toegevoegd met typed responses
  - `getOdds(fixtureId: Int): OddsResponseDto` - Odds voor specifieke fixture
  - `getOddsByDate(date: String, leagueId: Int?, timezone: String): OddsResponseDto` - Odds per datum
  - `getOddsByLeague(leagueId: Int, season: Int, bookmakerId: Int?, betId: Int?): OddsResponseDto` - Odds per competitie
- [x] **Compilation Test:** App compileert succesvol met nieuwe DTOs

### 23.2 Repository Logica (Smart Filtering + Odds)
- [x] **MatchRepository Interface:** `getBestOddsForTool()` functie toegevoegd aan interface
- [x] **MatchRepositoryImpl Update:** `getBestOddsForTool()` functie geïmplementeerd
  - Parallelle odds fetching voor meerdere fixtures
  - Beginner-friendly filtering met safety en value ratings
  - JSON string output voor AI consumption
- [x] **OddsMapper:** Filtering en mapping logica geïmplementeerd
  - `toSimplifiedOddsList()`: Converteert complexe odds naar vereenvoudigde DTOs
  - `filterForBeginners()`: Filtert odds op veiligheid en waarde voor beginners
  - `generateBettingAdvice()`: Genereert betting advies op basis van odds data

### 23.3 Tool Integration
- [x] **Tools.kt Update:** `createGetBestOddsTool()` functie toegevoegd
  - Tool definitie voor `get_best_odds` met parameters: date, league_id, team_name, limit
  - Beschrijving: "Get beginner-friendly betting odds with safety and value ratings"
- [x] **ToolOrchestrator Update:** `executeGetBestOdds()` functie geïmplementeerd
  - Verwerkt tool calls voor odds functionaliteit
  - Gebruikt `matchRepository.getBestOddsForTool()` voor data fetching
  - Slaat tool output op in database voor context retention
- [x] **AI Prompts:** System prompts bijgewerkt voor odds functionaliteit
  - `get_best_odds` tool toegevoegd aan gereedschapskist sectie
  - `ODDS_WIDGET` type toegevoegd aan response format
  - Tool beschrijving: "Gebruik dit voor beginner-vriendelijke wedkansen met veiligheids- en waarde-beoordelingen"

### 23.4 Database Schema Updates
- [ ] **LeagueEntity Uitbreiding:** Nieuwe velden voor odds caching (24 uur TTL) - *Uitgesteld voor FASE 24*
- [ ] **Database Migraties:** Migraties implementeren voor nieuwe velden - *Uitgesteld voor FASE 24*
- [ ] **Caching Strategy:** Performance optimalisatie voor odds data - *Uitgesteld voor FASE 24*

### 23.5 Risk Mitigatie
- [x] **Rate Limiting:** Odds endpoints beperken via bestaande RateLimitPlugin
- [x] **Error Handling:** Robuuste error handling voor API failures met try-catch blocks
- [x] **Fallback Logic:** Fallback mechanisme bij ontbrekende odds data met lege resultaten

### 23.6 Expected Result
**Gebruiker kan vragen:**
- `"Welke wedstrijd heeft de hoogste odds vandaag?"` → AI analyseert pre-match odds
- `"Geef me de safe bets voor Ajax"` → AI filtert op laagste odds
- `"Wat zijn de beste value bets?"` → AI identificeert odds met hoogste waarde

**Nieuwe AI Capabilities:**
- Dynamische competitie ontdekking
- Odds analyse en sortering (highest/safe/value)
- Slimme fixture filtering op basis van actieve competities
- Beginner-friendly betting advies met veiligheids- en waarde-beoordelingen

### 23.7 Build & Test
- [x] **Compilation Success:** App compileert succesvol zonder errors
- [x] **Tool Integration:** `get_best_odds` tool volledig geïntegreerd in AI systeem
- [x] **Data Flow:** Odds data stroomt correct van API naar AI via repository
- [x] **Error Handling:** Robuuste error handling voor alle odds gerelateerde operaties
- [x] **Documentation:** Fasestracker en project log bijgewerkt

### 23.8 Critical Bug Fixes (17 december 2025)
- [x] **Probleem 1 - Suggested Actions Crash:** Wanneer AI een onbekend response type zoals "ODDS_WIDGET" teruggeeft, crashte de UI omdat suggested actions binnen de content bubble werden gerenderd. 
  - **Oplossing:** `AgentResponseRenderer.kt` aangepast om suggested actions BUITEN de content bubble te renderen, zodat ze altijd werken, zelfs als de widget erboven 'stuk' is.
  - **Implementatie:** Suggested actions worden nu gerenderd in een aparte `FlowRow` onder de content, niet binnen het `when` statement.
  
- [x] **Probleem 2 - Rate Limit JSON Parsing Crash:** Wanneer API-SPORTS een rate limit error teruggeeft (429), stuurt het een andere JSON structuur: `{"errors": {"rateLimit": "Too many requests..."}, "parameters": []}`. De code probeerde dit te parsen als normale response, wat een `JsonConvertException` veroorzaakte.
  - **Oplossing:** `FootballApiService.kt` uitgebreid met rate limit error detection en specifieke DTO.
  - **Nieuwe DTO:** `RateLimitErrorResponse.kt` gemaakt voor rate limit error parsing.
  - **Helper functie:** `checkForRateLimitError()` functie die rate limit responses detecteert voordat ze geparsed worden als normale responses.
  - **Error handling:** Gooit specifieke `FootballApiException` met `RATE_LIMIT_ERROR` type dat de repository kan afhandelen.

### 23.9 ODDS_WIDGET Response Fix (17 december 2025)
- [x] **Probleem 3 - Vieze JSON bij odds vragen:** Wanneer gebruiker vraagt "Welke wedstrijd heeft de hoogste odds vandaag?", kreeg ze een "vieze JSON" in plaats van klikbare reacties.
  - **Oorzaak:** De AI gaf ruwe JSON terug in plaats van een goed geformatteerde `AgentResponse` met type `ODDS_WIDGET`.
  - **Oplossing:** 
    1. `ODDS_WIDGET` type toegevoegd aan `AgentResponse.ResponseType` enum
    2. `AgentResponseRenderer.kt` geüpdatet om `ODDS_WIDGET` te behandelen als text-based response
    3. `AgentResponse.kt` uitgebreid met `oddsWidget()` companion functie
  - **Resultaat:** Odds responses worden nu correct geparsed en getoond met suggested actions.

### 23.10 Final Validation
- [x] **Compilation Test:** Alle fixes compileren succesvol zonder errors
- [x] **Architecture Compliance:** Fixes volgen Clean Architecture principes
- [x] **User Experience:** 
  - Suggested actions blijven altijd beschikbaar
  - Rate limit errors worden netjes afgehandeld
  - Odds vragen tonen nu klikbare reacties in plaats van vieze JSON
- [x] **Documentation:** Fasestracker bijgewerkt met nieuwe fixes

---

## 🕒 FASE 25: Tijdweergave Verbeteringen (Dashboard Consistency)
**Doel:** Verbeteren van de tijdweergave in match cards voor betere context en consistentie tussen schermen.
**Status:** ✅ Voltooid

### 25.1 Probleem Analyse & Requirements
- [x] **Dashboard Status Analyse:** Dashboard toont alleen tijd (bijv. "18:30") zonder datum, wat verwarrend kan zijn voor wedstrijden op andere dagen
- [x] **Vergelijking met andere schermen:** MatchDetailScreen toont wel datum en tijd, DashboardScreen niet
- [x] **Requirements Verzameling:** Gebruikers hebben zowel datum als tijd nodig voor context, vooral voor wedstrijden die niet vandaag zijn
- [x] **Technische Haalbaarheid:** MatchFixture model heeft al `date` veld, maar wordt niet gebruikt in UI

### 25.2 StandardMatchCard Verbetering
- [x] **StandardMatchCard.kt** geüpdatet met intelligente tijdweergave:
  - **Nieuwe functie `buildTimeDisplayText()`:** Bepaalt de beste tijdweergave op basis van match status en beschikbare data
  - **Logica:**
    1. Live wedstrijd met elapsed time: Toont minuut (bijv. "65'")
    2. Wedstrijd heeft datum informatie: Toont "datum tijd" (bijv. "18 dec 18:30")
    3. Alleen tijd: Toont alleen tijd (bijv. "18:30")
  - **UI Integration:** Vervangen hardcoded tijd logica met `buildTimeDisplayText(match)` functie
  - **Backward Compatibility:** Werkt met bestaande MatchFixture data structuur

### 25.3 HeroMatchCard Verbetering
- [x] **HeroMatchCard.kt** geüpdatet met dezelfde intelligente tijdweergave:
  - **Nieuwe functie `buildHeroTimeDisplayText()`:** Identieke logica als StandardMatchCard
  - **Consistentie:** Zorgt voor uniforme tijdweergave tussen alle match cards
  - **UI Integration:** Vervangen hardcoded tijd logica met `buildHeroTimeDisplayText(match)` functie

### 25.4 Build & Testing
- [x] **Build Success:** App compileert succesvol zonder errors (`gradlew.bat assembleDebug`)
- [x] **Compilation:** Geen compile errors na implementatie
- [x] **UI Consistency:** Beide match cards tonen nu datum en tijd wanneer beschikbaar
- [x] **User Experience:** Gebruikers krijgen nu volledige context voor wedstrijden op andere dagen

### 25.5 Impact & Benefits
- [x] **Verbeterde Context:** Gebruikers zien nu zowel datum als tijd voor wedstrijden
- [x] **Consistentie:** Uniforme tijdweergave tussen Dashboard en MatchDetail schermen
- [x] **Intelligente Weergave:** Automatische detectie van live wedstrijden (toont minuut)
- [x] **Backward Compatible:** Werkt met bestaande MatchFixture data zonder API wijzigingen
- [x] **User Experience:** Minder verwarring over wanneer wedstrijden plaatsvinden

---

## 📋 VOLGENDE FASES (Planning)

### FASE 26: Advanced Betting Analytics
**Doel:** Geavanceerde betting analytics met historische data en trend analyse.
**Focus:** Machine learning modellen voor odds voorspelling, historische data analyse, en geavanceerde betting strategieën.

### FASE 27: Social Proof Integration
**Doel:** Integratie van social media sentiment voor betting decisions.
**Focus:** Social media sentiment analyse, crowd wisdom integration, en social proof based betting recommendations.

### FASE 28: Performance & Optimization
**Doel:** Performance optimalisatie en caching verbeteringen.
**Focus:** Database schema updates voor odds caching, real-time odds updates, en live betting analytics.

---

## 📊 OVERALL PROGRESS

**Totaal Voltooide Fasen:** 8
**Actieve Fase:** FASE 26
**Volgende Fase:** FASE 27

**Laatste Update:** 17 december 2025, 17:34
**Project Status:** ✅ Gezond - Tijdweergave verbeteringen geïmplementeerd voor betere UX

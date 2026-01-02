# 🎮 Settings Screen - Complete Documentatie

## 📋 Overzicht

De Settings screen is het centrale configuratiepunt voor MatchMind AI. Het bevat alle gebruikersinstellingen, API key management, dashboard personalisatie en systeemvoorkeuren.

## 📁 Bestandslocaties

### Core Bestanden
```
app/src/main/java/com/Lyno/matchmindai/
├── presentation/screens/
│   └── SettingsScreen.kt              # Hoofd UI component
├── presentation/viewmodel/
│   └── SettingsViewModel.kt           # State management
├── domain/model/
│   └── UserPreferences.kt             # Domain model
├── data/repository/
│   └── SettingsRepositoryImpl.kt      # Data opslag
└── data/local/
    └── ApiKeyStorage.kt               # Secure API key opslag
```

### Navigatie
```
app/src/main/java/com/Lyno/matchmindai/presentation/navigation/
└── Screen.kt                          # Route: "settings"
```

## 🏗️ Architectuur

### Data Flow
```
UI (SettingsScreen)
    ↓ (Events)
SettingsViewModel
    ↓ (Use Cases)
SettingsRepository
    ↓ (Data Sources)
DataStore / SharedPreferences
```

### Dependency Graph
```
SettingsScreen → SettingsViewModel → SettingsRepository → ApiKeyStorage
                                      ↓
                                   UserPreferences (Domain)
```

## 📊 Screen Structuur

### 1. 🎮 Control Room Sectie
**Header:** "🎮 Control Room - Beheer je persoonlijke voetbalervaring"

#### a. Usage Widget
- **Component:** `UsageWidget.kt`
- **Functionaliteit:** Toont API usage tracking
- **Data:** `apiCallsRemaining` / `apiCallsLimit`
- **Visualisatie:** Progress bar met kleurcodering
  - Groen: > 25% beschikbaar
  - Oranje: 10-25% (warning)
  - Rood: < 10% (critical)

#### b. Favorite Team Card
- **Toont:** Favoriete team naam en ID
- **Status:** 
  - Geselecteerd: Toont team info + "Wis Favoriet" button
  - Niet geselecteerd: "TODO: Team selectie implementeren" placeholder
- **Functionaliteit:** 
  - `clearFavoriteTeam()` - Wis favoriet
  - `selectFavoriteTeam()` - TODO: Team selectie implementatie

#### c. Data Saver Mode
- **Toggle:** `liveDataSaver` boolean
- **Beschrijving:** "Bespaar data door minder live data op te halen"
- **ViewModel functie:** `toggleDataSaver(Boolean)`

### 2. 📊 Dashboard Settings (DashX)
**Header:** "📊 Dashboard Settings - Configure how matches are displayed on your dashboard"

#### a. Show Live Matches Only
- **Setting:** `showLiveOnly: Boolean`
- **Beschrijving:** "Filter dashboard to show only matches currently in play"
- **ViewModel:** `updateShowLiveOnly(Boolean)`

#### b. Show Hero Match
- **Setting:** `showHeroMatch: Boolean`
- **Beschrijving:** "Display the most exciting match prominently at the top"
- **ViewModel:** `updateShowHeroMatch(Boolean)`

#### c. Show Predictions
- **Setting:** `showPredictions: Boolean`
- **Beschrijving:** "Display AI predictions for matches on dashboard"
- **ViewModel:** `updateShowPredictions(Boolean)`

#### d. Show Betting Odds
- **Setting:** `showOdds: Boolean`
- **Beschrijving:** "Display betting odds for matches on dashboard"
- **ViewModel:** `updateShowOdds(Boolean)`

#### e. Show Injury Information
- **Setting:** `showInjuries: Boolean`
- **Beschrijving:** "Display player injury information for matches"
- **ViewModel:** `updateShowInjuries(Boolean)`

#### f. Reset Dashboard Settings
- **Component:** Clickable card met delete icon
- **Functionaliteit:** Reset alle dashboard settings naar defaults
- **ViewModel:** `resetDashSettings()`

### 3. 🔄 Cache Management
- **Component:** "🔄 Cache Wissen" card
- **Functionaliteit:** Wis alle in-memory cache
- **Flow:** 
  1. Click → Toon confirmation dialog
  2. Bevestig → `clearAllCache()`
  3. Loading indicator tijdens operatie
- **Betekenis:** Forceert verse data bij volgende API calls

### 4. 📡 Live Data Switch
- **Setting:** `useLiveData: Boolean`
- **Label:** "Use Live Data"
- **Beschrijving:** "Schakel live data updates in of uit"
- **ViewModel:** `toggleLiveData(Boolean)`

### 5. 🔑 API Keys Sectie
**Header:** "🔑 API Keys - Configureer je API keys voor volledige functionaliteit"

#### a. DeepSeek API Key
- **Input:** `CyberTextField` met password visual transformation
- **Label:** "DeepSeek API Key"
- **Placeholder:** "Plak hier je DeepSeek API key..."
- **ViewModel:** `updateDeepSeekApiKey(String)`
- **Verplicht:** Ja (save button disabled zonder)

#### b. API-Sports Key
- **Input:** `CyberTextField` met password visual transformation
- **Label:** "API-Sports Key"
- **Placeholder:** "Voer je API-Sports key in"
- **ViewModel:** `updateApiSportsKey(String)`

#### c. Save Button
- **Component:** `PrimaryActionButton`
- **Text:** "Save"
- **Enabled:** Alleen als DeepSeek key ingevuld
- **Functionaliteit:** `saveApiKeys()`
- **Feedback:** Snackbar met success/error message

#### d. API Key Links
- **DeepSeek API Key:** Link naar `https://platform.deepseek.com/api_keys`
- **API-Sports Dashboard:** Link naar `https://dashboard.api-football.com`

### 6. 🎉 Footer
- **App Version:** Klik 8x voor easter egg
- **Copyright:** "MatchMind AI © 2025"
- **Easter Egg:** Toont familie afbeelding bij 8 clicks

## 🔧 Technische Implementatie

### UserPreferences Model
```kotlin
data class UserPreferences(
    // API Keys
    val deepSeekApiKey: String = "",
    val tavilyApiKey: String = "",          // Wordt verwijderd
    val apiSportsKey: String = "",
    
    // Live Data
    val useLiveData: Boolean = true,
    
    // Rate Limit Tracking
    val apiCallsRemaining: Int = 100,
    val apiCallsLimit: Int = 100,
    val lastRateLimitUpdate: Long = 0L,
    
    // Favorite Team
    val favoriteTeamId: String? = null,
    val favoriteTeamName: String? = null,
    
    // Data Saver
    val liveDataSaver: Boolean = false,
    
    // DashX Settings
    val dashSettings: DashSettings = DashSettings()
)
```

### DashSettings Model
```kotlin
data class DashSettings(
    val showLiveOnly: Boolean = false,
    val showHeroMatch: Boolean = true,
    val selectedLeagueIds: Set<Int> = emptySet(),
    val selectedTeamIds: Set<Int> = emptySet(),
    val sortOrder: DashSortOrder = DashSortOrder.EXCITEMENT,
    val maxMatches: Int = 20,
    val showPredictions: Boolean = true,
    val showOdds: Boolean = true,
    val showInjuries: Boolean = true
)
```

### SettingsViewModel State
```kotlin
data class SettingsUiState(
    val userPreferences: UserPreferences = UserPreferences(),
    val dashSettings: DashSettings = DashSettings(),
    val deepSeekApiKey: String = "",
    val tavilyApiKey: String = "",
    val apiSportsKey: String = "",
    val isLoading: Boolean = false,
    val keySaved: Boolean = false,
    val error: String? = null,
    val showCacheConfirmation: Boolean = false
)
```

## 🚀 Geplande Verbeteringen

### 1. Tavily API Key Verwijderen
**Status:** ✅ Voltooid (Fase 22)
**Acties:**
- ✅ Verwijder Tavily input veld uit UI
- ✅ Verwijder Tavily link uit API Key Links
- ✅ Update `UserPreferences` (tavilyApiKey gemarkeerd als deprecated voor backward compatibility)
- ✅ Update `SettingsViewModel` (verwijder `updateTavilyApiKey()`)

### 2. API Key Status Indicatie
**Probleem:** Gebruikers zien niet duidelijk of API keys gekoppeld zijn
**Oplossing:** `ApiKeyStatusCard` component
```kotlin
// UI Gedrag:
if (key.isBlank()) {
    // Toon input veld
    CyberTextField(...)
} else {
    // Toon status card
    ApiKeyStatusCard(
        provider = "DeepSeek",
        status = "✓ Gekoppeld",
        color = Color.Green,
        onEditClick = { /* Toon edit mode */ }
    )
}
```

### 3. Dashboard Switches Fix
**Probleem:** Switches reageren niet altijd
**Oplossing:**
- Verifieer `updateShowX()` methoden in ViewModel
- Check state updates flow
- Voeg visuele feedback toe

### 4. Favoriet Team Selectie Implementatie
**Status:** TODO placeholder
**Implementatie Plan:**
1. `TeamSelectionScreen.kt` - Nieuwe screen met team zoekfunctie
2. `TeamSelectionViewModel.kt` - State management
3. `SearchTeamsUseCase.kt` - Domain use case
4. Navigatie: Settings → TeamSelection → Back met geselecteerd team

## 🔒 Security

### API Key Opslag
- **Methode:** DataStore met encryption
- **Bestand:** `ApiKeyStorage.kt`
- **Encryptie:** AES-256 via Android Keystore
- **No hardcoding:** Keys worden nooit in code gehardcode

### Password Visual Transformation
- **Input velden:** Gebruiken `PasswordVisualTransformation()`
- **No plain text:** Keys zijn nooit zichtbaar als plain text

## 🎨 UI/UX Design

### Cyber-Minimalist Principes
- **Kleuren:** Donkere theme met neon green accenten
- **Typography:** Clean, monospace-inspired fonts
- **Spacing:** Ruimtelijke hiërarchie met consistente padding
- **Icons:** Minimalistische icon set

### Componenten
- `CyberTextField` - Custom text input met cyber styling
- `PrimaryActionButton` - Primaire actie button
- `UsageWidget` - API usage progress widget
- `Card` - Material3 cards voor secties

## 📱 Navigatie

### Toegangspunten
1. **Dashboard:** Settings icon in top bar
2. **Navigatie:** Route `"settings"`
3. **Back stack:** `onNavigateBack = { navController.popBackStack() }`

### Screen Route
```kotlin
object Settings : Screen("settings")
```

## 🐛 Bekende Issues

### 1. Tavily API Key Overbodig
- **Issue:** ✅ OPGELOST - Tavily is verwijderd uit de app (Fase 22)
- **Impact:** Geen verwarring meer voor gebruikers
- **Fix:** Tavily input veld en link verwijderd uit SettingsScreen

### 2. Dashboard Switches Non-responsive
- **Issue:** Switches reageren soms niet op clicks
- **Impact:** Gebruikers kunnen dashboard niet configureren
- **Fix:** State management verbeteren

### 3. Favoriet Team Placeholder
- **Issue:** "TODO: Team selectie implementeren"
- **Impact:** Functionaliteit niet beschikbaar
- **Fix:** TeamSelectionScreen implementeren

### 4. API Key Status Onduidelijk
- **Issue:** Gebruikers weten niet of keys gekoppeld zijn
- **Impact:** Onzekerheid over configuratie
- **Fix:** `ApiKeyStatusCard` implementeren

## 🔄 Cache Management Details

### Wat wordt gewist:
- In-memory cache voor predictions
- In-memory cache voor injuries
- In-memory cache voor odds
- In-memory cache voor match details

### Wat blijft behouden:
- API keys in DataStore
- UserPreferences in DataStore
- Favorite team in DataStore
- DashSettings in DataStore

### Flow:
```
User clicks "Cache Wissen"
    ↓
Show confirmation dialog
    ↓
User confirms
    ↓
Show loading indicator
    ↓
ViewModel.clearAllCache()
    ↓
Repository.clearCache()
    ↓
Hide loading indicator
    ↓
Show success feedback
```

## 📈 API Usage Tracking

### Rate Limit Management
- **Provider:** API-Sports
- **Limiet:** 100 calls per dag (default)
- **Tracking:** `apiCallsRemaining` / `apiCallsLimit`
- **Update:** Bij elke API call wordt counter verlaagd

### Usage Widget Logica
```kotlin
val apiUsagePercentage = if (apiCallsLimit > 0) {
    ((apiCallsRemaining.toFloat() / apiCallsLimit.toFloat()) * 100).toInt()
} else {
    100
}

val isApiUsageCritical = apiUsagePercentage < 10
val isApiUsageWarning = apiUsagePercentage < 25
```

## 🔗 Externe Links

### API Documentation
- **DeepSeek:** https://platform.deepseek.com/api_keys
- **API-Sports:** https://dashboard.api-football.com
- **Tavily:** https://app.tavily.com (deprecated)

### Development Resources
- **GitHub:** https://github.com/Farmeobaasje/MatchMind.git
- **Documentation:** `docs/` folder in project root

## 🚨 Error Handling

### API Key Errors
- **Missing DeepSeek Key:** Save button disabled
- **Invalid Key Format:** Error message in snackbar
- **Save Failure:** Error message met details

### Network Errors
- **Cache Operations:** Loading states en error feedback
- **Settings Save:** Retry logica in ViewModel

### State Management
- **Loading States:** CircularProgressIndicator tijdens operaties
- **Success Feedback:** Snackbar met "Successfully saved"
- **Error Feedback:** Snackbar met error message

## 📝 Best Practices

### Code Standards
1. **No hardcoded strings:** Gebruik `stringResource(R.string.x)`
2. **Clean Architecture:** Strict separation of concerns
3. **Kotlin Coroutines:** `suspend` functions voor async operaties
4. **State Management:** `StateFlow` voor UI state

### UI Standards
1. **Material3:** Gebruik Material3 componenten
2. **Theming:** Volg `MatchMindAITheme`
3. **Accessibility:** Content descriptions voor alle interactive elements
4. **Localization:** Strings in Dutch (user-facing)

### Security Standards
1. **No API keys in code:** Altijd via `ApiKeyStorage`
2. **Encrypted storage:** DataStore met encryption
3. **Input validation:** Validate API key format
4. **Error masking:** Don't expose sensitive info in errors

---

## 📋 Checklist voor Implementatie Verbeteringen

- [x] **Tavily verwijderen** (Voltooid - Fase 22)
  - [x] Verwijder input veld uit SettingsScreen
  - [x] Verwijder link uit API Key Links
  - [x] Update UserPreferences (gemarkeerd als deprecated)
  - [x] Update SettingsViewModel

- [ ] **API Key Status Cards**
  - [ ] Maak ApiKeyStatusCard component
  - [ ] Implementeer conditional rendering
  - [ ] Voeg edit functionaliteit toe
  - [ ] Test met lege/gevulde keys

- [ ] **Dashboard Switches Fix**
  - [ ] Verifieer ViewModel updates
  - [ ] Test switch interacties
  - [ ] Voeg visuele feedback toe
  - [ ] Documenteer fixed behavior

- [ ] **Favoriet Team Selectie**
  - [ ] Maak TeamSelectionScreen
  - [ ] Implementeer TeamSelectionViewModel
  - [ ] Maak SearchTeamsUseCase
  - [ ] Update MatchRepository
  - [ ] Integreer in SettingsScreen
  - [ ] Test volledige flow

- [ ] **Documentatie Updates**
  - [ ] Update deze documentatie na implementatie
  - [ ] Update `docs/05_project_log.md`
  - [ ] Update `docs/00_fasetracker.md`

---

**Laatst bijgewerkt:** 26/12/2025  
**Documentatie versie:** 1.0  
**MatchMind AI versie:** Zie app footer voor huidige versie

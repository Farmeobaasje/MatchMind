# 🔧 FASE 23: Settings Issues Fix & Feature Completion - Implementatieplan

## 📋 Overzicht
Fase 23 richt zich op het oplossen van bekende Settings issues en het implementeren van ontbrekende functionaliteit. Deze fase bestaat uit drie hoofdonderdelen:
1. **Dashboard Switches Fix** - Oplossen van non-responsive switches in SettingsScreen
2. **Favoriet Team Selectie** - Implementeren van team selectie functionaliteit
3. **API Key Status Cards** (Optioneel) - Verbetering van API key management UI

## 🎯 Prioriteit
1. **Dashboard Switches Fix** (Hoogste prioriteit - bug fix)
2. **Favoriet Team Selectie** (Medium prioriteit - feature completion)
3. **API Key Status Cards** (Lage prioriteit - UI verbetering)

---

## 🛠️ 23.1 Dashboard Switches Fix (Non-responsive Switches)

### Probleem Analyse
**Symptoom:** Switches in SettingsScreen reageren soms niet op clicks
**Mogelijke oorzaken:**
1. State updates in ViewModel worden niet correct doorgegeven aan UI
2. Toggle functies missen of zijn incorrect geïmplementeerd
3. StateFlow updates worden niet correct ge-collecteerd in UI
4. UI binding is incorrect of ontbreekt

### Onderzoek Bestanden
1. **SettingsViewModel.kt** - Controleer alle `updateShowX()` methoden:
   - `updateShowLiveOnly()`
   - `updateShowHeroMatch()`
   - `updateShowPredictions()`
   - `updateShowOdds()`
   - `updateShowInjuries()`

2. **SettingsScreen.kt** - Controleer UI binding:
   - Switch components en hun `checked` state
   - `onCheckedChange` callbacks
   - State collection met `collectAsStateWithLifecycle()`

3. **SettingsRepositoryImpl.kt** - Controleer data opslag:
   - `updateDashSettings()` implementatie
   - DataStore updates en error handling

### Implementatie Stappen
1. **Audit SettingsViewModel:**
   - Verifieer dat alle `updateShowX()` methoden bestaan en correct werken
   - Controleer dat `_uiState.update()` correct wordt aangeroepen
   - Test state updates met logging

2. **Audit SettingsScreen:**
   - Verifieer dat switches correct zijn gebonden aan ViewModel state
   - Controleer `onCheckedChange` callbacks
   - Test recompositie met state changes

3. **Fix Implementatie:**
   - Repareer ontbrekende of incorrecte methoden
   - Verbeter state management indien nodig
   - Voeg logging toe voor debugging

4. **Testing:**
   - Test alle toggle interacties
   - Verifieer state persistence
   - Test error scenarios

### Technische Details
- **State Management:** Gebruik `MutableStateFlow` voor reactive updates
- **UI Binding:** Gebruik `collectAsStateWithLifecycle()` voor lifecycle-aware state collection
- **Error Handling:** Implementeer snackbar feedback voor errors
- **Logging:** Voeg logging toe voor debugging tijdens development

---

## 🏆 23.2 Favoriet Team Selectie Implementatie

### Functionaliteit Overzicht
**Doel:** Vervang de "TODO: Team selectie implementeren" placeholder met een volledige team selectie functionaliteit.

### Nieuwe Bestanden
1. **TeamSelectionScreen.kt** (`presentation/screens/`)
   - Team zoekfunctie met search bar
   - Team lijst met filtering
   - Selectie functionaliteit
   - Navigatie terug naar SettingsScreen

2. **TeamSelectionViewModel.kt** (`presentation/viewmodel/`)
   - State management voor team selectie
   - Search functionaliteit
   - Team filtering en sorting
   - Navigatie state

3. **SearchTeamsUseCase.kt** (`domain/usecase/`)
   - Domain logica voor team zoeken
   - Team filtering en matching
   - Error handling

4. **TeamSelectionState.kt** (`presentation/model/`)
   - UI state model voor team selectie
   - Loading, Success, Error states
   - Selected team state

### Implementatie Stappen
1. **Domain Layer:**
   - Maak `SearchTeamsUseCase` voor team zoeklogica
   - Update `TeamRepository` interface indien nodig
   - Maak `TeamSelection` domain model

2. **Data Layer:**
   - Update `TeamRepositoryImpl` met search functionaliteit
   - Implementeer team caching voor performance
   - Voeg error handling toe

3. **Presentation Layer:**
   - Maak `TeamSelectionScreen` met Jetpack Compose UI
   - Implementeer `TeamSelectionViewModel` met state management
   - Maak UI components: TeamList, TeamCard, SearchBar
   - Implementeer team selectie en favoriet marking

4. **Navigatie:**
   - Voeg `TeamSelection` route toe aan navigatie
   - Implementeer SettingsScreen → TeamSelectionScreen navigatie
   - Implementeer terug navigatie met geselecteerd team
   - Update SettingsScreen om geselecteerd team te tonen

5. **Data Persistence:**
   - Update `UserPreferences` met favoriete team velden
   - Update `SettingsRepository` met team opslag methods
   - Implementeer team opslag in DataStore

### UI Design
- **Search Bar:** Zoekfunctionaliteit met real-time filtering
- **Team List:** Lijst van teams met logo's en informatie
- **Team Card:** Individuele team card met selectie indicator
- **Selection Confirmation:** Bevestiging dialog voor team selectie
- **Empty State:** "Geen teams gevonden" state voor lege zoekresultaten

### Technische Details
- **Clean Architecture:** Strict separation tussen layers
- **State Management:** `StateFlow` voor reactive updates
- **Search Performance:** Debounce voor search input (300ms)
- **Image Loading:** Coil voor team logo's met caching
- **Error Handling:** Graceful degradation bij netwerk errors
- **Dutch Localization:** Alle UI teksten in het Nederlands

---

## 🔑 23.3 API Key Status Cards (Optioneel - Verbetering)

### Functionaliteit Overzicht
**Doel:** Verbeter de API key management UI door status cards te tonen in plaats van alleen input velden.

### Nieuwe Componenten
1. **ApiKeyStatusCard.kt** (`presentation/components/settings/`)
   - Toont API key status (Gekoppeld/Niet gekoppeld)
   - Color coding voor status (Groen/Rood)
   - Edit functionaliteit voor bestaande keys
   - Visual feedback voor key validiteit

2. **ApiKeyStatus.kt** (`domain/model/`)
   - Data model voor API key status
   - Status enum: `CONNECTED`, `DISCONNECTED`, `INVALID`, `UNKNOWN`
   - Helper functies voor status determination

### Implementatie Stappen
1. **Domain Layer:**
   - Maak `ApiKeyStatus` domain model
   - Maak `ValidateApiKeyUseCase` voor key validatie
   - Update `SettingsRepository` met status methods

2. **Presentation Layer:**
   - Maak `ApiKeyStatusCard` component
   - Implementeer conditional rendering in SettingsScreen
   - Toon status card als key gekoppeld, input veld als leeg
   - Voeg edit functionaliteit toe voor bestaande keys

3. **UI Flow:**
   - **Lege Key:** Toon input veld voor key invoer
   - **Gekoppelde Key:** Toon status card met "✓ Gekoppeld" status
   - **Edit Mode:** Klik op status card toont edit mode met input veld
   - **Save:** Save button update key en toont nieuwe status

4. **Visual Design:**
   - **Color Coding:** Groen voor connected, Rood voor disconnected
   - **Icons:** Checkmark voor valid, Warning voor invalid
   - **Status Text:** "✓ DeepSeek API Key Gekoppeld"
   - **Last Updated:** Timestamp van laatste update

### Technische Details
- **Key Validation:** Basic format validation (niet te kort, juiste prefix)
- **Status Persistence:** Status opgeslagen in DataStore
- **Real-time Updates:** Status updates in real-time
- **Error Feedback:** Duidelijke error messages voor invalid keys
- **Security:** Keys nooit als plain text tonen, altijd masked

---

## 🧪 23.4 Build & Testing

### Compilation Testing
- [ ] Alle bestanden compileren zonder errors
- [ ] Gradle build succesvol
- [ ] Lint checks passed
- [ ] ProGuard/R8 optimization werkt

### UI Testing
- [ ] Dashboard switches werken correct
- [ ] Team selectie flow werkt end-to-end
- [ ] API key status cards tonen correcte status
- [ ] Alle toggle interacties werken
- [ ] Error states worden correct getoond

### Integration Testing
- [ ] SettingsScreen integreert met TeamSelectionScreen
- [ ] Data persistence werkt across app restarts
- [ ] Navigatie flows werken correct
- [ ] State management werkt consistent

### Performance Testing
- [ ] Geen memory leaks in nieuwe screens
- [ ] State updates zijn performant
- [ ] Search functionaliteit is responsive
- [ ] Image loading is geoptimaliseerd

---

## 📚 23.5 Documentation Update

### Fasetracker Update
- [ ] Fase 23 markeren als voltooid na implementatie
- [ ] Alle sub-taken afvinken
- [ ] Build status en testing results documenteren

### Project Log Update
- [ ] Implementatie details toevoegen aan `docs/05_project_log.md`
- [ ] Technische beslissingen documenteren
- [ ] Testing results documenteren

### Settings Documentation Update
- [ ] `docs/files and info/settings.md` bijwerken met nieuwe functionaliteit
- [ ] Nieuwe screens en components documenteren
- [ ] User flows documenteren

### Code Comments
- [ ] Alle nieuwe functies documenteren met KDoc
- [ ] Complexe logica documenteren met comments
- [ ] Architecture decisions documenteren

---

## 🚀 Implementatie Volgorde

### Fase 1: Dashboard Switches Fix (Week 1)
1. **Dag 1:** Probleem analyse en root cause identificatie
2. **Dag 2:** SettingsViewModel audit en fixes
3. **Dag 3:** SettingsScreen audit en fixes
4. **Dag 4:** Testing en bug fixes
5. **Dag 5:** Documentation update

### Fase 2: Favoriet Team Selectie (Week 2-3)
1. **Week 2:** Domain en Data layer implementatie
2. **Week 3:** Presentation layer en UI implementatie
3. **Week 3:** Navigatie en integration testing

### Fase 3: API Key Status Cards (Week 4 - Optioneel)
1. **Dag 1-2:** Domain layer en component design
2. **Dag 3-4:** UI implementation en integration
3. **Dag 5:** Testing en documentation

---

## 🔧 Technische Vereisten

### Dependencies
- **Geen nieuwe dependencies nodig** - gebruikt bestaande:
  - Jetpack Compose (UI)
  - Kotlin Coroutines (Async)
  - DataStore (Persistence)
  - Coil (Image loading)
  - Ktor (Networking)

### Architecture Compliance
- **Clean Architecture:** Strict separation tussen layers
- **Unidirectional Data Flow:** UI → ViewModel → UseCase → Repository
- **State Management:** `StateFlow` voor reactive updates
- **Error Handling:** Graceful degradation bij failures

### Code Standards
- **Modern Kotlin:** `suspend` functions, `Flow` voor data streams
- **Compose Best Practices:** `remember`, `derivedStateOf`, `LaunchedEffect`
- **Dutch Localization:** Alle UI teksten in het Nederlands
- **Security:** No hardcoded API keys, altijd via `ApiKeyStorage`

---

## 📊 Success Criteria

### Functioneel
- [ ] Dashboard switches reageren correct op clicks
- [ ] Team selectie functionaliteit werkt end-to-end
- [ ] API key status cards tonen correcte status (indien geïmplementeerd)
- [ ] Geen regressies in bestaande functionaliteit

### Technisch
- [ ] Alle code compileert zonder errors
- [ ] Geen crashes in nieuwe functionaliteit
- [ ] Performance is acceptabel (geen UI jank)
- [ ] Memory usage is stabiel

### UX
- [ ] UI is responsive en intuïtief
- [ ] Error states zijn gebruikersvriendelijk
- [ ] Loading states zijn duidelijk
- [ ] Navigatie flows zijn logisch

---

## 🎯 Volgende Stappen

1. **Start met Dashboard Switches Fix** - Dit is de hoogste prioriteit bug
2. **Implementeer Team Selection** - Zodra switches fix is getest
3. **Overweeg API Key Status Cards** - Als tijd en resources het toelaten
4. **Documenteer alles** - Voor toekomstige onderhoud en debugging

Dit implementatieplan biedt een gedetailleerde roadmap voor het voltooien van Fase 23. De focus ligt eerst op het oplossen van de kritieke bug (non-responsive switches) en daarna op het implementeren van de ontbrekende team selectie functionaliteit.

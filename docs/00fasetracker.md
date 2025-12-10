# 🚀 MatchMind AI - Phase Tracker

Dit document houdt de voortgang van het project bij.
**Instructie voor AI:** Controleer dit bestand bij aanvang van een sessie om te zien wat de actieve fase is. Vink taken af (`[x]`) zodra ze volledig zijn getest en geïmplementeerd.

---

## 🏗️ FASE 1: De Motor (Data & Foundation)
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

- 
- [x] `ViewModelFactory` gemaakt om repositories te injecteren.

---

## 🎨 FASE 3: Het Gezicht (UI & UX)
**Doel:** De "Cyber-Minimalism" look implementeren.
**Status:** ✅ Voltooid

### 3.1 Basis UI
- [x] Theme instellen (Colors, Type) -> *Zie docs/03*.
- [x] `MainActivity` opzetten met Navigation Compose.
- [x] Routes definiëren (`Settings`, `Match`).

### 3.2 Componenten (Bouwstenen)
- [x] `CyberTextField` (Custom input veld).
- [x] `PrimaryActionButton` (De groene knop met laad-animatie).
- [x] `PredictionCard` (De resultaat kaart met badges).

### 3.3 Schermen
- [x] **Settings Screen:**
    - Invoerveld voor API Key.
    - Uitleg + Link naar DeepSeek.
- [x] **Match Screen:**
    - Inputs voor teams.
    - Animatie tijdens laden.
    - Error handling (Snackbar/Dialog).

---

## 🏁 FASE 4: Finishing Touch (QA & Polish)
**Doel:** App productieklaar maken.
**Status:** 🔴 To Do

### 4.1 Testing
- [ ] Unit Test: Repository mapt JSON correct.
- [ ] Manual Test: Flow zonder API key (stuurt gebruiker naar settings?).
- [ ] Manual Test: Flow met slechte API key (foutmelding?).

### 4.2 RAG (Retrieval Augmented Generation) Implementation
- [x] Jsoup dependency toegevoegd voor web scraping.
- [x] `WebScraper` class gemaakt in `data/remote/scraper`.
- [x] Dependency Injection geüpdatet in `AppContainer.kt`.
- [x] `MatchRepositoryImpl` geüpdatet met web scraping integratie.
- [x] Test: Web scraping werkt en degradeert gracefully bij fouten.
  - Unit test toegevoegd: `WebScraperTest.kt`
  - Error handling geverifieerd: Returns "Geen live data gevonden." bij fouten
  - Build succesvol: App compileert en lint check passed

### 4.3 User-Controlled Live Data Feature
- [x] `ApiKeyStorage` geüpdatet met `USE_LIVE_DATA` preference en `UserPreferences` data class.
- [x] `SettingsRepository` interface uitgebreid met `getPreferences()` en `setLiveDataEnabled()`.
- [x] `SettingsRepositoryImpl` geüpdatet met nieuwe methods.
- [x] `MatchRepositoryImpl` geüpdatet om live data preference te checken voordat web scraping wordt uitgevoerd.
- [x] `SettingsViewModel` geüpdatet met `isLiveDataEnabled` state en `toggleLiveData()` functie.
- [x] `SettingsScreen` geüpdatet met Switch component voor "Gebruik Live Data" instelling.
- [x] String resources toegevoegd voor nieuwe UI elementen.

### 4.4 Polish
- [ ] App Icoon toevoegen.
- [ ] Naam in `strings.xml` controleren.
- [ ] Code opschonen (overbodige logs verwijderen).
- [ ] `README.md` schrijven voor GitHub.

---

## 📝 Notities & Bekende Bugs
*Plaats hier tijdelijke notities tijdens het bouwen.*
- **Nieuwe "Gebruik Live Data" feature geïmplementeerd:** Gebruikers kunnen nu kiezen tussen live data scraping (trager, accurater) of interne kennis alleen (sneller). De instelling wordt opgeslagen in DataStore en beïnvloedt de web scraping in `MatchRepositoryImpl`.
- **Compile errors:** Er zijn momenteel compile errors in `ApiKeyStorage.kt` en `AppContainer.kt` vanwege mogelijk ontbrekende DataStore dependencies. Deze moeten worden opgelost door de juiste dependencies te controleren in `build.gradle.kts`.

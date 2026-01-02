# 01. Architecture Guidelines - MatchMind AI

Dit document beschrijft de architecturale fundamenten van de MatchMind AI applicatie.
Wij hanteren strikte **Clean Architecture** volgens het **MVVM (Model-View-ViewModel)** patroon.

## 1. High-Level Concept
Het doel is **Separation of Concerns**. De code is opgedeeld in drie lagen die onafhankelijk van elkaar kunnen veranderen.
Een belangrijk kenmerk van deze architectuur is **User-Managed Security**: de API Key wordt niet door de app geleverd, maar door de gebruiker zelf ingevoerd en lokaal versleuteld opgeslagen.

### De 3 Lagen (Layers)
1.  **Presentation Layer** (UI & State Management)
2.  **Domain Layer** (Business Logic - *Pure Kotlin*)
3.  **Data Layer** (Network & Persistence)

---

## 2. Layer Details & Responsibilities

### A. Presentation Layer (`presentation`)
* **Doel:** UI tonen, Input verwerken en Settings beheren.
* **Regels:**
    * De UI moet omgaan met een specifieke error state: `MissingApiKey`.
    * Bevat schermen voor zowel Voorspellingen als Instellingen (Key invoer).

### B. Domain Layer (`domain`)
* **Doel:** Bedrijfslogica.
* **Componenten:**
    * **Models:** `MatchPrediction`, `Team`.
    * **Repositories (Interfaces):**
        * `MatchRepository`: Voor het ophalen van voorspellingen.
        * `SettingsRepository`: Voor het opslaan/lezen van de API Key.
    * **Use Cases:**
        * `GetPredictionUseCase`
        * `SaveApiKeyUseCase`
        * `ValidateApiKeyUseCase`

### C. Data Layer (`data`)
* **Doel:** Data leveren en bewaren.
* **Stack:** Ktor Client, Jetpack DataStore (Preferences).
* **Componenten:**
    * **Remote Data Source:** `DeepSeekApi` (Accepteert key als parameter).
    * **Local Data Source:** `ApiKeyStorage` (Beheert DataStore lezen/schrijven).
    * **Repository Impl:** Coördineert tussen Local en Remote.
* **Regels:**
    * Voor elke netwerk-call checkt de Repository eerst de Local Storage.
    * Als de key ontbreekt, wordt de netwerk-call geblokkeerd en een error geretourneerd.

---

## 3. Data Flow (Dynamic Auth)

De stroom van data bij een voorspelling:

1.  **UI Event:** Gebruiker klikt op "Voorspel" -> `ViewModel.predict()`.
2.  **Use Case:** ViewModel roept `GetPredictionUseCase` aan.
3.  **Repository Impl (De cruciale stap):**
    * Stap A: Haal API Key op uit `ApiKeyStorage` (Local).
    * *Check:* Is de key leeg? -> Return `Result.Failure(ApiKeyMissingException)`.
    * Stap B: Is de key aanwezig? -> Roep `DeepSeekApi` aan MET de key.
4.  **Network Request:** Ktor stuurt request naar DeepSeek.
5.  **Result Return:** Data Layer geeft `Result<Prediction>` terug.
6.  **State Update:** ViewModel update UI.
    * Bij `ApiKeyMissingException`: Toon dialoog/scherm om key in te voeren.

---

## 4. Error Handling Strategy
We voegen een specifiek error-type toe voor de UX:

* **Custom Exception:** `class ApiKeyMissingException : Exception()`
* De UI reageert hierop door **niet** een rode foutmelding te tonen, maar de gebruiker vriendelijk naar het instellingen-scherm te leiden.

---

## 5. Project Structure

```text
com.example.matchmind
├── data
│   ├── local          # NIEUW: Opslag (ApiKeyStorage)
│   ├── remote         # API clients (DeepSeekApi)
│   ├── repository     # Repository Implementation (Match & Settings)
│   └── dto            # JSON modellen
├── domain
│   ├── model          # Core modellen
│   ├── repository     # Interfaces (MatchRepository, SettingsRepository)
│   └── usecase        # GetPrediction, SaveApiKey
└── presentation
    ├── screens
    │   ├── match      # Het hoofdscherm
    │   └── settings   # NIEUW: Key invoer scherm
    └── viewmodel      # MatchViewModel, SettingsViewModel
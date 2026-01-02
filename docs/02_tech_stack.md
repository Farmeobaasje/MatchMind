# 02. Technology Stack & Libraries

Dit document definieert de definitieve technische keuzes voor MatchMind AI.
**Kernwaarde:** Wij kiezen voor "Modern Android Development" (MAD). Geen XML, geen SharedPreferences, geen Callbacks. Alles is Reactive en Type-Safe.

## 1. Core Environment
* **Language:** Kotlin (Latest Stable).
* **Build System:** Gradle met **Kotlin DSL** (`build.gradle.kts`).
* **Min SDK:** 26 (Android 8.0) - *Dekt ~96% van alle actieve toestellen.*
* **Target SDK:** 34 (Android 14).
* **Concurrency:** Kotlin Coroutines & Flow (voor alle asynchrone operaties).

## 2. Presentation Layer (UI)
Wij gebruiken **100% Jetpack Compose**.
* **Framework:** Jetpack Compose (via `androidx.compose:compose-bom`).
* **Design System:** Material Design 3 (`androidx.compose.material3`).
* **Navigation:** Jetpack Navigation Compose (`androidx.navigation:navigation-compose`).
    * *Structuur:* Single Activity Architecture.
* **Image Loading:** Coil (`io.coil-kt:coil-compose`).
    * *Configuratie:* Met crossfade animaties en error placeholders.
* **Icons:** Material Icons Extended (`androidx.compose.material:material-icons-extended`).

## 3. Data Layer (Networking & Persistence)

### A. Networking (The Motor)
Wij kiezen voor **Ktor** vanwege de native Kotlin/Coroutines ondersteuning.
* **Client:** Ktor Client Core (`io.ktor:ktor-client-core`).
* **Engine:** Ktor Android Engine (`io.ktor:ktor-client-android`).
* **Features:**
    * `ContentNegotiation`: Voor automatische JSON parsing.
    * `Logging`: Voor debuggen van requests (alleen in DEBUG build).
    * `HttpTimeout`: Om hangende requests af te kappen (15s connect / 30s read).
* **Serialization:** `kotlinx.serialization` (De snelste en veiligste JSON parser voor Kotlin).

### B. Local Persistence (The Vault)
Wij gebruiken **Jetpack DataStore** in plaats van de verouderde SharedPreferences.
* **Library:** DataStore Preferences (`androidx.datastore:datastore-preferences`).
* **Gebruik:** Opslaan van de User API Key en gebruikersvoorkeuren.
* **Toegang:** Uitsluitend via `Flow<T>`, nooit blocking.

## 4. AI Integration
* **Provider:** DeepSeek API.
* **Model:** `deepseek-chat` (V3).
* **Endpoint:** OpenAI-Compatible (`https://api.deepseek.com`).

## 5. Architecture Components
* **ViewModel:** `androidx.lifecycle:lifecycle-viewmodel-compose`.
* **Dependency Injection (DI):**
    * *Fase 1 (MVP):* Manual Dependency Injection.
        * We gebruiken een `AppContainer` class in de `Application` scope.
    * *Fase 2 (Scale):* Google Hilt (Dagger). *Nog niet implementeren.*

## 6. Testing & Quality Assurance
* **Unit Testing:** JUnit 5 (Jupiter).
* **Mocking:** Mockk (`io.mockk:mockk`).
* **Test Dispatchers:** `kotlinx-coroutines-test` (Voor het manipuleren van tijd in tests).
* **UI Testing:** Compose UI Test Manifest (voor debugging van UI boom).

## 7. Development Tools & Guidelines
* **IDE:** Android Studio (Ladybug of nieuwer).
* **AI Assistent:** Cline (Models: GLM-4 voor Planning, DeepSeek-V3 voor Coding).
* **Version Control:** Git (Conventional Commits: `feat:`, `fix:`, `docs:`).

---

## ⚠️ Dependency Rules
1.  **Geen Java Libraries:** Gebruik alleen libraries die geschreven zijn in/voor Kotlin (ivm Coroutine support).
2.  **BOM First:** Gebruik waar mogelijk een Bill of Materials (zoals de Compose BOM) om versies gelijk te trekken.
3.  **Strict Mode:** Geen `transitive dependencies` gebruiken als je ze direct aanroept. Voeg ze expliciet toe aan `build.gradle.kts`.
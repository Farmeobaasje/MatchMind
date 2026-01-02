Natuurlijk. We maken van 04_coding_rules.md een strikt wetboek. Dit bestand is de belangrijkste instructie voor Cline/DeepSeek om te voorkomen dat de code een rommeltje wordt.

Ik heb hem aangescherpt op:

Veiligheid (Geen API keys in code).

Clean Architecture (Strikte scheiding).

Modern Android (Best practices voor Compose en Coroutines).

Hier is de verbeterde versie:

Inhoud voor docs/04_coding_rules.md
Markdown

# 04. Coding Rules & Standards

⚠️ **BELANGRIJK VOOR AI:** Lees deze regels voordat je ook maar één regel code schrijft. Afwijken is niet toegestaan zonder expliciete toestemming van de gebruiker.

## 1. Algemene Workflow & Proces
1.  **Documentation Driven:**
    * Voordat je een nieuwe feature start: Check `01_architecture.md`.
    * Nadat je klaar bent: Update `05_project_log.md`.
2.  **Check op Bestaande Code:**
    * Controleer ALTIJD of een functie, variabele of kleur al bestaat. Hergebruik is verplicht (DRY - Don't Repeat Yourself).
3.  **Taal:**
    * Code (Variabelen, Functies, Classes): **Engels**.
    * UI Teksten (Strings): **Nederlands**.
    * Commentaar/Documentatie: **Nederlands** of **Engels** (wees consistent).

## 2. Architecture Integrity (Clean Architecture)
1.  **Layer Isolation:**
    * `Presentation` mag NOOIT direct met `Data` praten. Alles gaat via de `Domain` layer (Use Cases).
    * `Domain` layer is **Pure Kotlin**. Geen `android.*` imports (behalve heel specifieke uitzonderingen, liever niet).
2.  **Data Flow:**
    * Gebruik `suspend` functies voor one-shot operaties (API calls).
    * Gebruik `Flow<T>` voor streams van data.
3.  **Dependency Injection:**
    * Voorlopig handmatige injectie. Maak dependencies aan in `MainActivity` of een `AppContainer` en geef ze door. Gebruik géén `lateinit var` in classes waar het via de constructor kan.

## 3. Kotlin Code Style
1.  **Type Safety:**
    * Gebruik NOOIT `Any` of `Map<String, Any>` als data-model. Maak altijd een `@Serializable data class` aan.
2.  **Immutability:**
    * Gebruik `val` (read-only) tenzij `var` (mutable) strikt noodzakelijk is.
    * Gebruik `List` in plaats van `MutableList` in publieke functies.
3.  **Coroutines:**
    * Blokkeer NOOIT de Main Thread.
    * Zware operaties (JSON parsing, API calls) moeten expliciet op `Dispatchers.IO` draaien.
4.  **Error Handling:**
    * Vang errors af in de **Repository**. Laat de UI nooit crashen door een netwerkfout.
    * Gebruik `Result<T>` of een eigen `Resource` wrapper wrapper om succes/falen door te geven aan de ViewModel.

## 4. Android & Jetpack Compose Rules
1.  **UI State:**
    * Elk scherm heeft één `UiState` data class in de ViewModel (Single Source of Truth).
    * De UI (Compose) observeert deze state.
2.  **Hardcoding:**
    * ❌ **VERBODEN:** Hardcoded strings in UI code (bijv. `Text("Hallo")`).
    * ✅ **VERPLICHT:** Gebruik `res/values/strings.xml` (bijv. `Text(stringResource(R.string.hello))`).
    * ❌ **VERBODEN:** API Keys direct in de code.
    * ✅ **VERPLICHT:** Gebruik `BuildConfig` (via `local.properties`).
3.  **Preview:**
    * Maak voor elk belangrijk UI component een `@Preview` aan zodat we het visueel kunnen checken zonder de app te runnen.

## 5. File Structure
* Eén Class per Bestand (tenzij het hele kleine helper classes zijn).
* Organiseer bestanden logisch binnen de package structuur:
    * `data/dto` -> Voor API modellen.
    * `data/repository` -> Voor implementaties.
    * `domain/model` -> Voor app modellen.
    * `domain/usecase` -> Voor logica.
    * `presentation/screens` -> Voor schermen.

## 6. AI Specifieke Instructies
* Als je code genereert, geef **altijd** het volledige bestand, geen snippets met `// ... rest of code`.
* Als je een nieuwe library toevoegt aan `build.gradle.kts`, check eerst `02_tech_stac
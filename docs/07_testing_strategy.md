# 07. Testing Strategy & Quality Assurance

Kwaliteit is geen toeval. Omdat we met externe AI werken (DeepSeek) die onvoorspelbaar kan zijn, is onze teststrategie defensief: we gaan ervan uit dat de API faalt, en testen of de app dan overeind blijft.

## 1. Test Piramide

We hanteren drie niveaus van testen:

### A. Unit Tests (De basis)
* **Wat:** Testen van losse functies en classes in isolatie.
* **Scope:** Voornamelijk de **Domain Layer** en **Data Layer parsing**.
* **Framework:** JUnit 4/5 + Mockk (voor het mocken van de API).
* **Must-Have Tests:**
    * `MatchRepository`: Wat gebeurt er als de JSON `null` is? Of als velden missen?
    * `GetPredictionUseCase`: Geeft hij de juiste data door?
    * `Parsers`: Test of een corrupte JSON string netjes wordt opgevangen zonder crash.

### B. UI / Integration Tests
* **Wat:** Werkt het scherm zoals verwacht?
* **Scope:** **Presentation Layer**.
* **Framework:** Compose UI Test.
* **Scenario's:**
    * Toont de app een "Loading Spinner" als we op de knop drukken?
    * Verschijnt de "Error Card" als we het internet uitzetten?
    * Wordt de uitslag correct weergegeven in de Card?

### C. Manual AI "Sanity Checks" (The 'Vibe Check')
Omdat we de AI niet kunnen unit-testen (hij is elke keer anders), doen we handmatige checks voor elke release.
* **De Hallucinatie Test:** Vraag om een niet-bestaande wedstrijd (bijv. "Ajax vs LA Lakers"). De AI moet aangeven dat dit niet kan, of de app moet de fout afvangen.
* **De JSON Test:** Werkt de `response_format` instructie nog? (Soms updaten AI providers hun modellen).

---

## 2. Test Data (Mocks)

Om te ontwikkelen zonder elke keer voor API calls te betalen, gebruiken we **Mock Data** in de `Debug` build.

**Voorbeeld Mock JSON:**
```json
{
  "winner": "Feyenoord",
  "confidence_score": 82,
  "reasoning": "MOCK DATA: Feyenoord heeft 5x op rij gewonnen. Ajax mist 3 spelers."
}
Regel: De Repository moet een switch hebben: if (BuildConfig.DEBUG && USE_MOCKS) return mockData.3. Edge Cases & Error HandlingDe app mag NOOIT crashen (Force Close). We testen specifiek op deze scenario's:ScenarioVerwacht GedragGeen InternetToon "Geen verbinding" Snackbar/Dialog.API Timeout (>10s)Toon "De AI denkt te lang na. Probeer opnieuw."API Key InvalidToon duidelijke foutmelding (alleen in Debug mode).Lege Input"Voorspel" knop moet disabled zijn.Gekke TekensInput als "😊😊😊" moet gefilterd worden of mag geen crash veroorzaken.4. Release Checklist (Voor GitHub Push)Voordat code naar de main branch gaat:[ ] Code compileert zonder warnings.[ ] App start op in Emulator.[ ] Eén succesvolle AI call gedaan.[ ] Eén succesvolle error-afhandeling (wifi uit) getest.[ ] docs/05_project_log.md bijgewerkt.
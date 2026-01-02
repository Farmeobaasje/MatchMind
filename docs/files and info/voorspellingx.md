Hier is het complete, modulaire implementatieplan voor **Project VoorspellingenX**.

Dit plan is specifiek geschreven voor **Cline** (de AI-coding assistant). Het is opgedeeld in logische fases om de codebase veilig te migreren van de oude "RSS-ChiChi" naar de nieuwe "DeepChi-Data" structuur.

Elke fase bevat specifieke bestandsnamen, acties en de logica die geïmplementeerd moet worden.

---

# 🚀 PROJECT VOORSPELLINGEN-X: IMPLEMENTATIE PLAN

**Doel:** Transformatie van nieuws-gebaseerde ruis naar data-gedreven AI-analyse.
**Core Tech:** Kotlin, API-Sports v3, DeepSeek LLM.

---

## FASE 1: OPRUIMEN & FUNDERING

**Doel:** Verwijder de oude nieuws-logica en bereid de datastructuren voor op de nieuwe metrics.

### 1.1 Sanering (Cleanup)

* **Verwijder:** `domain/service/NewsImpactAnalyzer.kt` (Oude RSS logica).
* **Verwijder:** Alle RSS-gerelateerde dependencies uit `build.gradle` indien niet meer nodig.

### 1.2 Update Context Model

Pas het datamodel aan om harde feiten op te slaan in plaats van "sentiment".

* **Bestand:** `domain/model/SimulationContext.kt`
* **Actie:** Vervang de oude velden door de nieuwe "Trinity" metrics.

```kotlin
data class SimulationContext(
    val fatigueScore: Int,      // 0-100 (100 = Doodop, berekend uit schema)
    val styleMatchup: Double,   // 0.5 - 1.5 (1.0 = Neutraal, >1.0 = Voordelig voor Team A)
    val lineupStrength: Int,    // 0-100 (Impact van ontbrekende sleutelspelers)
    val reasoning: String       // Korte uitleg van DeepSeek
)

```

---

## FASE 2: API-SPORTS DATA HARVESTING

**Doel:** Zorg dat we de *specifieke* data binnenkrijgen die DeepSeek nodig heeft om te redeneren.

### 2.1 Extended Fixture Data (Voor Fatigue)

* **Bestand:** `data/remote/ApiSportsClient.kt`
* **Taak:** Functie toevoegen `getLastMatches(teamId: Int, count: Int = 5)`.
* **Endpoint:** `GET /fixtures?team={id}&last=5`
* **Nodige data:** `fixture.date`, `fixture.venue.city` (voor reistijd schatting).

### 2.2 Advanced Team Stats (Voor Style Clash)

* **Bestand:** `data/remote/ApiSportsClient.kt`
* **Taak:** Functie toevoegen `getTeamTactics(teamId: Int, season: Int)`.
* **Endpoint:** `GET /teams/statistics`
* **Nodige data:**
* `lineups` (meest gebruikte formaties).
* `clean_sheet` (defensieve stabiliteit).
* `goals.for.minute` (scorende fases).



### 2.3 Live Lineups (Voor Synergy)

* **Bestand:** `data/remote/ApiSportsClient.kt`
* **Taak:** Functie toevoegen `getFixtureLineups(fixtureId: Int)`.
* **Endpoint:** `GET /fixtures/lineups`
* **Fallback:** Als lineups nog niet beschikbaar zijn (bijv. >1u voor wedstrijd), gebruik `getInjuries` om verwachte afwezigen te vinden.

---

## FASE 3: DEEP-CHI ENGINE (DEEPSEEK INTEGRATIE)

**Doel:** De hersenen van het systeem. DeepSeek interpreteert de JSON data uit Fase 2.

### 3.1 De Prompts Construeren

* **Bestand:** `domain/ai/DeepChiPrompts.kt` (Nieuw bestand)
* **Actie:** Maak templates voor de 3 analyses.

**Prompt Structuur (Concept):**

```text
JIJ BENT EEN VOETBAL DATA ANALIST.
Input Data:
1. Schema Team A: [Lijst met datums/locaties laatste 5 games]
2. Stijl Team A: [Formatie, Goals/Min, CleanSheets]
3. Stijl Team B: [Formatie, Goals/Min, CleanSheets]
4. Opstelling: [Lijst met spelers + Ratings]

TAAK:
1. Bereken FatigueScore (0-100) gebaseerd op rustdagen en reizen.
2. Bereken StyleMatchup (0.5-1.5): Is de verdediging van B bestand tegen aanval A?
3. Bereken LineupStrength (0-100): Spelen de beste spelers?

OUTPUT (JSON):
{
  "fatigue": 45,
  "style_multiplier": 1.1,
  "lineup_score": 90,
  "reasoning": "Team A heeft 2 dagen rust minder, maar Team B speelt met een B-keeper."
}

```

### 3.2 De AI Service

* **Bestand:** `domain/service/DeepChiService.kt`
* **Taak:**
1. Verzamel data uit `ApiSportsClient`.
2. Bouw de prompt.
3. Stuur naar DeepSeek API.
4. Parse JSON response naar `SimulationContext`.



---

## FASE 4: INTEGRIREN IN REKENMOTOREN

**Doel:** De AI-inzichten (Context) gebruiken om de wiskunde (Oracle/Tesseract) te beïnvloeden.

### 4.1 Tesseract Upgrade (Simulatie)

* **Bestand:** `domain/tesseract/TesseractEngine.kt`
* **Logica Aanpassing:**
* *Fatigue Impact:* Vermoeide teams scoren minder in de 2e helft.
* *Formule:* `Lambda2ndHalf = BaseLambda * (1 - (fatigueScore / 200))`
* *Style Impact:* `Lambda = Lambda * styleMatchup`



### 4.2 Mastermind Upgrade (Beslissing)

* **Bestand:** `domain/mastermind/MastermindEngine.kt`
* **Nieuwe Logica (Lineup Check):**
* Als `lineupStrength < 70` (Slechte opstelling) AND `Oracle` zegt Winst → **Degradeer naar HIGH RISK**.
* Als `fatigueScore > 80` (Uitgeput) AND `Oracle` zegt Winst → **Degradeer naar GEEL**.



---

## FASE 5: OUTPUT & UI SIGNALEN

**Doel:** De gebruiker laten zien *waarom* een keuze is gemaakt.

### 5.1 Resultaat Object

* **Bestand:** `domain/model/PredictionResult.kt`
* **Toevoegen:** `aiReasoning` veld.

### 5.2 Console/Log Output (Voor testen)

Zorg dat Cline een log print na analyse:

```text
[VOORSPELLINGEN-X RAPPORT]
Wedstrijd: Ajax vs Feyenoord
---------------------------
Oracle Power: 140 vs 135
DeepChi Context:
  - Fatigue: Ajax (Laag), Feyenoord (Hoog - midweek gespeeld)
  - Style: Feyenoord countert sterk tegen Ajax balbezit (1.2x multiplier)
  - Lineup: Beide teams op volle sterkte.
---------------------------
TESSERACT: 10,000 sims gedraaid met Style Multiplier.
UITSLAG: Feyenoord Winst (Value Bet)
REDEN: Oracle zegt Gelijkspel, maar DeepChi ziet vermoeidheid en tactisch voordeel.

```

---

### Instructie voor Gebruik:

Geef je coding assistant (Cline) de opdracht per fase.
Bijvoorbeeld: *"Cline, start met Fase 1. Verwijder de NewsImpactAnalyzer en update de SimulationContext data class zoals beschreven in het plan."*
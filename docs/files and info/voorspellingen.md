Ik heb nu alle systemen en hun berekeningen in kaart gebracht. Hier is een volledig overzicht:

## 🔧 BEREKENINGSSYSTEMEN - BESTANDEN EN FUNCTIES

### 1. ORACLE ("Hard Reality")

__Bestand:__ `domain/prediction/PowerRankCalculator.kt`

__Berekening:__

```kotlin
Power Score = (100 - (Rank × 3)) + (PointsPerGame × 10) + (GoalDiffPerGame × 5)
Home Bonus = +10 punten
Delta = HomePower - AwayPower
```

__Beslissingslogica:__

- Delta < -30 → Uit Winst (0-3) met 90% zekerheid
- Delta < -15 → Uit Winst (1-2) met 75% zekerheid
- Delta > +30 → Thuis Winst (3-0) met 90% zekerheid
- Delta > +15 → Thuis Winst (2-1) met 75% zekerheid
- Anders → Gelijkspel (1-1) met 60% zekerheid

---

### 2. TESSERACT (Monte Carlo Simulatie)

__Bestand:__ `domain/tesseract/TesseractEngine.kt`

__Berekening 1: Power → Lambda (Expected Goals)__

```kotlin
lambda = (powerScore / 100.0) × 3.2
// 0 power → 0 goals
// 100 power → 3.2 goals max
```

__Berekening 2: Form Noise (Simuleert dagelijkse vorm)__

```kotlin
noiseFactor = Random.nextDouble(0.9, 1.1)
adjustedLambda = baseLambda × noiseFactor
```

__Berekening 3: Poisson Verdeling (Knuth Algoritme)__

```kotlin
fun poisson(lambda: Double): Int {
    val L = exp(-lambda)
    var k = 0
    var p = 1.0
    
    do {
        k++
        p *= Random.nextDouble()
    } while (p > L)
    
    return k - 1
}
```

__Berekening 4: ChiChi Integratie (Phase 2)__

```kotlin
// Fitness Impact
fitnessMultiplier = 0.5 + ((fitness / 100.0) × (1.0 - 0.5))
effectiveLambda = baseLambda × fitnessMultiplier
// 0 fitness = 50% reductie
// 100 fitness = volle lambda

// Distraction Impact
distractionImpact = (distraction / 200.0) × 0.5
minNoise = 0.9
maxNoise = 0.9 + distractionImpact
// 0 afleiding = 0.9-1.1 range
// 100 afleiding = 0.9-1.4 range (onvoorspelbaarder)
```

__Monte Carlo Loop (10,000 simulaties):__

- Tellen: homeWins, draws, awayWins
- Tellen: BTTS (Both Teams To Score), Over 2.5
- Track: Score distributie (bijv. 2-1: 1243 keer)

---

### 3. CHICHI (Contextuele Analyse)

__Bestand:__ `domain/model/SimulationContext.kt` + `domain/service/NewsImpactAnalyzer.kt`

__Nieuws Impact Analyzer Flow:__

1. Fetch RSS feeds voor beide teams
2. Analyseer met DeepSeek AI (feature engineering)
3. Extract kwantitatieve scores (0-100)

__AI Prompts:__

- __Feature Engineering Prompt:__ Nieuws → Attack/Defense modifiers (0.5-1.5)
- __Simulation Context Prompt:__ Nieuws → Distraction/Fitness scores
- __Oracle Validation Prompt:__ Nieuws → Validate statistische voorspelling

__Berekeningen:__

```kotlin
// Distraction Index (0-100)
0-20: Volledig gefocust (geen afleiding)
21-40: Lichte afleiding
41-60: Gemiddelde afleiding
61-80: Hoge afleiding
81-100: Catastrofaal (crisis, chaos)

// Fitness Level (0-100)
0-20: Catastrofaal (veel blessures)
21-40: Zeer laag
41-60: Gemiddeld
61-80: Goed
81-100: Uitstekend (top fitheid)

// Team Strength Modifiers (0.5-1.5)
0.50-0.65: Catastrofaal (3+ sleutelspelers weg)
0.66-0.79: Ernstig (2 sleutelspelers weg)
0.80-0.89: Matig (1 sleutelspeler twijfelachtig)
0.90-0.95: Licht
0.96-1.04: Neutraal (standaard verwachting)
1.05-1.10: Licht positief (motivatie, thuispubliek)
1.11-1.20: Matig positief (sterke vorm)
1.21-1.35: Sterk positief (perfecte omstandigheden)
1.36-1.50: Extreem positief
```

---

### 4. MASTERMIND (Decision Engine)

__Bestand:__ `domain/mastermind/MastermindEngine.kt`

__Decision Tree:__

1. __BANKER (Zekerheidje)__

```kotlin
oracle.confidence >= 70% &&
oracleWinner == tesseractWinner &&
fitness >= 70
→ GROEN signaal
```

2. __HIGH RISK__

```kotlin
oracleWinner != tesseractWinner ||
oracle.confidence < 50%
→ GEEL/ROOD signaal
```

3. __GOALS FESTIVAL__

```kotlin
tesseract.over2_5Probability > 65% &&
tesseract.bttsProbability > 60%
→ GROEN signaal (Over 2.5 & BTTS)
```

4. __TACTICAL DUEL__

```kotlin
powerDifference < 20 &&
oracle.confidence in 50..70%
→ GEEL signaal (Gelijk of ±1 goal)
```

5. __DEFENSIVE BATTLE__

```kotlin
tesseract.under2_5Probability > 70% &&
tesseract.bttsProbability < 40%
→ GEEL signaal (Under 2.5)
```

6. __VALUE BET (default)__

```kotlin
bestProbability = max(homeWin, draw, awayWin)
→ GROEN signaal
```

__LLMGRADE Enhancement:__

```kotlin
// Context Factors Integration
mostImpactfulFactor → Pas confidence aan
outlierScenarios → Risk level bepalen
confidenceAdjustment = -10% to +10%

// Color Logic based on Risk
RiskLevel.VERY_HIGH/HIGH → ROOD
RiskLevel.MEDIUM → GEEL
RiskLevel.LOW → Huidige of GEEL (upgrade)
```

---

## 📊 COMPLETE DATA FLOW MET WIRING

```javascript
┌─────────────────────────────────────────────────────────────────┐
│              API DATA SOURCES                          │
│  • Fixtures, Standings, H2H, Live Matches     │
│  • RSS News Feeds (Team News)                      │
└──────────────────────┬──────────────────────────────────┘
                       │
         ┌─────────────┼─────────────┐
         │             │             │
         ▼             ▼             ▼
   ┌─────────┐  ┌──────────┐  ┌─────────────┐
   │ ORACLE  │  │ CHICHI   │  │  TESSERACT  │
   │         │  │          │  │             │
   │PowerRank│  │ RSS+AI   │  │ Monte Carlo  │
   │Calculator│  │Analysis   │  │ (10,000x)   │
   │         │  │          │  │             │
   │ 0-200   │  │ 0-100    │  │ Probabilities│
   │ score   │  │ scores    │  │ % outcomes   │
   └────┬────┘  └─────┬────┘  └──────┬──────┘
        │              │                │
        │   Distraction & Fitness
        │   (from ChiChi)
        │              │                │
        └──────┬───────┴────────────────┘
               │
               ▼
        ┌──────────────┐
        │  MASTERMIND   │
        │  Decision     │
        │  Engine       │
        │               │
        │  • Banker     │
        │  • High Risk  │
        │  • Goals Fest │
        │  • Tactical   │
        │  • Defensive  │
        │  • Value Bet  │
        └──────┬───────┘
               │
               ▼
        ┌──────────────┐
        │ UI SIGNAL     │
        │ • Green/Yellow/│
        │   Red         │
        │ • Confidence  │
        │ • Recommen-   │
        │   dation     │
        └──────────────┘
```

## 📊 OVERZICHT VAN DE VIER SYSTEMEN

### 1. __ORACLE__ - "Hard Reality" Motor

__Locatie:__ `domain/prediction/PowerRankCalculator.kt`

__Doel:__ Deterministische, feit-gebaseerde voorspellingen

__Algoritme:__

```javascript
Power Score = (100 - (Rank × 3)) + (PointsPerGame × 10) + (GoalDiffPerGame × 5)
Home Bonus: +10 voor thuis team
Delta = HomePower - AwayPower
```

__Beslissingsregels:__

- Delta < -30: Sterke Uit Winst (0-3) - 90% zekerheid
- Delta < -15: Uit Winst (1-2) - 75% zekerheid
- Delta > +30: Sterke Thuis Winst (3-0) - 90% zekerheid
- Delta > +15: Thuis Winst (2-1) - 75% zekerheid
- Anders: Gelijkspel (1-1) - 60% zekerheid

__Output:__ `OracleAnalysis` (prediction, confidence, reasoning, power scores 0-200)

---

### 2. __TESSERACT__ (Terrasect) - Monte Carlo Simulatie

__Locatie:__ `domain/tesseract/TesseractEngine.kt`

__Doel:__ Probabilistische voorspellingen via Poisson process

__Algoritme:__

1. Convert power scores (0-100) naar expected goals (lambda)

- 0 power → 0 goals
   - 100 power → 3.2 goals max

2. Apply form noise (0.9-1.1 multiplier)

3. Simuleer 10,000 keer met Poisson verdeling (Knuth algoritme)

4. Aggregeer resultaten

__ChiChi Integratie (Phase 2):__

- Fitness Impact: `effectiveLambda = baseLambda × (fitness / 100)`

- 0 fitness = 50% reductie
  - 100 fitness = volle lambda

- Distraction Impact: Wijdere noise range bij hoge afleiding

- 0 afleiding = 0.9-1.1 range
  - 100 afleiding = 0.9-1.4 range (onvoorspelbaarder)

__Output:__ `TesseractResult`

- home/draw/away win probabilities
- BTTS (Both Teams To Score) probability
- Over 2.5 probability
- Most likely score
- Top 3 score distribution

---

### 3. __CHICHI__ - Contextuele Enriching

__Locatie:__ Verweven door systeem (SimulationContext)

__Doel:__ Psychologische en fysieke factoren kwantificeren

__Input:__ Team-specifieke nieuwsfeeds (RSS) → AI Analyse

__Quantitatieve Variabelen (`SimulationContext`):__

- `homeDistraction` (0-100): Hoog = chaos, onvoorspelbaar
- `awayDistraction` (0-100)
- `homeFitness` (0-100): 100 = piekconditie
- `awayFitness` (0-100)
- Attack/Defense modifiers (0.0+)

__Nieuws Analyse Flow:__

1. Fetch team-specifieke RSS feeds
2. Analyseer met DeepSeek AI
3. Extract kwantitatieve scores
4. Inject in Tesseract simulaties

__Impact:__

- Verandert lambda in Tesseract
- Beïnvloed noise range
- Kan Oracle voorspelling aanpassen

---

### 4. __MASTERMIND__ - De "Gouden Tip" Beslissingsmotor

__Locatie:__ `domain/mastermind/MastermindEngine.kt`

__Doel:__ Eindbeslissing nemen gebaseerd op alle data

__Decision Tree:__

1. __BANKER__ (Zekerheidje)

- Oracle confidence > 70%
   - Oracle & Tesseract akkoord over winnaar
   - Fitness > 70 (indien beschikbaar)
   - Signaal: GROEN

2. __HIGH RISK__

- Oracle & Tesseract oneens
   - OF Oracle confidence < 50%
   - Signaal: GEEL/ROOD

3. __GOALS FESTIVAL__

- Over 2.5 probability > 65%
   - BTTS probability > 60%
   - Signaal: GROEN (Over 2.5 & BTTS)

4. __TACTICAL DUEL__

- Power verschil < 20 punten
   - Oracle confidence 50-70%
   - Signaal: GEEL (Gelijk of ±1 goal)

5. __DEFENSIVE BATTLE__

- Under 2.5 probability > 70%
   - BTTS probability < 40%
   - Signaal: GEEL (Under 2.5)

6. __VALUE BET__ (default)

- Beste waarde uit Tesseract probabilities
   - Signaal: GROEN

__LLMGRADE Enhancement:__

- Context factoren integreren
- Uitschieterscenario's detecteren
- Vertrouwen aanpassen op basis van ongestructureerde data
- Risico niveau bepalen

__Output:__ `MastermindSignal`

- Title, Description, Color (GROEN/GEEL/ROOD)
- Confidence score
- Recommendation
- ScenarioType

---

## 🔗 WIRING / DATA FLOW

```javascript
┌─────────────────────────────────────────────────────────────┐
│                    MATCH DATA INPUT                      │
│  (Standings, H2H, Fixtures, News)                 │
└───────────────────────┬─────────────────────────────────┘
                        │
         ┌──────────────┼──────────────┐
         │              │              │
         ▼              ▼              ▼
    ┌─────────┐   ┌──────────┐  ┌────────────┐
    │ ORACLE  │   │ CHICHI   │  │  TESSERACT │
    │ "Hard   │   │ Context  │  │  Monte     │
    │ Reality"│   │ Analyzer  │  │  Carlo     │
    └────┬────┘   └────┬─────┘  └─────┬──────┘
         │              │               │
         │              │               │
         └──────┬───────┴───────────────┘
                │
                ▼
         ┌──────────────┐
         │  MASTERMIND   │
         │  "Gouden     │
         │  Tip"        │
         └──────┬───────┘
                │
                ▼
         ┌──────────────┐
         │  UI SIGNAL    │
         │  (Green/Yellow│
         │   /Red)       │
         └──────────────┘
```
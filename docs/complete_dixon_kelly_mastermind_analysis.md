# COMPLETE DIXON-COLES & KELLY METHODES + MASTERMIND AI ANALYSE

## 🎯 OVERZICHT: MATCHMIND AI 2.0 ARCHITECTUUR

MatchMind AI 2.0 combineert drie geavanceerde methodologieën voor sportvoorspellingen:

1. **Dixon-Coles Model** - Statistische voorspellingsmotor met xG-integratie
2. **Kelly Criterion** - Risicobeheer en optimal betting sizing
3. **MasterMind AI** - Feature engineering en scenario analyse

---

## 1. DIXON-COLES MODEL IMPLEMENTATIE

### 1.1 Core Component: EnhancedScorePredictor.kt
**Locatie:** `app/src/main/java/com/Lyno/matchmindai/domain/service/EnhancedScorePredictor.kt`

### 1.2 Wiskundige Basis
Het Dixon-Coles model is een verbeterde Poisson-verdeling die rekening houdt met:
- **Teamsterktes** (aanval en verdediging)
- **Thuisspelvoordeel** (home advantage)
- **Correlatie tussen lage scores** (τ-factor)
- **Expected Goals (xG)** i.p.v. werkelijke goals

### 1.3 Model Parameters
```kotlin
private const val DEFAULT_RHO = -0.13  // Correlatie parameter voor lage scores
private const val MIN_MATCHES_FOR_PREDICTION = 10
private const val DEFAULT_HOME_AVG_GOALS = 1.55  // Globale professionele gemiddelden
private const val DEFAULT_AWAY_AVG_GOALS = 1.25
```

### 1.4 Dixon-Coles Formules

#### Expected Goals Berekenen:
```
λ_home = attack_home × defense_away × home_advantage
λ_away = attack_away × defense_home
```

#### τ-factor voor Lage Scores:
```kotlin
private fun calculateTau(x: Int, y: Int, lambdaHome: Double, lambdaAway: Double, rho: Double): Double {
    return when {
        x == 0 && y == 0 -> 1.0 - lambdaHome * lambdaAway * rho
        x == 0 && y == 1 -> 1.0 + lambdaHome * rho
        x == 1 && y == 0 -> 1.0 + lambdaAway * rho
        x == 1 && y == 1 -> 1.0 - rho
        else -> 1.0
    }
}
```

#### Probability Matrix:
```kotlin
for (i in 0..4) { // Home goals
    for (j in 0..4) { // Away goals
        val poissonProb = poissonProbability(i, lambdaHome) * poissonProbability(j, lambdaAway)
        val tau = calculateTau(i, j, lambdaHome, lambdaAway, rho)
        val adjustedProb = poissonProb * tau
        
        when {
            i > j -> homeWinProb += adjustedProb
            i == j -> drawProb += adjustedProb
            i < j -> awayWinProb += adjustedProb
        }
    }
}
```

### 1.5 xG-Integratie
- **xG-gewogen data** i.p.v. werkelijke goals
- **Adaptive time decay** - recente wedstrijden zwaarder gewogen
- **League averages** als fallback bij onvoldoende data

### 1.6 Confidence Calibration
```kotlin
private fun calculatePredictionConfidence(
    baseTeamStrength: BaseTeamStrength,
    adjustedTeamStrength: AdjustedTeamStrength,
    homeTeamFixtures: List<HistoricalFixture>,
    awayTeamFixtures: List<HistoricalFixture>
): Double {
    // Base confidence from data quality
    var confidence = baseTeamStrength.calculationConfidence
    
    // Adjust based on fixture count
    val totalMatches = homeTeamFixtures.size + awayTeamFixtures.size
    val fixtureConfidence = when {
        totalMatches >= 50 -> 0.9
        totalMatches >= 30 -> 0.7
        totalMatches >= 15 -> 0.5
        totalMatches >= 5 -> 0.3
        else -> 0.1
    }
    
    confidence = (confidence + fixtureConfidence) / 2.0
    
    // Adjust based on AI modifiers
    adjustedTeamStrength.modifiers?.let { modifiers ->
        if (modifiers.hasMeaningfulImpact) {
            if (!modifiers.isExtreme && modifiers.confidence >= 0.4) {
                confidence = (confidence + modifiers.confidence) / 2.0
            } else if (modifiers.isExtreme) {
                confidence *= 0.8
            }
        }
    }
    
    return confidence.coerceIn(0.1, 1.0)
}
```

---

## 2. KELLY CRITERION IMPLEMENTATIE

### 2.1 Core Component: EnhancedCalculateKellyUseCase.kt
**Locatie:** `app/src/main/java/com/Lyno/matchmindai/domain/usecase/EnhancedCalculateKellyUseCase.kt`

### 2.2 Kelly Parameters
```kotlin
private const val FRACTIONAL_KELLY_FACTOR = 0.25  // 25% van volledige Kelly
private const val MAX_STAKE_PERCENTAGE = 0.05    // Max 5% van bankroll
private const val MIN_STAKE_PERCENTAGE = 0.005   // Min 0.5% van bankroll
private const val MIN_VALUE_EDGE = 0.02          // Min 2% edge
```

### 2.3 Kelly Formule
```
f = (bp - q) / b

Waar:
- f = fractie van bankroll om in te zetten
- b = decimal odds - 1
- p = winst waarschijnlijkheid (onze voorspelling)
- q = verlies waarschijnlijkheid (1 - p)
```

### 2.4 Fractional Kelly Implementatie
```kotlin
private fun calculateFractionalKellyForMarket(
    probability: Double,
    odds: Double?,
    confidence: Double
): Double? {
    if (probability <= 0.0 || probability >= 1.0 || odds == null || odds <= 1.0) {
        return null
    }
    
    // Calculate full Kelly
    val fullKelly = calculateKellyFraction(probability, odds)
    
    // Apply fractional Kelly (25% of full Kelly)
    val fractionalKelly = fullKelly * FRACTIONAL_KELLY_FACTOR
    
    // Adjust based on confidence
    val confidenceAdjustedKelly = fractionalKelly * confidence
    
    // Apply bounds
    return confidenceAdjustedKelly.coerceIn(0.0, MAX_STAKE_PERCENTAGE)
}
```

### 2.5 Value Edge Calculation
```kotlin
private fun calculateValueEdge(
    ourProbability: Double,
    bookmakerProbability: Double?,
    closingLineProbability: Double?
): Double {
    // Use closing line if available, otherwise use opening line
    val marketProbability = closingLineProbability ?: bookmakerProbability ?: return 0.0
    
    // Calculate edge: our probability - market probability
    val edge = ourProbability - marketProbability
    
    // Only return positive edges
    return edge.coerceAtLeast(0.0)
}
```

### 2.6 Risk Level Classification
```kotlin
private fun calculateRiskLevel(
    bestKelly: Double?,
    confidence: Double,
    valueEdge: Double
): RiskLevel {
    if (bestKelly == null || bestKelly <= 0.0) {
        return RiskLevel.LOW
    }
    
    // Calculate risk score (0-100)
    var riskScore = 0
    
    // Kelly contribution (0-40)
    riskScore += when {
        bestKelly >= 0.02 -> 40
        bestKelly >= 0.01 -> 30
        bestKelly >= 0.005 -> 20
        else -> 10
    }
    
    // Confidence contribution (0-30)
    riskScore += when {
        confidence < 0.3 -> 30
        confidence < 0.6 -> 20
        else -> 10
    }
    
    // Value edge contribution (0-30)
    riskScore += when {
        valueEdge >= 0.10 -> 10  // HIGH_VALUE_EDGE
        valueEdge >= 0.05 -> 20  // MEDIUM_VALUE_EDGE
        valueEdge >= 0.02 -> 30  // MIN_VALUE_EDGE
        else -> 30
    }
    
    return when {
        riskScore >= 80 -> RiskLevel.VERY_HIGH
        riskScore >= 60 -> RiskLevel.HIGH
        riskScore >= 40 -> RiskLevel.MEDIUM
        else -> RiskLevel.LOW
    }
}
```

### 2.7 KellyResult Model
```kotlin
@Serializable
data class KellyResult(
    val fixtureId: Int,
    val homeTeam: String,
    val awayTeam: String,
    
    // Kelly calculations for each market
    val homeWinKelly: Double?,
    val drawKelly: Double?,
    val awayWinKelly: Double?,
    
    // Value scores (0-10)
    val homeWinValueScore: Int,
    val drawValueScore: Int,
    val awayWinValueScore: Int,
    
    // Best value bet
    val bestValueBet: ValueBet,
    
    // Risk assessment
    val riskLevel: RiskLevel,
    val recommendedStakePercentage: Double, // 0.0 to 1.0 (0% to 100%)
    
    // Analysis
    val analysis: String,
    val confidence: Int // 0-100
)
```

---

## 3. MASTERMIND AI ANALYSE SYSTEM

### 3.1 Core Component: NewsImpactAnalyzer.kt
**Locatie:** `app/src/main/java/com/Lyno/matchmindai/domain/service/NewsImpactAnalyzer.kt`

### 3.2 Feature Engineering Strategie
MasterMind AI gebruikt **feature engineering** i.p.v. directe voorspellingen:
- **Input**: Kwalitatief nieuws (blessures, tactieken, psychologische factoren)
- **Output**: Kwantitatieve modifiers voor Dixon-Coles parameters
- **Focus**: Teamsterkte aanpassingen (0.5-1.5 range)

### 3.3 Geoptimaliseerde System Prompt
```kotlin
const val FEATURE_ENGINEERING_SYSTEM_PROMPT = """
JIJ BENT: Een Senior Data Scientist gespecialiseerd in voetbalmodellen.
DOEL: Vertaal kwalitatief nieuws naar kwantitatieve model-modifiers.

INPUT DATA:
1. Teamnamen & Competitie
2. Basis Voorspelling (Win/Gelijk/Verlies %)
3. Nieuws Snippets (Blessures, Ruzies, Transfers)

OUTPUT REGELS (JSON):
- Modifiers moeten tussen 0.5 (catastrofaal) en 1.5 (perfect) liggen.
- 1.0 is neutraal (geen nieuws = 1.0).
- 'confidence': Hoe zeker ben je van het nieuws? (0.0 - 1.0).
- 'chaos_factor': 0.0 (Saai) tot 1.0 (Totale chaos/Derby).

RANGE GUIDE:
- 0.85 - 0.95: Belangrijke speler twijfelachtig/lichte blessure.
- 0.70 - 0.80: Sterspeler definitief afwezig.
- < 0.70: Meerdere sleutelspelers weg / Team in crisis.
- > 1.10: Team in topvorm / Manager 'bounce' effect.

BELANGRIJK:
- Wees CONSERVATIEF. Bij twijfel, houd modifiers dicht bij 1.0.
- Hallucineer GEEN blessures die niet in de snippets staan.
"""
```

### 3.4 Modifier Bounds en Validatie
```kotlin
// Modifier bounds for sanity checking
private const val MIN_MODIFIER = 0.5
private const val MAX_MODIFIER = 1.5
private const val DEFAULT_MODIFIER = 1.0

// Confidence thresholds
private const val HIGH_CONFIDENCE_THRESHOLD = 0.7
private const val MEDIUM_CONFIDENCE_THRESHOLD = 0.4
```

### 3.5 NewsImpactModifiers Data Class
```kotlin
data class NewsImpactModifiers(
    val homeAttackMod: Double,      // 0.5-1.5
    val homeDefenseMod: Double,     // 0.5-1.5
    val awayAttackMod: Double,      // 0.5-1.5
    val awayDefenseMod: Double,     // 0.5-1.5
    val confidence: Double,         // 0.0-1.0
    val reasoning: String,          // NL uitleg
    val chaosFactor: Double,        // 0.0-1.0
    val newsRelevance: Double       // 0.0-1.0
) {
    val hasMeaningfulImpact: Boolean
        get() = homeAttackMod != 1.0 || homeDefenseMod != 1.0 || 
                awayAttackMod != 1.0 || awayDefenseMod != 1.0
    
    val isExtreme: Boolean
        get() = homeAttackMod < 0.7 || homeAttackMod > 1.3 ||
                homeDefenseMod < 0.7 || homeDefenseMod > 1.3 ||
                awayAttackMod < 0.7 || awayAttackMod > 1.3 ||
                awayDefenseMod < 0.7 || awayDefenseMod > 1.3
}
```

### 3.6 Tool Orchestration: ToolOrchestrator.kt
**Locatie:** `app/src/main/java/com/Lyno/matchmindai/data/ai/ToolOrchestrator.kt`

#### 9 Tools voor Complete Analyse:
1. **get_fixtures** - Wedstrijden, schema's, kalenders
2. **tavily_search** - Nieuws, blessures, transfers
3. **get_live_scores** - Live scores en standen
4. **get_match_prediction** - Voorspellingen en analyse
5. **get_standings** - Klassementen en ranglijsten
6. **get_best_odds** - Wedkansen met veiligheidsbeoordeling
7. **get_injuries** - Speler blessures en afwezigheden
8. **get_predictions** - Gedetailleerde voorspellingen
9. **get_odds** - Match-specifieke odds

#### "Kenner aan de Bar" Persona:
- **Tone**: Informeel, direct, feitelijk onderbouwd
- **Taal**: Nederlands, gevat, geen robot-taal
- **Assertiviteit**: Direct tools aanroepen, geen aankondigingen

#### Tool Decision Logic:
```kotlin
// FEITEN & CIJFERS (GEBRUIK API-SPORTS):
// - Stand, Uitslagen, Programma, Opstellingen
// - ACTIE: Gebruik get_fixtures, get_standings, get_live_scores
// - VERBODEN: Gebruik hiervoor NOOIT tavily_search

// NIEUWS & CONTEXT (GEBRUIK TAVILY):
// - Blessures, Reden van afwezigheid, Geruchten
// - ACTIE: Gebruik tavily_search
// - TIP: Combineer dit met API data

// VOORSPELLINGEN (COMBINATIE):
// - 'Wie wint er?'
// - ACTIE: Haal EERST de feiten (API), zoek DAN naar blessurenieuws (Tavily)
```

---

## 4. COMPLETE INTEGRATIE PIPELINE

### 4.1 Data Flow Architectuur
```
1. MATCH DETAIL → Historical Data → xG Integration
   ↓
2. BASE TEAM STRENGTH → NewsImpact Modifiers → Adjusted Strength
   ↓
3. DIXON-COLES MODEL → Probabilities → Expected Goals
   ↓
4. KELLY CRITERION → Value Edge → Stake Recommendation
   ↓
5. MASTERMIND AI → Scenario Generation → NL Report
```

### 4.2 Wiskundige Koppeling: Dixon-Coles × NewsImpact
```
λ_home_adj = λ_home_base × HomeAttackMod × AwayDefenseMod
λ_away_adj = λ_away_base × AwayAttackMod × HomeDefenseMod
```

**Implementatie in EnhancedScorePredictor:**
```kotlin
// 2. Apply AI modifiers if available and valid
val adjustedTeamStrength = if (modifiers != null && modifiers.isValid) {
    baseTeamStrength.applyModifiers(modifiers)
} else {
    AdjustedTeamStrength(
        homeAttackStrength = baseTeamStrength.homeAttackStrength,
        homeDefenseStrength = baseTeamStrength.homeDefenseStrength,
        awayAttackStrength = baseTeamStrength.awayAttackStrength,
        awayDefenseStrength = baseTeamStrength.awayDefenseStrength,
        homeAdvantage = baseTeamStrength.homeAdvantage,
        leagueAverageHomeGoals = baseTeamStrength.leagueAverageHomeGoals,
        leagueAverageAwayGoals = baseTeamStrength.leagueAverageAwayGoals,
        baseStrength = baseTeamStrength,
        modifiers = modifiers ?: createDefaultModifiers()
    )
}
```

### 4.3 EnhancedPrediction Data Flow
```kotlin
data class EnhancedPrediction(
    val fixtureId: Int,
    val homeTeam: String,
    val awayTeam: String,
    val homeWinProbability: Double,
    val drawProbability: Double,
    val awayWinProbability: Double,
    val expectedGoalsHome: Double,
    val expectedGoalsAway: Double,
    val valueEdge: Double,
    val kellyStake: Double,
    val

# 📊 Prestatie-Analyse Dashboard - Project Kaptigun

## 🎯 Overzicht

Het Prestatie-Analyse Dashboard is een geavanceerd tactisch analyse tool dat diepgaande inzichten biedt in voetbalwedstrijden. Het dashboard is opgedeeld in drie duidelijke secties die samen een compleet beeld geven van de wedstrijddynamiek.

**Doel:** Gebruikers voorzien van data-gedreven inzichten die verder gaan dan traditionele statistieken, met focus op onderliggende prestaties (xG), efficiëntie en sentiment.

## 🏗️ Architectuur

### Clean Architecture Flow
```
API Layer (Data) → Repository → Domain Models → ViewModel → UI Components
```

### Componenten Overzicht
1. **Domain Models** (`KaptigunAnalysis.kt`)
   - `HeadToHeadDuel` - Onderlinge duels met xG analyse
   - `TeamRecentForm` - Recente vorm met efficiency icons
   - `DeepStatsComparison` - Diepgaande statistieken

2. **Repository Layer**
   - `KaptigunRepository` - Interface
   - `KaptigunRepositoryImpl` - Implementatie met caching (5 min TTL)

3. **UI Components**
   - `HeadToHeadCard` - Sectie 1: Directe Duels
   - `FormComparisonCard` - Sectie 2: Recente Vorm
   - `DeepStatsChart` - Sectie 3: Diepgaande Statistieken
   - `AnalysisTab` - Hoofd container component

---

## 📈 Sectie 1: Directe Duels (Head-to-Head)

### Data Bronnen
- **API Endpoint:** `fixtures/headtohead?h2h={team1_id}-{team2_id}`
- **Filter:** Laatste 5 onderlinge duels
- **Data Velden:** Score, xG, shots, possession, datum

### Tesseract Twist: xG Integratie
Naast de score tonen we de Expected Goals (xG) van die wedstrijd:
```
Voorbeeld: 2-1 (1.2 - 1.8)
```

### Performance Labels (Kleurcode)
| Resultaat | xG Vergelijking | Label | Kleur | Betekenis |
|-----------|-----------------|-------|-------|-----------|
| Win | Hogere xG | DOMINANT | 🟢 Groen | Verdiente overwinning |
| Win | Lagere xG | LUCKY | 🟠 Oranje | Gelukkige overwinning |
| Loss | Hogere xG | UNLUCKY | 🔴 Rood | Ongelukkig verlies |
| Gelijk | - | NEUTRAL | ⚪ Grijs | Gebalanceerd |

### Implementatie Logica
```kotlin
val performanceLabel: PerformanceLabel
    get() {
        val isHomeWin = homeScore > awayScore
        val isAwayWin = awayScore > homeScore
        val xgDifference = homeXg - awayXg
        
        return when {
            isHomeWin && xgDifference > 0.3 -> PerformanceLabel.DOMINANT
            isHomeWin && xgDifference < -0.3 -> PerformanceLabel.LUCKY
            isAwayWin && xgDifference < -0.3 -> PerformanceLabel.DOMINANT
            isAwayWin && xgDifference > 0.3 -> PerformanceLabel.LUCKY
            !isHomeWin && !isAwayWin && xgDifference > 0.5 -> PerformanceLabel.UNLUCKY
            !isHomeWin && !isAwayWin && xgDifference < -0.5 -> PerformanceLabel.UNLUCKY
            else -> PerformanceLabel.NEUTRAL
        }
    }
```

---

## 📊 Sectie 2: Recente Vorm (Laatste 5 Wedstrijden)

### Data Bronnen
- **API Endpoint:** `fixtures?team={team_id}&last=5`
- **Per Team:** Aparte calls voor thuis- en uitploeg
- **Data Velden:** Tegenstander, datum, score, xG, shots

### Visuele Weergave
```
Team A                          Team B
W - W - D - L - W               L - W - D - W - W
🟢 🟢 🟠 🔴 🟢                  🔴 🟢 🟠 🟢 🟢
```

### Efficiency Icons (Tesseract Twist)
| Doelpunten vs xG | Verschil | Icon | Betekenis |
|------------------|----------|------|-----------|
| Goals > xG | > +0.5 | ↑ | Klinisch (efficiënt/gelukkig) |
| Goals ≈ xG | ±0.5 | → | Gebalanceerd |
| Goals < xG | < -0.5 | ↓ | Inefficiënt (pech) |

### Implementatie Logica
```kotlin
val efficiencyIcon: EfficiencyIcon
    get() {
        val goalDifference = goals - xg
        return when {
            goalDifference > 0.5 -> EfficiencyIcon.CLINICAL
            goalDifference < -0.5 -> EfficiencyIcon.INEFFICIENT
            else -> EfficiencyIcon.BALANCED
        }
    }
```

### Side-by-Side Vergelijking
```
Team A (Thuis)                  Team B (Uit)
══════════════════════════════════════════════════════
Tegenstander: Barcelona        Tegenstander: Real Madrid
Datum: 15/12/2024              Datum: 14/12/2024
Resultaat: W                   Resultaat: L
Score: 2-1                     Score: 0-2
xG vs Goals: ↑ 2.1 vs 2        xG vs Goals: ↓ 1.8 vs 0
```

---

## 📊 Sectie 3: Diepgaande Prestatievergelijking

### Data Bronnen
- **API Endpoint:** `fixtures/statistics?fixture={fixture_id}`
- **Aggregatie:** Gemiddelden over laatste 5 wedstrijden per team
- **Data Velden:** xG, possession, shots, passes, defensive actions

### Statistieken Overzicht
| Statistiek | Team A | Team B | Uitleg |
|------------|--------|--------|--------|
| **Gem. xG** | 1.85 | 1.45 | Wie creëert de beste kansen? |
| **Gem. xG Tegen** | 0.92 | 1.10 | Wie is kwetsbaar in verdediging? |
| **Gem. Balbezit** | 58% | 42% | Wie heeft initiatief? |
| **Gem. Schoten op Doel** | 5.4 | 3.8 | Efficiëntie in afwerken |
| **PPDA** | 8.2 | 12.5 | Pressing intensiteit |

### PPDA (Passes Per Defensive Action)
- **Lage score (<10):** Hoog en intensief druk zetten
- **Hoge score (>12):** Diep zakken, ruimte geven
- **Berekening:** `PPDA = passes / defensive_actions`

### Tesseract Sentiment Score
- **Bereik:** -1.0 (Chaos) tot +1.0 (Euforie)
- **Data Bron:** NewsImpactAnalyzer (Tavily AI analyse)
- **Betekenis:**
  - **Positief:** Goed nieuws, lage afleiding, goed moraal
  - **Negatief:** Blessures, onrust, negatieve berichtgeving

### Mood Indicators
| Score | Mood | Betekenis |
|-------|------|-----------|
| ≥ 0.7 | Euforie | Uitstekend team sentiment |
| ≥ 0.3 | Optimistisch | Positieve ontwikkelingen |
| ≥ -0.3 | Neutraal | Geen significant nieuws |
| ≥ -0.7 | Zorgen | Negatieve ontwikkelingen |
| < -0.7 | Chaos | Ernstige problemen |

### Bar Chart Implementatie
```kotlin
// Voorbeeld: xG bar chart
val homeXg = deepStats.avgXg.first
val awayXg = deepStats.avgXg.second
val maxValue = max(homeXg, awayXg) * 1.2 // 20% margin

// Home team bar width
val homeWidth = (homeXg / maxValue) * maxBarWidth
// Away team bar width  
val awayWidth = (awayXg / maxValue) * maxBarWidth
```

---

## 🔧 Implementatie Plan

### Stap 1: Dependency Injection Setup
```kotlin
// AppContainer.kt
val kaptigunRepository: KaptigunRepository by lazy {
    KaptigunRepositoryImpl(
        apiSportsApi = apiSportsApi,
        matchCacheManager = matchCacheManager,
        newsImpactAnalyzer = newsImpactAnalyzer
    )
}

val kaptigunViewModel: KaptigunViewModel by lazy {
    KaptigunViewModel(kaptigunRepository)
}
```

### Stap 2: Tab Navigatie in MatchDetailScreen
```kotlin
// MatchDetailScreen.kt
val tabs = remember(isMatchLive) {
    if (isMatchLive) {
        listOf("Live", "Details", "Oracle", "Tips", "Analyse", "Mastermind", "Verslag")
    } else {
        listOf("Details", "Oracle", "Tips", "Analyse", "Mastermind", "Verslag")
    }
}

// Tab content
when (tabs[selectedTab]) {
    "Analyse" -> AnalysisTab(matchDetail, kaptigunViewModel)
    // ... andere tabs
}
```

### Stap 3: Loading & Error States
```kotlin
// KaptigunViewModel.kt
sealed class KaptigunUiState {
    object Loading : KaptigunUiState()
    data class Success(val analysis: KaptigunAnalysis) : KaptigunUiState()
    data class Error(val message: String) : KaptigunUiState()
    object NoDataAvailable : KaptigunUiState()
}

// AnalysisTab.kt
when (uiState) {
    is KaptigunUiState.Loading -> LoadingIndicator()
    is KaptigunUiState.Error -> ErrorMessage(uiState.message)
    is KaptigunUiState.Success -> ShowAnalysis(uiState.analysis)
    is KaptigunUiState.NoDataAvailable -> NoDataMessage()
}
```

### Stap 4: Cache Management
```kotlin
// KaptigunRepositoryImpl.kt
private val analysisCache = mutableMapOf<Int, CachedAnalysis>()

data class CachedAnalysis(
    val analysis: KaptigunAnalysis,
    val timestamp: Long
)

private fun isCacheValid(fixtureId: Int): Boolean {
    val cached = analysisCache[fixtureId]
    return cached != null && 
           (System.currentTimeMillis() - cached.timestamp) < CACHE_TTL_MS
}
```

---

## 🧮 Tesseract Twists Details

### xG Berekening (Fallback)
Als API geen xG levert:
```kotlin
fun calculateExpectedGoals(): Double {
    val shotsOffTarget = totalShots - shotsOnTarget
    return (shotsOnTarget * 0.3) + (shotsOffTarget * 0.07)
}
```

### Performance Label Thresholds
- **DOMINANT/LUCKY:** xG verschil > 0.3
- **UNLUCKY:** xG verschil > 0.5 bij gelijkspel
- **NEUTRAL:** Alle andere gevallen

### Efficiency Icon Thresholds
- **CLINICAL:** Goals > xG + 0.5
- **INEFFICIENT:** Goals < xG - 0.5  
- **BALANCED:** Tussen -0.5 en +0.5

---

## 🧪 Testing & Validatie

### Unit Tests
```kotlin
class KaptigunAnalysisTest {
    @Test
    fun testPerformanceLabelDominant() {
        val duel = HeadToHeadDuel(
            homeScore = 2, awayScore = 1,
            homeXg = 2.5, awayXg = 0.8  // xG diff = 1.7 (> 0.3)
        )
        assertEquals(PerformanceLabel.DOMINANT, duel.performanceLabel)
    }
    
    @Test
    fun testEfficiencyIconClinical() {
        val match = FormMatch(
            goals = 3, xg = 2.0  // diff = 1.0 (> 0.5)
        )
        assertEquals(EfficiencyIcon.CLINICAL, match.efficiencyIcon)
    }
}
```

### UI Testing Checklist
- [ ] Head-to-head tabel toont laatste 5 duels
- [ ] Performance labels tonen correcte kleuren
- [ ] Vorm-streep toont W/G/V cirkels
- [ ] Efficiency icons tonen ↑/→/↓
- [ ] Bar charts tonen correcte verhoudingen
- [ ] Loading states werken
- [ ] Error handling toont foutmeldingen
- [ ] Cache werkt (snelle refresh)

### Edge Cases
1. **Geen data beschikbaar:** Toon "Geen analyse data beschikbaar"
2. **Minder dan 5 duels:** Toon beschikbare duels
3. **API errors:** Fallback naar cached data
4. **Network issues:** Offline modus met laatste cache

---

## 📱 UI/UX Richtlijnen

### Cyber-Minimalist Design
- **Kleuren:** Donker/Neon Green palette
- **Typography:** Clean, monospace voor data
- **Spacing:** Ruim gebruik van whitespace
- **Icons:** Minimalistische icon set

### Responsive Design
- **Mobile:** Verticale scroll, compacte tabellen
- **Tablet:** Meer side-by-side vergelijkingen
- **Desktop:** Volledig dashboard in één view

### Accessibility
- **Kleur contrast:** Minimaal 4.5:1 ratio
- **Screen readers:** Alt text voor alle icons
- **Focus states:** Duidelijke focus indicators
- **Font size:** Minimaal 14sp voor leesbaarheid

---

## 🔄 Integratie met Bestaande Systeem

### MatchDetailScreen Flow
```
User opens match detail
    ↓
Load Kaptigun analysis (parallel met andere data)
    ↓
Show "Analyse" tab in tab row
    ↓
User clicks "Analyse" tab
    ↓
Show loading → success/error state
    ↓
Display 3-section dashboard
```

### Data Synchronisatie
- **Real-time updates:** Geen (statische analyse)
- **Cache invalidatie:** Bij nieuwe wedstrijd data
- **Background refresh:** Elke 5 minuten bij actieve tab

---

## 🚀 Prestatie Optimalisaties

### Caching Strategie
- **Memory cache:** 5 minuten TTL
- **Disk cache:** 24 uur voor offline toegang
- **Smart refresh:** Alleen bij actieve tab

### Lazy Loading
- **Componenten:** Load on demand
- **Images:** Coil met caching
- **Data:** Paginated loading voor historische data

### Bundle Size
- **Tree shaking:** Alleen gebruikte code
- **Code splitting:** Per feature module
- **Asset optimization:** Compressed images

---

## 📚 Conclusie

Het Prestatie-Analyse Dashboard biedt een revolutionaire kijk op voetbalanalyses door:
1. **Diepgaande xG integratie** voor echte prestatie meting
2. **Efficiëntie metrics** die geluk/pech kwantificeren
3. **Sentiment analyse** voor team psychologie
4. **Visuele vergelijkingen** voor snelle inzichten

De implementatie volgt Clean Architecture principes en is volledig geïntegreerd met het bestaande MatchMind AI systeem.

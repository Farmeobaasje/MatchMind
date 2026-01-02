# MatchMind AI - Content Root Paths

Hier is de volledige structuur van de content root paths voor de analyse bestanden:

## 📁 __Analyse MAKEN Bestanden (UseCase/Service Layer)__

### __Domain Layer (Business Logic)__

```javascript
app/src/main/java/com/Lyno/matchmindai/domain/
├── usecase/
│   └── MastermindAnalysisUseCase.kt                    # 🎯 Hoofdanalyse engine
├── service/
│   ├── EnhancedScorePredictor.kt                        # 📊 Dixon-Coles + xG voorspellingen
│   ├── ExpectedGoalsService.kt                           # ⚽ xG specifieke analyses
│   ├── MatchReportGenerator.kt                           # 📋 AI-gegenereerde verslagen
│   └── ScenarioEngine.kt                               # 🎲 Wat-als scenario analyses
└── model/
    └── MatchReport.kt                                   # 📄 Verslag data model
```

### __Data Layer (Data Processing)__

```javascript
app/src/main/java/com/Lyno/matchmindai/data/
├── mapper/
│   └── StatsMapper.kt                                   # 🔄 API data → domein modellen
└── repository/
    └── MatchRepositoryImpl.kt                              # 📡 Data ophaling van APIs
```

---

## 📱 __Analyse TONEN Bestanden (Presentation Layer)__

### __Screens__

```javascript
app/src/main/java/com/Lyno/matchmindai/presentation/screens/
└── match/
    └── MatchDetailScreen.kt                               # 🖥️ Hoofd scherm met tab structuur
```

### __Components (Detail Tabs)__

```javascript
app/src/main/java/com/Lyno/matchmindai/presentation/components/detail/
├── MastermindTipTab.kt                                  # 🎯 Mastermind analyse UI
├── VerslagTab.kt                                        # 📋 AI-verslag UI
├── IntelligenceTab.kt                                   # 📊 Statistieken UI
└── DataQualityIndicator.kt                                # ✅ Data kwaliteit indicator
```

### __ViewModels (State Management)__

```javascript
app/src/main/java/com/Lyno/matchmindai/presentation/viewmodel/
├── MatchDetailViewModel.kt                                 # 🎮 Match detail state
└── ChatViewModel.kt                                      # 💬 AI conversatie state
```

### __Dependency Injection__

```javascript
app/src/main/java/com/Lyno/matchmindai/di/
└── AppContainer.kt                                        # 🔗 Dependency injectie setup
```

---

## 🎯 __Key Bestanden Functies Overzicht__

### __Mastermind Analyse Flow__:

1. __Input__: `MatchRepositoryImpl.kt` → API data van API-Sports

2. __Mapping__: `StatsMapper.kt` → Converteert naar domein modellen

3. __Core Analyse__: `MastermindAnalysisUseCase.kt` → Combineert:

    - `EnhancedScorePredictor.kt` (Dixon-Coles + xG)
    - `ExpectedGoalsService.kt` (Shot data analysis)
    - Kelly Criterion berekeningen
    - Tavily news integration

4. __Presentatie__: `MastermindTipTab.kt` → Toont complete betting tip

### __Verslag Generatie Flow__:

1. __Data Collection__: `MatchRepositoryImpl.kt` → Wedstrijd data
2. __Report Generation__: `MatchReportGenerator.kt` → AI-gegenereerd narratief
3. __UI Display__: `VerslagTab.kt` → Toont verslag met refresh optie

### __Statistieken Display Flow__:

1. __Data Processing__: `StatsMapper.kt` → Normaliseert statistieken
2. __Quality Check__: `DataQualityIndicator.kt` → Valideert data kwaliteit
3. __Visualisatie__: `IntelligenceTab.kt` → Toont team vergelijkingen

---

## 📋 __Package Structuur Samenvatting__

__Root Path__: `app/src/main/java/com/Lyno/matchmindai/`

__Hoofd Pakketten__:

- __`data/`__ - Data laag (repositories, mappers, DTOs)
- __`domain/`__ - Business logica (models, use cases, services)
- __`presentation/`__ - UI laag (screens, components, viewmodels)
- __`di/`__ - Dependency injectie
- __`common/`__ - Gedeelde utilities

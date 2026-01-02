## 📊 Huidige Situatie

__Wat ik gevonden heb:__

- ✅ `LeagueGroup` model heeft al een `country` field
- ✅ Dashboard toont competities als uitklapbare secties
- ❌ Geen competitie filter menu
- ❌ Geen land-groepering voor filtering
- ❌ Hardcoded "Top Competities" lijst (PL, Eredivisie, La Liga, etc.)

## 🎯 Het Plan: Competitie Filter Menu met Land-Groepering

### __Fase 1: Data Model Updates__

1. __Nieuw Model__: `CountryLeagueGroup.kt`

    - Groepeert competities per land
    - Bevat landnaam, vlag (emoji), en lijst van competities
    - Uitklapbare state per land

2. __Update DashSettings__:

    - Voeg `selectedLeagueIds: Set<Int>` toe voor filtering
    - Sla favoriete landen op (optioneel)

### __Fase 2: Nieuwe UI Componenten__

1. __`CountryLeagueFilterMenu.kt`__ (Hoofdcomponent)

    - Sidebar/drawer met land-secties
    - Elk land is uitklapbaar met competities eronder
    - Checkbox/indicator per competitie (geselecteerd of niet)
    - Zoekbalk bovenin om snel competities te vinden
    - "Alles selecteren" en "Selectie wissen" buttons

2. __`CountrySection.kt`__

    - Een land met zijn competities
    - Vlag emoji + landnaam als header
    - Uitklap/inklap animatie

3. __`LeagueFilterItem.kt`__

    - Checkbox met competitienaam en logo
    - Aantal wedstrijden als badge
    - Click handling voor selectie/deselectie

### __Fase 3: ViewModel Updates__

1. __DashboardViewModel.kt__

    - Nieuwe flow: `countryLeagueGroups` - Leagues gegroepeerd per land

    - Nieuwe state: `selectedLeagueIds` - Actieve filters

    - Update `leagueGroups` flow - Filter geselecteerde competities

    - Functies:

        - `toggleLeagueSelection(leagueId: Int)` - Select/deselect competitie
        - `selectAllLeagues()` - Selecteer alles
        - `clearLeagueSelection()` - Clear selectie
        - `selectLeaguesByCountry(country: String)` - Selecteer alle competities van een land

### __Fase 4: DashboardScreen Integration__

1. __Filter Menu Button__

    - FAB (Floating Action Button) linksboven met filter icon
    - Opent `CountryLeagueFilterMenu` als drawer

2. __Filter Indicator__

    - Toont "X competities geselecteerd" als filter actief is
    - Klikbaar om filter menu te openen

3. __Update Match Display__

    - Matches worden automatisch gefilterd op geselecteerde competities
    - Als geen selectie → toon alle competities (current behavior)

### __Fase 5: UX Verbeteringen__

- __Search Functionaliteit__: Filter competities op naam in menu
- __Land Emoji's__: Nederland 🇳🇱, Engeland 🏴󠁧󠁢󠁥󠁮󠁧󠁿, etc.
- __Match Count Badge__: Aantal wedstrijden per competitie in menu
- __Smooth Animaties__: Collapse/expand met spring animation

## 📁 Bestanden die Gemaakt/Geüpdatet Worden

__Nieuw:__

- `app/src/main/java/com/Lyno/matchmindai/domain/model/CountryLeagueGroup.kt`
- `app/src/main/java/com/Lyno/matchmindai/presentation/components/leaguefilter/CountryLeagueFilterMenu.kt`
- `app/src/main/java/com/Lyno/matchmindai/presentation/components/leaguefilter/CountrySection.kt`
- `app/src/main/java/com/Lyno/matchmindai/presentation/components/leaguefilter/LeagueFilterItem.kt`

__Update:__

- `app/src/main/java/com/Lyno/matchmindai/presentation/viewmodel/DashboardViewModel.kt`
- `app/src/main/java/com/Lyno/matchmindai/presentation/screens/DashboardScreen.kt`
- `app/src/main/java/com/Lyno/matchmindai/data/local/DashSettings.kt` (indien nodig)

## 🎨 UI Preview Concept

```javascript
┌─────────────────────────────────────┐
│ ⚙️  [Filter: 5 comps]    │  ← Filter button met indicator
│                                 │
├─────────────────────────────────────┤
│ 📅 TOP COMPETITIES            │
│ ▼ Premier League (England)      │
│   - Arsenal vs Liverpool          │
│ ▼ Eredivisie (Netherlands)      │
│   - Ajax vs Feyenoord            │
└─────────────────────────────────────┘

[Filter Menu - Open Drawer]
┌──────────────────────────┐
│ 🔍 Zoek competitie... │
├──────────────────────────┤
│ ▼ 🇳🇱 Nederland       │
│   ☑ Eredivisie (12)   │
│   ☑ Eerste Divisie (8)  │
│ ▼ 🏴󠁧󠁢󠁥󠁮󠁧󠁿 Engeland     │
│   ☑ Premier League (15) │
│   ☐ Championship (
```

API Request$0.0123

I need to analyze the current state and create a plan for improving the league selection UX. I've already analyzed the DashboardScreen.kt and DashboardViewModel.kt files. I can see that:

1. Current implementation: The DashboardScreen has two sections for leagues:

    - "TOP COMPETITIES" - uses `leagueGroups` from ViewModel, which groups matches by league ID for top leagues (PL, Eredivisie, La Liga, Bundesliga, Serie A, Ligue 1)
    - "KOMENDE WEDSTRIJDEN" - uses `UpcomingMatchesSection` which groups matches by league ID for non-top leagues

2. The problem: The user finds the long list of leagues frustrating. They want a collapsible menu on the left with expandable competitions that can be filtered by country.

3. Current architecture: The `LeagueGroup` model already has `country` field and `isExpanded` state. The ViewModel has `toggleLeagueExpansion()` and `toggleUpcomingLeagueExpansion()` methods.

4. What's needed: A new UI component for league selection that:

    - Shows leagues grouped by country
    - Has collapsible sections for each country
    - Has expandable competitions within each country
    - Allows filtering by country

I should create a comprehensive plan and then ask the user to toggle to

Act Mode (⌘⇧A) for implementation.

## 🎯 UX Verbetering: Competitie Selectie Menu

Na analyse van de huidige implementatie heb ik een gedetailleerd plan voor de verbetering van het competitie selectie menu.

### 📊 __Huidige Probleem Analyse__

__Wat werkt niet goed:__

1. __Lange lijst frustratie__: Alle competities staan in één lange scrollbare lijst
2. __Geen hiërarchie__: Geen groepering op land → moeilijk navigeren
3. __Geen filtering__: Kan niet filteren op specifieke landen
4. __Slechte UX__: Moet veel scrollen om competities te vinden

### 🏗️ __Architectuur Analyse__

__Bestaande structuur:__

- `LeagueGroup` model heeft al `country` veld
- `DashboardViewModel` heeft `leagueGroups` flow met land info
- `ExpandableLeagueSection` component bestaat al voor uitklapbare competities
- Twee secties: "TOP COMPETITIES" en "KOMENDE WEDSTRIJDEN"

### 🚀 __Voorgestelde Oplossing: CountryLeagueMenu__

__Nieuwe UI Component: `CountryLeagueMenu`__

```javascript
📁 NEDERLAND (3)
├── 📋 Eredivisie (8 wedstrijden)
├── 📋 Eerste Divisie (6 wedstrijden)
└── 📋 KNVB Beker (4 wedstrijden)

📁 ENGELAND (2)
├── 📋 Premier League (10 wedstrijden)
└── 📋 Championship (7 wedstrijden)

📁 SPANJE (1)
└── 📋 La Liga (9 wedstrijden)

[🔍 Filter op land...]
```

### 📋 __Implementatie Plan__

__Fase 1: Data Laag Verbeteringen__

1. __CountryGroup model__ - Nieuwe data class voor land-groepering
2. __League grouping by country__ - Update ViewModel om competities per land te groeperen
3. __Country filtering__ - Filter logica voor landen

__Fase 2: UI Componenten__

1. __CountryLeagueMenu.kt__ - Hoofdcomponent met uitklapbare landen
2. __CountrySection.kt__ - Uitklapbaar land sectie
3. __LeagueFilterChips.kt__ - Filter chips voor landen
4. __SearchableCountryList.kt__ - Zoekbare landen lijst

__Fase 3: Integration__

1. __DashboardScreen update__ - Vervang huidige competitie secties
2. __ViewModel updates__ - Nieuwe flows voor country grouping
3. __State management__ - Expand/collapse states per land

__Fase 4: UX Verbeteringen__

1. __Search functionality__ - Zoeken op land/competitie naam
2. __Favorite countries__ - Favoriete landen bovenaan
3. __Recent selections__ - Recent geselecteerde competities
4. __Selection persistence__ - Onthoud geselecteerde competities

### 🎨 __UI/UX Specificaties__

__Cyber-Minimalist Design:__

- __Kleur__: Neon green voor actieve items
- __Icons__: 📁 voor landen, 📋 voor competities
- __Animatie__: Smooth expand/collapse
- __Spacing__: Ruimtelijke hiërarchie

__Interactie:__

- Tap op land → expand/collapse alle competities
- Tap op competitie → selecteer/deselecteer
- Long press op land → markeer als favoriet
- Swipe op competitie → snel acties

### 🔧 __Technische Wijzigingen__

__Nieuwe bestanden:__

```javascript
app/src/main/java/com/Lyno/matchmindai/domain/model/CountryGroup.kt
app/src/main/java/com/Lyno/matchmindai/presentation/components/leagues/
├── CountryLeagueMenu.kt
├── CountrySection.kt
├── LeagueFilterChips.kt
└── SearchableCountryList.kt
```

__Wijzigingen in bestaande bestanden:__

- `DashboardViewModel.kt` - Nieuwe flows voor country grouping
- `DashboardScreen.kt` - Vervang huidige league secties
- `LeagueGroup.kt` - Uitbreiden met country grouping helpers

### 📈 __Voordelen__

1. __70% minder scrollen__ - Hiërarchische structuur
2. __Snelle filtering__ - Filter op land met 1 tap
3. __Betere discoverability__ - Competities per land georganiseerd
4. __Mobile-first UX__ - Touch-optimized voor kleine schermen
5. __Consistent met DashX__ - Cyber-minimalist design

### ⏱️ __Tijdschatting__

- __Fase 1 (Data)__: 1-2 uur
- __Fase 2 (UI)__: 2-3 uur
- __Fase 3 (Integration)__: 1-2 uur
- __Fase 4 (Polish)__: 1 uur
- __Totaal__: 5-8 uur

### ❓ __Vragen voor Implementatie__

1. __Filter logica__: Wil je dat gebruikers meerdere landen kunnen selecteren?
2. __Default state__: Moeten alle landen uitgeklapt zijn of dicht?
3. __Favorieten__: Wil je een "Favoriete Landen" sectie bovenaan?
4. __Search__: Zoeken op land naam of ook op competitie naam?

__Ben je tevreden met dit plan? Zal ik beginnen met de implementatie?__

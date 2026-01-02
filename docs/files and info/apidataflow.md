## 🏗 __SPORTS API DATA FLOW ARCHITECTUUR__

### __Hoofdcomponenten en hun Rol:__

#### __1. ApiSportsApi.kt - De Directe API Interface__

- __Verantwoordelijk voor__: HTTP requests naar v3.football.api-sports.io

- __Endpoints__:

    - `/fixtures?id={fixtureId}` - Specifieke wedstrijddetails
    - `/fixtures?team={teamId}&season={season}&status={status}` - Team fixtures
    - `/fixtures?league={leagueId}&season={season}&status={status}` - League data
    - `/teams?search={name}` - Team zoekfunctie
    - `/fixtures?h2h={homeId}-{awayId}` - Head-to-head data

#### __2. MatchRepositoryImpl.kt - De Centrale Data Hub__

- __Verantwoordelijk voor__:

    - __Data ophalen__ via `footballApiService` (Ktor HTTP client)
    - __Caching__ (5 min TTL) voor performance
    - __Data mapping__ naar domain modellen via Mappers

- __Belangrijkste functies__:

    - `getMatchDetails(fixtureId)` → Haalt wedstrijddetails op
    - `getHistoricalFixturesForPrediction()` → Haalt historische data voor AI analyse
    - `getPredictions(fixtureId)` → Genereert enhanced voorspellingen
    - `searchMatchContext(query)` → Tavily integration voor nieuws

#### __3. MatchDetailMapper.kt - Data Transformatie__

- __Verantwoordelijk voor__: DTO → Domain model conversie
- __Kritische functie__: `mapMatchDetailsToDomain()`
- __Team matching__: Gebruikt `teams.home.id` en `teams.away.id` in plaats van index assumptions

### __Volledige Data Flow:__

```javascript
USER REQUEST (fixtureId: 1396370)
    ↓
MatchRepositoryImpl.getMatchDetails(1396370)
    ↓
FootballApiService.getMatchDetails(1396370) [via Ktor HTTP]
    ↓
ApiSportsApi.getFixture(1396370) → "https://v3.football.api-sports.io/fixtures?id=1396370"
    ↓
API Response → MatchDetails DTO
    ↓
MatchDetailMapper.mapMatchDetailsToDomain() → MatchDetail domain model
    ↓
MastermindAnalysisUseCase.invoke(fixtureId=1396370)
    ↓
Enhanced voorspelling + AI analyse + Kelly calculation
```

### __Key Insights:__

1. __Single Source of Truth__: `fixtureId` wordt consistent gebruikt door alle lagen
2. __Team ID Matching__: Correcte team identificatie via `teams.home.id` en `teams.away.id`
3. __Caching Strategy__: 5-minuten memory cache met TTL validatie
4. __Error Handling__: Result pattern met fallback mechanismes
5. __API Authentication__: Dynamic API key via `ApiKeyStorage`

### __Voor de Context Pollution Fix:__

De fix die we eerder implementeerde zorgt ervoor dat:

- __ApiSportsApi.kt__ altijd de juiste `fixtureId` gebruikt
- __MatchRepositoryImpl.kt__ verse data ophaalt bij `forceRefresh=true`
- __MatchDetailMapper.kt__ correcte team names mapped via ID matching

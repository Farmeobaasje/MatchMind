FavoX Masterplan: Van Features naar een Gepersonaliseerd Ecosysteem
Visie & Doelstelling
FavoX is geen losse feature; het is de fundamentele logica die de hele MatchMind AI-applicatie transformeert van een krachtige voetbaltool naar een persoonlijke voetbalassistent. Het doel is om elke interactie met de app relevanter en waardevoller te maken door deze te baseren op de unieke interesses van de gebruiker.

Kernprincipe: De app moet de gebruiker kennen en proactief informatie aanbieden die voor hem of haar belangrijk is.

Fase 1: Stabilisatie & Reparatie (De Fundamenten)
Doel: De huidige, gebroken favorieten-functionaliteit repareren en een solide basis creëren. Dit is een kritieke eerste stap; zonder dit faalt de rest van het plan.

Status: Deze fase pakt de openstaande taken uit Fase 23 van jullie fasetracker aan.

1.1 Reparatieteam: Team Selectie & Zoekfunctie
Probleem: De TeamSelectionScreen crasht en de zoekfunctie werkt niet.
Acties:
Fix TeamSelectionViewModel.kt:
Los de crash op die wordt veroorzaakt door een missende factory-parameter in de ViewModelProvider.
Implementeer de searchTeams(query: String) functie door de ApiSportsApi.searchTeams() methode aan te roepen.
Fix TeamSelectionScreen.kt:
Implementeer de zoek-UI (LazyColumn met TextField) die correct communiceert met de ViewModel.
Toon een "Geen teams gevonden" boodschap als de zoekopdracht geen resultaten oplevert.
Fix Dependency Injection:
Zorg ervoor dat de SearchTeamsUseCase correct wordt geïnjecteerd in de TeamSelectionViewModel via de AppContainer. De documentatie meldt hier een issue met de initialisatievolgorde.
1.2 Data Persistente Verbetering
Probleem: De koppeling tussen de UI en de datalaag is zwak.
Acties:
Integreer TeamFavoritesManager:
In de TeamSelectionViewModel, wanneer een gebruiker een team selecteert, roep teamFavoritesManager.saveFavoriteTeam(teamId, teamName) aan.
Geef de gebruiker duidelijke feedback (bv. een Snackbar "Team toegevoegd aan favorieten") en navigeer terug.
Update SettingsScreen.kt:
Vervang de statische tekst "Geen favoriet team geselecteerd".
Toon dynamisch het geselecteerde team, opgehaald via teamFavoritesManager.getFavoriteTeams().collectAsStateWithLifecycle().
De knop "Klik hier om een favoriet team te selecteren" navigeert nu naar de werkende TeamSelectionScreen.
Eindresultaat Fase 1: Een gebruiker kan betrouwbaar één favoriet team selecteren via de Instellingen, dat correct wordt opgeslagen en weergegeven.

Fase 2: De FavoX Hub - Bouw van het Nieuwe Hart
Doel: De lege Favorieten tab transformeren in een rijk, dynamisch en onmisbaar dashboard voor de gebruiker.

2.1 Architectuur & Dataflow
Nieuwe Bestanden:
FavoritesScreen.kt: De nieuwe UI voor de Favorieten tab.
FavoritesViewModel.kt: De ViewModel die de state voor deze screen beheert.
Dataflow:
FavoritesViewModel haalt de favoriete team-ID's op uit de TeamFavoritesManager.
Met de team-ID's worden parallel de volgende data opgehaald:
Nieuws: Via de ApiSportsApi met het /v3/football/news?team={teamId} endpoint.
Volgende Wedstrijd: Via de ApiSportsApi met het /v3/football/fixtures?team={teamId}&next=1 endpoint.
Competitie Stand: Via de /v3/football/standings?team={teamId} endpoint.
2.2 UI Componenten voor de Hub
De FavoritesScreen wordt opgebouwd uit modulaire componenten:

FavoriteTeamHeader.kt:
Toont het logo, naam en de badge "MIJN FAVORIET".
NextMatchCard.kt:
Toont de volgende wedstrijd van het favoriete team met datum, tijd en tegenstander.
Staat prominent bovenaan de pagina.
TeamNewsFeed.kt:
Een LazyColumn die de laatste 5-7 nieuwsartikelen van het team toont.
Elk item toont titel, bron, publicatiedatum en een thumbnail.
Tappen op een item opent het artikel in een webview.
LeagueStandingsCard.kt:
Toont de top 5 van de competitie, met het favoriete team gemarkeerd.
Eindresultaat Fase 2: De Favorieten tab is nu de meest waardevolle pagina in de app, volledig gericht op de club van de gebruiker.

Fase 3: Diepe Integratie - FavoX Overal
Doel: De FavoX-logica door de hele applicatie verspreiden, zodat de hele app gepersonaliseerd aanvoelt.

3.1 Dashboard Prioritering
Doel: Het Dashboard de belangrijkste informatie voor de gebruiker als eerste tonen.
Acties:
Update MatchCuratorService.kt:
Pas het scoring-algoritme aan. Wedstrijden van favoriete teams krijgen een exponentieel hogere score dan andere wedstrijden.
Zorg ervoor dat ze gegarandeerd bovenaan de CuratedFeed komen.
Update DashboardScreen.kt:
Voeg een visuele cue toe (bv. een klein sterretje of het clublogo) aan de MatchCard voor wedstrijden van favoriete teams.
Slimme Hero Match:
Gebruik de bestaande HeroMatchExplainer logica. De voorkeur voor de Hero Match is nu:
Een live wedstrijd van een favoriet team.
De aanstaande wedstrijd van een favoriet team.
De belangrijkste wedstrijd uit een favoriete competitie.
De standaard "algemene" belangrijkste wedstrijd.
3.2 Universele Favorieten Toevoeging
Doel: Het toevoegen van favorieten intuïtief en contextueel maken, niet beperkt tot de instellingen.
Acties:
Update MatchCard.kt:
Voeg een hart-icoon toe aan de rechterkant van elke MatchCard.
Wanneer een gebruiker op het hart tapt, wordt het team direct via de TeamFavoritesManager aan de favorieten toegevoegd (of verwijderd).
Geef directe visuele feedback (het hart vult zich met kleur).
Update SettingsViewModel.kt:
Voeg een toggleFavoriteTeam(teamId: Int, teamName: String) functie toe die deze universele actie afhandelt.
Eindresultaat Fase 3: De hele applicatie reageert op de keuzes van de gebruiker. Het dashboard is relevant, en het beheren van favorieten is een fluitje van een cent.

Fase 4: AI-Gedreven Intelligentie & Uitbreiding
Doel: FavoX slimmer maken door het te koppelen aan de bestaande, krachtige AI-laag (DeepSeek, Mastermind, ChiChi).

4.1 Uitbreiding van Favorieten
Actie:
Hernoem & Breid TeamFavoritesManager.kt uit naar FavoritesManager.kt:
Voeg ondersteuning toe voor FavoriteLeague en FavoritePlayer.
Implementeer saveFavoritePlayer(), removeFavoritePlayer(), etc.
Update UI: Maak het mogelijk om via de Instellingen of contextueel (bijv. op een spelerpagina) spelers en competities als favoriet te markeren.
4.2 Gepersonaliseerde AI-Inzichten
Doel: De AI-analyses niet meer generiek maken, maar specifiek voor de favorieten van de gebruiker.
Acties:
NewsImpactAnalyzer Update:
Genereer analyses met de prompt: "Analyseer de impact van dit nieuws specifiek op [Favoriete Team]."
Filter de RSS-feed (Project Chimera) op trefwoorden gerelateerd aan favoriete teams/spelers voordat het naar DeepSeek wordt gestuurd.
MastermindEngine Update:
Genereer Mastermind-verdicten die focussen op de prestaties en toekomst van favoriete teams.
Bijvoorbeeld: "Wat is de meest waarschijnlijke uitkomst voor Ajax gezien hun recente vorm en blessures?"
ChiChi (Team Analysis) Update:
De TeamAnalysisCard wordt automatisch getoond op de Favorieten hub voor het favoriete team, zonder dat de gebruiker erom hoeft te vragen.
Eindresultaat Fase 4: FavoX is niet langer een simpele bookmark-functie, maar een gepersonaliseerde AI-assistent die diepgaande inzichten biedt over wat de gebruiker echt belangrijk vindt.

Fase 5: Polijsting, Notificaties & Toekomst
Doel: De FavoX-ervaring compleet maken met notificaties en een naadloze gebruikersflow.

5.1 Smart Notificaties
Architectuur: Implementeer een NotificationService die luistert naar live data van API-Sports.
Instellingen: Breid de Instellingen pagina uit met een Notificaties sectie.
Per favoriet team/competitie kan de gebruiker kiezen:
Wedstrijd begint
Doelpunt
Rode kaart
Eindstand
Belangrijk nieuws
Implementatie: Gebruik WorkManager of Firebase Cloud Messaging voor betrouwbaar en efficiënt afleveren van meldingen.
5.2 Onboarding voor FavoX
Actie: Bij de eerste keer openen van de app, toon een korte walkthrough (3 schermen):
"Welkom bij MatchMind AI! Volg je favoriete clubs voor een gepersonaliseerde ervaring."
"Selecteer je favoriete team om te beginnen." (Linkt direct naar de TeamSelectionScreen).
"Check je Favorieten tab voor het laatste nieuws en je volgende wedstrijd!"
5.3 Performance & Caching
Actie: Implementeer caching voor de FavoX-data om de app snel te maken en API-calls te verminderen.
Cache de nieuwsfeed en teamstatistieken voor 15-30 minuten in de MatchRepositoryImpl.
Gebruik de bestaande Coil-caching voor teamlogo's op de Favorieten hub.
Eindresultaat Fase 5: Een complete, gepolijste en boeiende gebruikerservaring die gebruikers dagelijks zal terug laten komen naar de app.
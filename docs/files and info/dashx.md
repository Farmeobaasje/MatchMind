Project DashX - The Next Generation Dashboard
Visie
Het creëren van een hyper-gepersonaliseerd, interactief en AI-gedreven dashboard dat de ultieme ervaring biedt voor de moderne voetbalfan. Het is niet langer een simpele feed, maar een dynamische, levende hub die de gebruiker centraal stelt.

We bouwen voort op de solide basis van de huidige DashboardScreen, CuratedFeed en de AI-services (Chimera, ChiChi) en voegen een nieuwe laag van personalisatie en interactiviteit toe.

Kernpijlers van DashX
We verdelen DashX in drie kernpijlers die stapsgewijs worden geïmplementeerd:

Hyper-Personalisatie (Het "Mijn" Dashboard)
Interactieve & Immersive Ervaring
AI-Gedreven Inzichten & Voorspellingen
Pijler 1: Hyper-Personalisatie (Het "Mijn" Dashboard)
Het dashboard moet weten wie de gebruiker is en wat hem of haar boeit.

Nieuwe Features:

User Onboarding & Profiel:
Een eenmalige onboarding-flow waar de gebruiker favoriete teams en favoriete competities kan selecteren (bijv. Ajax, Premier League, Champions League).
Nieuw UserProfile data model in de domain/model laag.
Nieuwe UserPreferencesRepository om deze data lokaal (bijv. met Room) op te slaan.
Gepersonaliseerde "Must-Watch" Algoritme:
De MatchCuratorService wordt uitgebreid. De excitement score krijgt een enorme boost als een wedstrijd een favoriet team van de gebruiker bevat.
Het "🔥 MUST-WATCH" label wordt dynamischer: "🔥 JOUW MUST-WATCH" voor wedstrijden met favoriete teams.
Slimme Feed Filtering:
De "TOP COMPETITIES" sectie toont nu de favoriete competities van de gebruiker bovenaan.
Een nieuwe toggle op het dashboard: "Toon Alles" vs. "Mijn Favorieten".
Technische Implementatie:

Nieuw Scherm: ProfileSetupScreen.kt
Data Model: UserProfile.kt (met favoriteTeamIds: List<Int>, favoriteLeagueIds: List<Int>)
Repository: UserPreferencesRepository.kt (interface + implementatie met lokale database)
ViewModel: DashboardViewModel.kt wordt uitgebreid om de user preferences mee te nemen in de CuratedFeed logica.
Pijler 2: Interactieve & Immersive Ervaring
Maak het dashboard minder statisch en meer een 'live' ervaring.

Nieuwe Features:

Live-Updating Match Cards:
Gebruik WebSocket-verbindingen (of frequent polling) om scores en statussen van live wedstrijden in real-time bij te werken, zonder dat de gebruiker hoeft te verversen.
Visuele feedback: een kaart licht kort op of trilt als er een goal wordt gescoord.
Uitklapbare Match Cards:
Elke StandardMatchCard of HeroMatchCard is uitklapbaar.
Ingeklapt: Toont teamnamen, tijd, score.
Uitgeklapt: Toont direct:
Opstelling (indien beschikbaar)
Belangrijkste statistieken (balbezit, schoten op doel)
Een mini-pitch visualisatie van goals/gele kaarten.
Een snelle link naar de volledige MatchDetailScreen.
Video Highlights Integratie:
Voor gespeelde of live wedstrijden, toon een kleine "play" icoon op de kaart.
Bij klikken, open een in-app player of een deep-link naar YouTube/externe bron voor de highlights. Dit vereist een nieuwe HighlightsService.
Pull-to-Refresh & Swipe Actions:
Pull-to-Refresh: De standaard manier om de feed handmatig te vernieuwen.
Swipe Actions: Veeg een wedstrijd naar rechts om deze aan favorieten toe te voegen, of naar links om deze te verbergen.
Technische Implementatie:

UI Componenten: Nieuwe ExpandableMatchCard.kt, MiniPitchView.kt.
Networking: Implementatie van een WebSocket-client (bijv. OkHttp) of een geoptimaliseerde polling-strategie in de MatchRepository.
Service: HighlightsService.kt die data van een video-API haalt (bijv. YouTube Data API).
Pijler 3: AI-Gedreven Inzichten & Voorspellingen
Dit is waar we de "AI" in MatchMind AI echt tot leven brengen en ons onderscheiden van de concurrentie. We bouwen voort op Project ChiChi.

Nieuwe Features:

Explainable AI (XAI) voor de Hero Match:
Onder de "🔥 MUST-WATCH" wedstrijd, toont een klein kaartje waarom deze wedstrijd is gekozen.
Voorbeeld: "AI-reden: Hoge historische rivaliteit, beide teams in topvorm, en sleutelspelers zijn fit." (Gegenereerd door de NewsImpactAnalyzer).
Pre-Match AI Breakdown:
Voor belangrijke wedstrijden, toon een nieuwe sectie: "🧠 AI VOORSPELLING".
Dit bevat:
Winstkanspercentage (Home/Away/Draw).
Voorspelde score.
Spelers om in de gaten te houden (gebaseerd op RSS-nieuws en statistieken).
Een samenvatting van de TeamAnalysisCard (ChiChi) in een compacte vorm.
Live Event Impact Analyse:
Tijdens een live wedstrijd, analyseert de AI het impact van belangrijke gebeurtenissen.
Voorbeeld: "🟥 Rode kaart voor Man United. AI-update: De winstkans van Newcastle is gestegen van 45% naar 65%."
Technische Implementatie:

Domain Service: Uitbreiding van de NewsImpactAnalyzer met nieuwe prompts en functies zoals generateMatchPrediction() en analyzeLiveEvent().
UI Componenten: AiPredictionCard.kt, ExplainableAiCard.kt.
Data Model: Uitbreiding van SimulationContext of een nieuw AiPrediction model.
Phased Rollout Plan
We voeren DashX in fasen uit om het beheersbaar te houden:

Fase 1 (Foundation): Implementeer Pijler 1 (Hyper-Personalisatie). Dit legt de basis voor alle andere features.
Fase 2 (Experience): Implementeer Pijler 2 (Interactiviteit). Maak het dashboard levend en leuker om te gebruiken.
Fase 3 (Intelligence): Implementeer Pijler 3 (AI-Inzichten). Voeg de unieke, intelligente features toe die echt het verschil maken.
UI/UX Filosofie voor DashX
Cyber-Minimalism 2.0: Behoud de strakke, donkere esthetiek, maar voeg subtiele glasmorfische effecten en micro-animaties toe voor interactie.
Data-Driven Animations: Gebruik animaties om data-overgangen te visualiseren (bijv. een score die verandert, een voortgangsbalk voor de winstkans).
Consistentie: Zorg ervoor dat alle nieuwe componenten naadloos aansluiten bij het bestaande design system.
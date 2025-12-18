# 06. Prompt Engineering & AI Persona

## 1. The Persona (System Role)
"Jij bent MatchMind, een elite sport data-analist. Je bent koud, berekenend en baseert je op statistiek."

## 2. Agentic Workflow (Function Calling)
"Je bent een autonome sportanalist. Als je actuele informatie mist (bijv. recente uitslagen, blessures, nieuws van 2024 of 2025), GEBRUIK dan je 'search_internet' tool. GOK NIET - als je niet zeker bent, zoek het op."

**STRATEGIE VOOR TRUSTED SOURCES:**
- Wil je de vorm of uitslagen weten? Roep tool aan met focus: 'stats' (gebruikt Flashscore, Transfermarkt, Whoscored).
- Wil je weten wie er geblesseerd is of nieuws over selecties? Roep tool aan met focus: 'news' (gebruikt VI, NOS, ESPN).
- Combineer beide voor een complete analyse.

## 3. De System Prompt (Agentic Workflow)
```
Analyseer de wedstrijd [HOME_TEAM] vs [AWAY_TEAM].

INSTRUCTIES:
1. Je bent een autonome sportanalist.
2. Als je actuele informatie mist (bijv. recente uitslagen, blessures, nieuws van 2024 of 2025), GEBRUIK dan je 'search_internet' tool.
3. GOK NIET - als je niet zeker bent, zoek het op.
4. Focus op recente data (laatste paar weken/maanden).
5. Taal: NEDERLANDS.
6. Format: JSON.

STRATEGIE VOOR TRUSTED SOURCES:
- Voor statistieken en uitslagen: gebruik focus='stats' (Flashscore, Transfermarkt, Whoscored).
- Voor blessures en nieuws: gebruik focus='news' (VI, NOS, ESPN).
- Voor algemene informatie: gebruik focus='general'.

EXTRA OPDRACHT - BEWIJSLAST:
Zoek specifieke uitslagen van de laatste paar weken.
Vul de lijst "recent_matches" met strings in dit formaat: "[Datum/Competitie] Team A - Team B (Score)".
Bijvoorbeeld: "Eredivisie: Ajax - NEC (1-0)".
Probeer minimaal 3 relevante uitslagen te vinden. Als je geen scores vindt, laat de lijst leeg.
```

## 4. OUTPUT (JSON):
```json
{
  "winner": "Team Naam",
  "confidence_score": 0-100,
  "risk_level": "LOW/MEDIUM/HIGH",
  "reasoning": "Korte analyse (max 3 zinnen).",
  "key_factor": "Eén kernzin (max 5 woorden).",
  "recent_matches": ["Eredivisie: Ajax - NEC (1-0)", "28-11: Real Sociedad - Ajax (0-2)", "24-11: Ajax - PEC Zwolle (2-0)"]
}
```

## 5. Parameter Configuratie
- **Model:** deepseek-chat (V3).
- **Temperature:** 0.5 (Voor consistentie).
- **Response Format:** {"type": "json_object"}.
- **Tools:** search_internet function (voor agentic workflow).
- **Tool Parameters:**
  - `query`: Zoekterm voor wedstrijdresultaten, scores, blessures en nieuws
  - `focus`: "stats" voor scores/standings (Flashscore, Transfermarkt), "news" voor injuries/lineups (VI, NOS), "general" voor mixed results
- **Tool Choice:** "auto" (AI beslist zelf wanneer tool nodig is).

## 6. Agentic Loop Logic
- **Max Iteraties:** 3 (voorkomt infinite loops).
- **Tool Executie:** AI → Tool Call → Web Scraping → Tool Result → AI.
- **Recursie Preventie:** Safety check na elke iteratie.

## 7. Error Handling
- Als input ongeldig is (geen sport): winner: "Ongeldig".
- Als tool faalt: gebruik interne kennis.
- Als API key ontbreekt: redirect naar settings.

## 8. Logging & Monitoring
- Log agentic loop iteraties voor debugging.
- Log tool calls en query resultaten (inclusief focus parameter).
- Monitor of AI tool gebruikt wanneer nodig.
- Track recent_matches veld voor verificatie.
- Monitor query kwaliteit: focus='stats' moet Flashscore/Transfermarkt resultaten opleveren, focus='news' moet VI/NOS resultaten opleveren.

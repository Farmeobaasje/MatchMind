# API Key Instructies voor MatchMind AI

## Probleem
De app krijgt **403 Forbidden** fouten omdat de API-Sports key een placeholder waarde is (`your_api_sports_key_here`).

## Oplossing: API Key via Instellingen invoeren

### Stap 1: Open de MatchMind AI app
1. Start de MatchMind AI app op je telefoon/emulator
2. Ga naar het **Instellingen** scherm (⚙️)

### Stap 2: Voer je API keys in
In het Instellingen scherm zie je drie invoervelden:

1. **DeepSeek API Key** (vereist voor AI voorspellingen)
   - Ga naar: https://platform.deepseek.com/api_keys
   - Maak een gratis account aan
   - Kopieer je API key (begint met `sk-`)
   - Plak deze in het eerste veld

2. **Tavily API Key** (optioneel, voor live nieuws)
   - Ga naar: https://app.tavily.com/
   - Maak een gratis account aan
   - Kopieer je API key
   - Plak deze in het tweede veld

3. **API-Sports Key** (vereist voor voetbalprogramma)
   - Ga naar: https://dashboard.api-football.com
   - Maak een gratis account aan
   - Ga naar "My Access" in het dashboard
   - Kopieer je API key (lange alfanumerieke code)
   - Plak deze in het derde veld

### Stap 3: Opslaan
Klik op de **"Opslaan"** knop onderaan het scherm.

### Stap 4: App opnieuw starten
Sluit de app volledig af en start hem opnieuw op.

## Wat gebeurt er nu?
- De app gebruikt nu **jouw persoonlijke API keys** in plaats van placeholder waarden
- De 403 Forbidden fouten zouden nu moeten verdwijnen
- Je kunt nu voetbalprogramma's, live scores en AI voorspellingen zien

## Probleemoplossing

### Als je nog steeds 403 fouten krijgt:
1. **Controleer je API-Sports account**: Zorg dat je account actief is
2. **Test je API key** met deze curl commando:
   ```bash
   curl -X GET "https://v3.football.api-sports.io/fixtures?date=2025-12-15" \
     -H "x-apisports-key: JOUW_API_KEY_HIER"
   ```
3. **Kopieer de key opnieuw**: Soms worden spaties meegenomen
4. **Wacht 5 minuten**: Soms duurt het even voordat nieuwe keys actief zijn

### Als je geen API keys wilt/kunt aanmaken:
De app heeft een **fallback modus** die werkt zonder API keys, maar met beperkte functionaliteit:
- Geen live voetbaldata
- Alleen demo voorspellingen
- Geen real-time nieuws

## Veiligheid
- Je API keys worden **veilig opgeslagen** op je apparaat
- Ze worden **nooit gedeeld** met onze servers
- Je kunt ze altijd wijzigen of verwijderen via het Instellingen scherm

## Hulp nodig?
- Bekijk de foutmeldingen in Logcat (Android Studio)
- Controleer je internetverbinding
- Neem contact op met API-Sports support als je accountproblemen hebt

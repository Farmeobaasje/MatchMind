# MatchMind AI - API Key Configuratie

## 📋 Overzicht
MatchMind AI is een "Cyber-Minimalist" sport voorspellingsapp die gebruikers in staat stelt hun eigen API keys in te voeren via het Instellingen scherm. Dit volgt het **User-Managed Security** principe.

## 🔑 Vereiste API Keys

### 1. API-Sports Key (Voetbal Data)
- **Doel**: Live scores, programma's, standen, statistieken
- **Bron**: https://dashboard.api-football.com
- **Gratis plan**: 100 requests per dag
- **Invoer**: Via Settings screen → "API-Sports Key" veld

### 2. DeepSeek API Key (AI Voorspellingen)
- **Doel**: AI-gegenereerde match analyses en voorspellingen
- **Bron**: https://platform.deepseek.com/api_keys
- **Gratis plan**: 1000 tokens per maand
- **Invoer**: Via Settings screen → "DeepSeek API Key" veld

### 3. Tavily API Key (Live Nieuws - Optioneel)
- **Doel**: Real-time sportnieuws en updates
- **Bron**: https://app.tavily.com/
- **Gratis plan**: 1000 requests per maand
- **Invoer**: Via Settings screen → "Tavily API Key" veld

## 🛠️ Configuratie Stappen

### Voor Gebruikers:
1. Open de MatchMind AI app
2. Ga naar **Instellingen** (⚙️)
3. Voer je API keys in de bijbehorende velden
4. Klik op **Opslaan**
5. Herstart de app

### Voor Ontwikkelaars:
De app gebruikt een **dubbele laag** voor API key management:

1. **Primaire Laag**: Gebruiker-ingevoerde keys (via `ApiKeyStorage`)
2. **Fallback Laag**: `BuildConfig` waarden (voor ontwikkeling)

```kotlin
// Voorbeeld: API key ophalen
val userKey = apiKeyStorage.getPreferences().first().apiSportsKey
val finalKey = if (userKey.isNotBlank()) userKey else BuildConfig.API_SPORTS_KEY
```

## 🚨 Probleemoplossing

### 403 Forbidden Fouten
**Symptoom**: Logcat toont `"x-apisports-key: your_api_sports_key_here"`

**Oplossing**:
1. Open Settings screen in de app
2. Voer een geldige API-Sports key in
3. Sla op en herstart de app
4. Controleer Logcat voor `"User key: abc12..."` (geeft aan dat gebruiker key wordt gebruikt)

### Geen Internet Verbinding
De app heeft een **offline modus** met:
- Cachede match data
- Demo voorspellingen
- Basis functionaliteit

### API Rate Limits
- API-Sports: 100 requests/dag (gratis)
- DeepSeek: 1000 tokens/maand (gratis)
- Tavily: 1000 requests/maand (gratis)

## 🔒 Beveiliging

### DataOpslag
- API keys worden versleuteld opgeslagen via `EncryptedSharedPreferences`
- Geen hardcoded keys in de codebase
- Keys blijven op het apparaat van de gebruiker

### Netwerk Veiligheid
- Alle API calls gebruiken HTTPS
- API keys worden als headers verzonden (niet in URL)
- Timeout en retry mechanismen

## 📊 Monitoring

### Logging
De app logt API key gebruik:
- `"User key: abc12..."` - Gebruiker key in gebruik
- `"BuildConfig key: xyz34..."` - Developer fallback in gebruik
- `"No API key configured"` - Geen key beschikbaar

### Foutmeldingen
- **403 Forbidden**: Ongeldige of ontbrekende API key
- **429 Too Many Requests**: Rate limit bereikt
- **Network Error**: Geen internet verbinding

## 🆘 Ondersteuning

### Voor Gebruikers:
1. Controleer je internetverbinding
2. Verifieer je API keys op de provider websites
3. Herstart de app
4. Contacteer app support als problemen aanhouden

### Voor Ontwikkelaars:
1. Check Logcat voor gedetailleerde fouten
2. Test API keys met curl/Postman
3. Verifieer `BuildConfig` waarden in `local.properties`
4. Controleer network interceptors in `AppContainer.kt`

## 📈 Toekomstige Verbeteringen

### Gepland:
1. **API Key Validatie**: Real-time validatie bij invoer
2. **Usage Dashboard**: In-app API usage monitoring
3. **Multi-provider Fallback**: Automatisch switchen tussen providers
4. **Key Rotation**: Automatische key vernieuwing

### In Overweging:
1. **OAuth Integratie**: Directe login bij API providers
2. **Key Sharing**: Veilig delen van keys tussen apparaten
3. **Enterprise Management**: Bedrijfs-wide key management

# API Key Setup for MatchMind AI

## Problem Identified
The application is failing with **403 Forbidden** errors when trying to fetch football data from API-Sports. The error logs show:

```
2025-12-15 20:18:55.575  7787-7787  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures?date=2025-12-15&timezone=Europe%2FBerlin
                                                                                                    -> x-apisports-key: your_api_sports_key_here
```

The API key being sent is a placeholder value `your_api_sports_key_here` instead of a valid API key.

## Root Cause
The `local.properties` file contains placeholder API keys that need to be replaced with actual API keys.

## Solution

### Step 1: Get Your API Keys

1. **API-Sports Key** (Required for football data):
   - Visit: https://dashboard.api-sports.io/register
   - Sign up for a free or paid plan
   - Copy your API key from the dashboard

2. **Tavily API Key** (Optional, for AI-powered search):
   - Visit: https://app.tavily.com/
   - Sign up for a free account
   - Copy your API key

### Step 2: Update local.properties

Open the file `C:/Users/BoetsAllroundService/AndroidStudioProjects/MatchMindAI/local.properties` and replace:

```properties
# Change this:
TAVILY_API_KEY=your_tavily_api_key_here
API_SPORTS_KEY=your_api_sports_key_here

# To this (with your actual keys):
TAVILY_API_KEY=your_actual_tavily_key_here
API_SPORTS_KEY=your_actual_api_sports_key_here
```

**Example:**
```properties
TAVILY_API_KEY=tvly-abc123def456ghi789
API_SPORTS_KEY=abc123def456ghi789jkl012
```

### Step 3: Rebuild the Project

After updating the `local.properties` file:

1. In Android Studio: **Build > Rebuild Project**
2. Or run from terminal: `./gradlew clean build`

### Step 4: Test the Application

Run the app again. The API requests should now work with your valid API keys.

## Alternative: Use User-Managed Keys in App

The MatchMind AI app also supports **User-Managed Security** where users can enter their own API keys within the app:

1. Open the MatchMind AI app
2. Go to **Settings** screen
3. Enter your API keys in the provided fields
4. The app will use these keys instead of the BuildConfig values

## Troubleshooting

If you still get 403 errors:

1. **Verify your API-Sports subscription is active**
2. **Check API key format**: API-Sports keys are typically long alphanumeric strings
3. **Test your API key** using curl or Postman:
   ```bash
   curl -X GET "https://v3.football.api-sports.io/fixtures?date=2025-12-15" \
     -H "x-apisports-key: YOUR_API_KEY_HERE"
   ```
4. **Check API rate limits**: Free plans have daily limits

## Security Notes

- The `local.properties` file is excluded from Git (see `.gitignore`)
- Never commit actual API keys to version control
- For production, consider using environment variables or a secure key store

## Need Help?

If you continue to experience issues:
1. Check the Android Studio Logcat for detailed error messages
2. Verify your internet connection
3. Ensure the API-Sports service is operational
4. Contact API-Sports support if you suspect account issues

# MatchMind AI 🧠⚽

**Smart football predictions powered by DeepSeek AI** - Live data analysis, cyber-minimalist Android app with betting insights. Built with Kotlin, Compose, and Clean Architecture.

## 🚀 Features

- **AI-Powered Predictions**: DeepSeek AI analyzes match statistics, team form, and news context
- **Live Football Data**: Real-time integration with API-Sports for fixtures, standings, and odds
- **Cyber-Minimalist Design**: Dark mode-first UI with neon green accents and futuristic aesthetics
- **Smart Match Curation**: Intelligent feed prioritizes "must-watch" games based on excitement scores
- **Prophet Module**: Advanced AI analysis combining hard stats with soft news context
- **Betting Analytics**: Beginner-friendly odds analysis with safety and value ratings
- **User-Managed Security**: API keys stored locally with encryption, never hardcoded

## 🏗️ Architecture

MatchMind AI follows **Clean Architecture** with strict separation of concerns:

```
Presentation Layer (UI) → Domain Layer (Business Logic) → Data Layer (Network/Persistence)
```

### Tech Stack
- **Language**: Kotlin 1.9+
- **UI**: 100% Jetpack Compose + Material Design 3
- **Networking**: Ktor Client with Kotlinx Serialization
- **Persistence**: Room Database + DataStore Preferences
- **DI**: Manual Dependency Injection (AppContainer)
- **Image Loading**: Coil with SVG support
- **Navigation**: Jetpack Navigation Compose

## 📱 Screens

### Dashboard Screen
- Curated feed with hero match highlighting
- Live ticker for ongoing matches
- League-based categorization
- Smart prioritization algorithm

### Match Detail Screen
- Comprehensive match analysis
- Predictions with win probability bars
- Team statistics and head-to-head data
- Injury reports and lineup information

### Chat Screen
- AI-powered conversation interface
- Prophet Module for deep match analysis
- Interactive widgets (predictions, odds, match cards)
- Suggested actions for follow-up questions

## 🔧 Installation

### Prerequisites
1. Android Studio (latest version)
2. JDK 11 or higher
3. API keys for:
   - DeepSeek AI (user-provided)
   - API-Sports (configured in local.properties)

### Setup
1. Clone the repository
2. Open in Android Studio
3. Configure API keys:
   - Copy `local.properties.backup` to `local.properties`
   - Add your API-Sports key: `API_SPORTS_KEY="your_key_here"`
4. Build and run the app

### API Key Configuration
The app uses **user-managed security**:
- DeepSeek API key is entered in the Settings screen
- API-Sports key is configured in `local.properties`
- All keys are stored locally with encryption
- No hardcoded credentials in the codebase

## 📊 Project Status

### Completed Phases
✅ **Phase 1**: Intelligence Engine Upgrade  
✅ **Phase 2**: Curator Dashboard - Smart Prioritized Feed  
✅ **Phase 3**: Prophet Module - Generative UI & Advanced AI Analysis  
✅ **Phase 22**: Dynamic League Discovery - Serialization Fix  
✅ **Phase 23**: Dynamic League Discovery + Beste Odds Engine  
✅ **Phase 25**: Time Display Improvements  

### Current Development
🔄 **Phase 26**: Advanced Betting Analytics (In Progress)

## 🎯 Roadmap

### Phase 27: Social Proof Integration
- Social media sentiment analysis
- Crowd wisdom integration
- Social proof-based betting recommendations

### Phase 28: Performance & Optimization
- Database schema updates for odds caching
- Real-time odds updates
- Live betting analytics

## 🤝 Contributing

### Development Setup
1. Follow the architectural guidelines in `docs/01_architecture.md`
2. Adhere to coding standards in `docs/04_coding_rules.md`
3. Use the tech stack defined in `docs/02_tech_stack.md`
4. Maintain the "Cyber-Minimalist" design from `docs/03_ux_ui_design.md`

### Code Standards
- **Kotlin First**: Use modern Kotlin features (coroutines, flows, sealed classes)
- **Clean Architecture**: Strict layer separation
- **Unidirectional Data Flow**: UI → ViewModel → UseCase → Repository
- **Error Handling**: Use `Result<T>` wrappers, no crashes allowed
- **Localization**: User-facing strings in Dutch, code in English

## 📄 License

This project is proprietary software. All rights reserved.

## 📞 Contact

For questions or support, please open an issue in the repository.

---

**Built with precision for football enthusiasts who appreciate data-driven insights.**

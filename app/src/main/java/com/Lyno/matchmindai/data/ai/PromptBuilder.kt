package com.Lyno.matchmindai.data.ai

import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.domain.model.NewsItemData
import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.domain.model.Injury
import com.Lyno.matchmindai.data.dto.football.PredictionsDto
import com.Lyno.matchmindai.data.dto.football.WinningPercentDto
import com.Lyno.matchmindai.data.dto.football.CoverageDto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Builder for constructing AI prompts using the "Anchor & Adjust" strategy.
 * This class is responsible for creating context-rich prompts that combine
 * hard statistical data with soft news context for AI analysis.
 */
class PromptBuilder {

    /**
     * Builds a Mastermind prediction prompt for advanced AI analysis.
     * This prompt is specifically designed for the "Mastermind" feature with
     * chaos scores, atmosphere analysis, and detailed scenario predictions.
     *
     * @param match The match fixture to analyze
     * @param predictions The statistical predictions from API-SPORTS
     * @param news List of relevant news items from Tavily
     * @param coverage Optional coverage information for the league
     * @param favoriteTeamName Optional favorite team name for personalization
     * @return A structured prompt for Mastermind AI analysis
     */
    fun buildMastermindPrompt(
        match: MatchFixture,
        predictions: PredictionsDto,
        news: List<NewsItemData>,
        coverage: CoverageDto? = null,
        favoriteTeamName: String? = null,
        favoriteTeamIds: Set<Int>? = null
    ): String {
        val today = getTodayDate()
        
        return buildString {
            // System prompt
            appendLine("Jij bent een Elite Voetbal Analist voor de Eredivisie/Eerste Divisie.")
            appendLine("TAAL: NEDERLANDS (Dutch).")
            appendLine("ANTWOORD UITSLUITEND IN HET NEDERLANDS. Vertaal alle inzichten, scenario's en tactische analyses naar het Nederlands.")
            appendLine()
            appendLine("Jouw doel: Analyseer data en nieuws om een winstgevend wed-advies te geven.")
            appendLine()
            
            // 1. ANCHOR: Hard facts first
            appendLine("DATUM VANDAAG: $today")
            appendLine()
            appendLine("📊 HARD FACTS (API-SPORTS):")
            appendLine("Wedstrijd: ${match.homeTeam} vs ${match.awayTeam}")
            appendLine("Competitie: ${match.league}")
            appendLine("Datum/Tijd: ${match.date} ${match.time}")
            
            if (match.fixtureId != null) {
                appendLine("Fixture ID: ${match.fixtureId}")
            }
            
            // Favorite team context
            val favoriteTeams = mutableListOf<String>()
            
            if (favoriteTeamName != null) {
                favoriteTeams.add(favoriteTeamName)
            }
            
            if (favoriteTeamIds != null && (match.homeTeamId in favoriteTeamIds || match.awayTeamId in favoriteTeamIds)) {
                val playingFavoriteTeams = mutableListOf<String>()
                if (match.homeTeamId in favoriteTeamIds) {
                    playingFavoriteTeams.add(match.homeTeam)
                }
                if (match.awayTeamId in favoriteTeamIds) {
                    playingFavoriteTeams.add(match.awayTeam)
                }
                
                if (playingFavoriteTeams.isNotEmpty()) {
                    appendLine()
                    appendLine("❤️ GEBRUIKERS CONTEXT:")
                    appendLine("De gebruiker heeft meerdere favoriete teams, waaronder: ${playingFavoriteTeams.joinToString(", ")}")
                    appendLine("⚠️ BELANGRIJK: Deze wedstrijd bevat een favoriet team van de gebruiker.")
                    appendLine("   Houd hier rekening mee in je analyse en taalgebruik.")
                }
            } else if (favoriteTeamName != null) {
                val isFavoriteTeamPlaying = match.homeTeam.contains(favoriteTeamName, ignoreCase = true) ||
                                           match.awayTeam.contains(favoriteTeamName, ignoreCase = true)
                
                if (isFavoriteTeamPlaying) {
                    appendLine()
                    appendLine("❤️ GEBRUIKERS CONTEXT:")
                    appendLine("De gebruiker is fan van: $favoriteTeamName")
                    appendLine("⚠️ BELANGRIJK: Deze wedstrijd bevat het favoriete team van de gebruiker.")
                    appendLine("   Houd hier rekening mee in je analyse en taalgebruik.")
                }
            }
            
            appendLine()
            appendLine("⚖️ STATISTISCHE KANSEN:")
            val winningPercent = predictions.winningPercentData
            if (winningPercent != null) {
                appendLine("• ${match.homeTeam} wint: ${winningPercent.home}%")
                appendLine("• Gelijk: ${winningPercent.draw}%")
                appendLine("• ${match.awayTeam} wint: ${winningPercent.away}%")
            } else {
                appendLine("• Geen statistische kansen beschikbaar voor deze wedstrijd.")
            }
            appendLine()
            val advice = predictions.advice ?: "Geen advies beschikbaar"
            appendLine("💡 API ADVIES: $advice")
            
            // Coverage awareness
            if (coverage != null && !coverage.predictions) {
                appendLine()
                appendLine("⚠️ WAARSCHUWING: Geen statistisch model beschikbaar voor deze competitie.")
                appendLine("   Baseer je analyse op algemene vorm en nieuwscontext.")
            }
            
            // 2. ADJUST: News context
            if (news.isNotEmpty()) {
                appendLine()
                appendLine("📰 NIEUWS CONTEXT (TAVILY):")
                news.take(5).forEachIndexed { index, newsItem ->
                    appendLine("${index + 1}. ${newsItem.headline}")
                    appendLine("   Bron: ${newsItem.source}")
                    if (newsItem.snippet != null) {
                        appendLine("   Samenvatting: ${newsItem.snippet}")
                    }
                    if (newsItem.publishedDate != null) {
                        appendLine("   Publicatiedatum: ${newsItem.publishedDate}")
                    }
                    appendLine()
                }
            } else {
                appendLine()
                appendLine("📰 NIEUWS CONTEXT: Geen recent nieuws gevonden.")
            }
            
            // 3. MASTERMIND ANALYSIS INSTRUCTIONS
            appendLine()
            appendLine("🎯 MASTERMIND ANALYSE INSTRUCTIES:")
            appendLine("1. Chaos Meter (0-100): Hoe onvoorspelbaar is deze wedstrijd?")
            appendLine("   - Derby's, rivaliteiten, veel kaarten = hoog chaos")
            appendLine("   - Tactische, gecontroleerde wedstrijden = laag chaos")
            appendLine()
            appendLine("2. Atmosfeer Score (0-100): Impact van het thuispubliek/stadion.")
            appendLine("   - Heksenketel, vol stadion = hoog atmosfeer")
            appendLine("   - Leeg stadion, neutrale locatie = laag atmosfeer")
            appendLine()
            appendLine("3. Primair Scenario: Het meest waarschijnlijke 'script' van de wedstrijd.")
            appendLine("   - Korte titel (max 5 woorden)")
            appendLine("   - Gedetailleerd verloop van de wedstrijd")
            appendLine()
            appendLine("4. Tactische Sleutel: Eén tactisch punt waarop de wedstrijd beslist wordt.")
            appendLine()
            appendLine("5. Wedtip & Vertrouwen: Specifiek advies met zekerheidspercentage.")
            appendLine("   - Bijv. 'Thuis wint & BTTS' of 'Over 2.5 Goals'")
            appendLine("   - Vertrouwen: 0-100%")
            appendLine()
            appendLine("6. Voorspelde Score: Realistische eindstand.")
            
            // Special handling for postponed matches
            if (match.status == "PST") {
                appendLine()
                appendLine("⏸️ UITGESTELDE WEDSTRIJD:")
                appendLine("Deze wedstrijd is uitgesteld. Leg uit waarom (op basis van Tavily nieuws).")
            }
            
            // 4. Output format
            appendLine()
            appendLine("📋 OUTPUT FORMAT:")
            appendLine("Je MOET antwoorden in valide JSON formaat:")
            appendLine("{")
            appendLine("  \"chaos_score\": 0-100,")
            appendLine("  \"atmosphere_score\": 0-100,")
            appendLine("  \"primary_scenario_title\": \"Korte titel (max 5 woorden)\",")
            appendLine("  \"primary_scenario_desc\": \"Gedetailleerd verloop van de wedstrijd.\",")
            appendLine("  \"tactical_key\": \"Eén tactisch punt waarop de wedstrijd beslist wordt.\",")
            appendLine("  \"betting_tip\": \"Specifiek advies (bijv. 'Thuis wint & BTTS' of 'Over 2.5 Goals')\",")
            appendLine("  \"betting_confidence\": 0-100,")
            appendLine("  \"predicted_score\": \"2-1\"")
            appendLine("}")
            
            // 5. Language reminder
            appendLine()
            appendLine("🌐 TAAL: Antwoord altijd in het NEDERLANDS met bovenstaand JSON formaat.")
        }
    }
    
    /**
     * Builds a general analysis prompt for matches without specific predictions.
     */
    fun buildGeneralAnalysisPrompt(
        match: MatchFixture,
        news: List<NewsItemData>
    ): String {
        val today = getTodayDate()
        
        return buildString {
            appendLine("DATUM VANDAAG: $today")
            appendLine()
            appendLine("📊 WEDSTRIJD ANALYSE:")
            appendLine("Wedstrijd: ${match.homeTeam} vs ${match.awayTeam}")
            appendLine("Competitie: ${match.league}")
            appendLine("Datum/Tijd: ${match.date} ${match.time}")
            
            if (match.status == "PST") {
                appendLine("Status: UITGESTELD")
            } else if (match.status != null) {
                appendLine("Status: ${match.status}")
            }
            
            if (news.isNotEmpty()) {
                appendLine()
                appendLine("📰 NIEUWS CONTEXT:")
                news.take(3).forEachIndexed { index, newsItem ->
                    appendLine("${index + 1}. ${newsItem.headline}")
                    if (newsItem.snippet != null) {
                        appendLine("   ${newsItem.snippet}")
                    }
                    appendLine()
                }
            }
            
            appendLine()
            appendLine("🎯 ANALYSE INSTRUCTIES:")
            appendLine("1. Analyseer de wedstrijd op basis van beschikbare informatie.")
            appendLine("2. Identificeer belangrijke factoren die de uitkomst kunnen beïnvloeden.")
            appendLine("3. Geef een gebalanceerde analyse met zowel kansen als risico's.")
            appendLine("4. Antwoord in het Nederlands.")
            
            appendLine()
            appendLine("📋 OUTPUT FORMAT:")
            appendLine("{")
            appendLine("  \"analysis\": \"Jouw analyse...\",")
            appendLine("  \"keyFactors\": [\"Factor 1\", \"Factor 2\"],")
            appendLine("  \"prediction\": \"Jouw voorspelling (bijv. 'Thuiswinst', 'Uitwinst', 'Gelijk')\",")
            appendLine("  \"confidence\": \"hoog/medium/laag\",")
            appendLine("  \"risk\": \"Belangrijkste risico...\"")
            appendLine("}")
        }
    }
    
    /**
     * Builds a LLMGRADE context extraction prompt for advanced analysis.
     * This prompt extracts context factors and identifies outlier scenarios.
     *
     * @param matchDetail Detailed match information
     * @param newsItems List of relevant news items
     * @param oraclePrediction Optional Oracle prediction for context
     * @param tesseractPrediction Optional Tesseract prediction for context
     * @return A structured prompt for LLMGRADE context extraction
     */
    fun buildLLMGradeContextExtractionPrompt(
        matchDetail: MatchDetail,
        newsItems: List<NewsItemData>,
        oraclePrediction: String? = null,
        tesseractPrediction: String? = null,
        favoriteTeamIds: Set<Int>? = null,
        injuries: List<Injury> = emptyList()
    ): String {
        val today = getTodayDate()
        
        return buildString {
            // System prompt
            appendLine("Jij bent een Elite Voetbal Analist gespecialiseerd in contextuele factoren.")
            appendLine("TAAL: NEDERLANDS (Dutch).")
            appendLine("ANTWOORD UITSLUITEND IN HET NEDERLANDS.")
            appendLine()
            appendLine("Jouw doel: Analyseer ongestructureerde data om contextfactoren en uitschieterscenario's te identificeren.")
            appendLine()
            
            // 1. Match context
            appendLine("DATUM VANDAAG: $today")
            appendLine()
            appendLine("⚽ WEDSTRIJD CONTEXT:")
            appendLine("Wedstrijd: ${matchDetail.homeTeam} vs ${matchDetail.awayTeam}")
            appendLine("Competitie: ${matchDetail.league ?: "Onbekend"}")
            
            if (matchDetail.status != null) {
                appendLine("Status: ${matchDetail.status.displayName}")
            }
            
            // User context for favorite teams
            if (favoriteTeamIds != null) {
                val playingFavoriteTeams = mutableListOf<String>()
                if (matchDetail.homeTeamId in favoriteTeamIds) {
                    playingFavoriteTeams.add(matchDetail.homeTeam)
                }
                if (matchDetail.awayTeamId in favoriteTeamIds) {
                    playingFavoriteTeams.add(matchDetail.awayTeam)
                }
                
                if (playingFavoriteTeams.isNotEmpty()) {
                    appendLine()
                    appendLine("❤️ GEBRUIKERS CONTEXT:")
                    appendLine("De gebruiker heeft meerdere favoriete teams, waaronder: ${playingFavoriteTeams.joinToString(", ")}")
                    appendLine("Houd hier rekening mee in je contextuele analyse.")
                }
            }
            
            // 2. Existing predictions (if available)
            if (oraclePrediction != null || tesseractPrediction != null) {
                appendLine()
                appendLine("📊 BESTAANDE VOORSPELLINGEN:")
                if (oraclePrediction != null) {
                    appendLine("• Oracle (AI): $oraclePrediction")
                }
                if (tesseractPrediction != null) {
                    appendLine("• Tesseract (Stats): $tesseractPrediction")
                }
            }
            
            // 3. Injuries context (NEW)
            if (injuries.isNotEmpty()) {
                appendLine()
                appendLine("🏥 BLESSURES (API-SPORTS):")
                val homeTeamInjuries = injuries.filter { it.team == matchDetail.homeTeam }
                val awayTeamInjuries = injuries.filter { it.team == matchDetail.awayTeam }
                
                if (homeTeamInjuries.isNotEmpty()) {
                    appendLine("${matchDetail.homeTeam}:")
                    homeTeamInjuries.forEach { injury ->
                        appendLine("  • ${injury.playerName} - ${injury.type} (${injury.reason})")
                        if (injury.expectedReturn != null) {
                            appendLine("    Verwacht terug: ${injury.expectedReturn}")
                        }
                    }
                }
                
                if (awayTeamInjuries.isNotEmpty()) {
                    appendLine("${matchDetail.awayTeam}:")
                    awayTeamInjuries.forEach { injury ->
                        appendLine("  • ${injury.playerName} - ${injury.type} (${injury.reason})")
                        if (injury.expectedReturn != null) {
                            appendLine("    Verwacht terug: ${injury.expectedReturn}")
                        }
                    }
                }
                
                // Calculate injury impact
                val homeImpact = calculateInjuryImpact(homeTeamInjuries)
                val awayImpact = calculateInjuryImpact(awayTeamInjuries)
                appendLine()
                appendLine("🏥 BLESSURE IMPACT SCORE:")
                appendLine("  • ${matchDetail.homeTeam}: $homeImpact/10")
                appendLine("  • ${matchDetail.awayTeam}: $awayImpact/10")
            } else {
                appendLine()
                appendLine("🏥 BLESSURES: Geen blessures gemeld voor deze wedstrijd.")
            }
            
            // 4. News context
            if (newsItems.isNotEmpty()) {
                appendLine()
                appendLine("📰 NIEUWS CONTEXT:")
                newsItems.take(5).forEachIndexed { index, newsItem ->
                    appendLine("${index + 1}. ${newsItem.headline}")
                    if (newsItem.snippet != null) {
                        appendLine("   Samenvatting: ${newsItem.snippet}")
                    }
                    if (newsItem.publishedDate != null) {
                        appendLine("   Publicatiedatum: ${newsItem.publishedDate}")
                    }
                    appendLine()
                }
            } else {
                appendLine()
                appendLine("📰 NIEUWS CONTEXT: Geen recent nieuws gevonden.")
            }
            
            // 5. LLMGRADE analysis instructions
            appendLine()
            appendLine("🎯 LLMGRADE ANALYSE INSTRUCTIES:")
            appendLine("1. CONTEXTFACTOREN: Identificeer en score de volgende factoren (1-10):")
            appendLine("   • Teammorale - Recente resultaten, sfeer in de groep")
            appendLine("   • Blessureproblematiek - Belangrijke spelers uit de selectie (Gebruik de blessuredata hierboven!)")
            appendLine("   • Tactische veranderingen - Nieuwe coach, nieuwe formatie")
            appendLine("   • Weersinvloeden - Regen, wind, extreme temperaturen")
            appendLine("   • Externe drukfactoren - Degradatiegevaar, titelstrijd")
            appendLine("   • Historische anomalieën - Rare patronen in H2H")
            appendLine()
            appendLine("2. UITSCHIETERSCENARIO'S: Identificeer mogelijke uitschieters die traditionele modellen missen.")
            appendLine("   • Beschrijf 3-5 mogelijke onverwachte scenario's")
            appendLine("   • Geef elke scenario een waarschijnlijkheid (0-100%)")
            appendLine("   • Noem ondersteunende factoren (inclusief blessures!)")
            appendLine("   • Geef historische precedenten (indien beschikbaar)")
            appendLine()
            appendLine("3. VERBETERDE REDENERING: Geef een samenvatting van je analyse in natuurlijke taal.")
            
            // 6. Output format
            appendLine()
            appendLine("📋 OUTPUT FORMAT:")
            appendLine("Je MOET antwoorden in valide JSON formaat:")
            appendLine("{")
            appendLine("  \"context_factors\": [")
            appendLine("    {")
            appendLine("      \"type\": \"TEAM_MORALE\",")
            appendLine("      \"score\": 7,")
            appendLine("      \"description\": \"Laatste 3 wedstrijden ongeslagen, sfeer is goed\",")
            appendLine("      \"weight\": 1.2")
            appendLine("    }")
            appendLine("  ],")
            appendLine("  \"outlier_scenarios\": [")
            appendLine("    {")
            appendLine("      \"description\": \"Thuisploeg wint verrassend ondanks onderdogstatus\",")
            appendLine("      \"probability\": 35.5,")
            appendLine("      \"supporting_factors\": [\"Blessure bij ster speler uitploeg\", \"Historisch goed thuisrecord\"],")
            appendLine("      \"historical_precedents\": [\"Vergelijkbare situatie in 2023: 2-1 winst\"],")
            appendLine("      \"impact_score\": 8")
            appendLine("    }")
            appendLine("  ],")
            appendLine("  \"enhanced_reasoning\": \"Samenvatting van de analyse in natuurlijke taal...\"")
            appendLine("}")
            
            // 7. Language reminder
            appendLine()
            appendLine("🌐 TAAL: Antwoord altijd in het NEDERLANDS met bovenstaand JSON formaat.")
            appendLine("   Gebruik de exacte type namen: TEAM_MORALE, INJURIES, TACTICAL_CHANGES, WEATHER, PRESSURE, HISTORICAL_ANOMALY")
        }
    }
    
    /**
     * Builds a LLMGRADE outlier detection prompt specifically for identifying
     * unexpected outcomes that traditional models might miss.
     */
    fun buildLLMGradeOutlierDetectionPrompt(
        matchDetail: MatchDetail,
        historicalData: List<String>,
        basePrediction: String,
        contextFactors: List<String>
    ): String {
        val today = getTodayDate()
        
        return buildString {
            appendLine("DATUM VANDAAG: $today")
            appendLine()
            appendLine("🎯 UITSCHIETER DETECTIE ANALYSE:")
            appendLine("Wedstrijd: ${matchDetail.homeTeam} vs ${matchDetail.awayTeam}")
            appendLine("Basisvoorspelling: $basePrediction")
            appendLine()
            
            if (contextFactors.isNotEmpty()) {
                appendLine("📋 CONTEXTFACTOREN:")
                contextFactors.forEach { factor ->
                    appendLine("• $factor")
                }
                appendLine()
            }
            
            if (historicalData.isNotEmpty()) {
                appendLine("📊 HISTORISCHE DATA:")
                historicalData.take(10).forEach { data ->
                    appendLine("• $data")
                }
                appendLine()
            }
            
            appendLine("🎯 ANALYSE INSTRUCTIES:")
            appendLine("1. Identificeer 3-5 uitschieterscenario's die waarschijnlijker zijn dan de basisvoorspelling suggereert.")
            appendLine("2. Focus op scenario's die traditionele modellen zouden missen.")
            appendLine("3. Geef elke scenario:")
            appendLine("   • Een duidelijke beschrijving")
            appendLine("   • Waarschijnlijkheid (0-100%)")
            appendLine("   • Ondersteunende factoren")
            appendLine("   • Historische precedenten")
            appendLine()
            appendLine("4. Rangschik de scenario's op waarschijnlijkheid.")
            
            appendLine()
            appendLine("📋 OUTPUT FORMAT: JSON zoals in de context extractie prompt.")
        }
    }
    
    /**
     * Gets today's date in Dutch format.
     */
    private fun getTodayDate(): String {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("nl", "NL"))
        return dateFormat.format(Date())
    }
    
    /**
     * Extracts news headlines for quick reference.
     */
    private fun extractNewsHeadlines(news: List<NewsItemData>): List<String> {
        return news.map { it.headline }
    }
    
    /**
     * Calculates if news is conflicting with statistics.
     * This is a simple heuristic - in production, this would be more sophisticated.
     */
    private fun isNewsConflicting(
        predictions: PredictionsDto,
        news: List<NewsItemData>
    ): Boolean {
        // Simple heuristic: if there are injury news for the favorite team
        val winningPercent = predictions.winningPercentData
        if (winningPercent == null) {
            // No predictions available, can't determine conflict
            return false
        }
        
        val homePercent = winningPercent.home.toIntOrNull() ?: 0
        val awayPercent = winningPercent.away.toIntOrNull() ?: 0
        
        val favoriteTeam = if (homePercent > awayPercent) "home" else "away"
        
        val newsText = news.joinToString(" ") { it.headline + " " + (it.snippet ?: "") }
        
        // Check for negative keywords for the favorite team
        val negativeKeywords = listOf("blessure", "gekwetst", "out", "geschorst", "ontbreekt", "mist")
        
        return negativeKeywords.any { keyword ->
            newsText.contains(keyword, ignoreCase = true)
        }
    }
    
    /**
     * Calculates injury impact score (0-10) based on injuries.
     * Higher score = more severe impact on team performance.
     */
    private fun calculateInjuryImpact(injuries: List<Injury>): Int {
        if (injuries.isEmpty()) {
            return 0
        }
        
        var totalImpact = 0
        for (injury in injuries) {
            val severityScore = when (injury.type.lowercase()) {
                "out" -> 8
                "doubtful" -> 5
                "questionable" -> 3
                "probable" -> 1
                else -> 2 // Default for unknown types
            }
            
            // Check if it's a key player (simplified heuristic)
            val isKeyPlayer = injury.playerName.contains("captain", ignoreCase = true) ||
                              injury.playerName.contains("star", ignoreCase = true) ||
                              injury.playerName.contains("top", ignoreCase = true) ||
                              injury.reason.contains("hamstring", ignoreCase = true) ||
                              injury.reason.contains("knee", ignoreCase = true) ||
                              injury.reason.contains("ankle", ignoreCase = true)
            
            val keyPlayerMultiplier = if (isKeyPlayer) 1.5 else 1.0
            
            totalImpact += (severityScore * keyPlayerMultiplier).toInt()
        }
        
        // Normalize to 0-10 scale
        return minOf(10, totalImpact / maxOf(1, injuries.size))
    }
}

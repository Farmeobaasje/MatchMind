package com.Lyno.matchmindai.data.ai

import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.domain.model.NewsItemData
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
     * Builds a prediction prompt using the "Anchor & Adjust" strategy.
     *
     * @param match The match fixture to analyze
     * @param predictions The statistical predictions from API-SPORTS
     * @param news List of relevant news items from Tavily
     * @param coverage Optional coverage information for the league
     * @param favoriteTeamName Optional favorite team name for personalization (Phase 4)
     * @return A structured prompt for AI analysis
     */
    fun buildPredictionPrompt(
        match: MatchFixture,
        predictions: PredictionsDto,
        news: List<NewsItemData>,
        coverage: CoverageDto? = null,
        favoriteTeamName: String? = null
    ): String {
        val today = getTodayDate()
        
        return buildString {
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
            
            // Phase 4: Add favorite team context
            if (favoriteTeamName != null) {
                appendLine()
                appendLine("❤️ GEBRUIKERS CONTEXT:")
                appendLine("De gebruiker is fan van: $favoriteTeamName")
                
                // Check if favorite team is playing
                val isFavoriteTeamPlaying = match.homeTeam.contains(favoriteTeamName, ignoreCase = true) ||
                                           match.awayTeam.contains(favoriteTeamName, ignoreCase = true)
                
                if (isFavoriteTeamPlaying) {
                    appendLine("⚠️ BELANGRIJK: Deze wedstrijd bevat het favoriete team van de gebruiker.")
                    appendLine("   Houd hier rekening mee in je analyse en taalgebruik.")
                    appendLine("   Gebruik 'we' en 'ons' wanneer je verwijst naar $favoriteTeamName.")
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
            
            // Add coverage awareness
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
            
            // 3. INSTRUCTION: Analysis guidance
            appendLine()
            appendLine("🎯 ANALYSE INSTRUCTIES:")
            appendLine("1. Analyseer of het nieuws conflicteert met de statistieken.")
            appendLine("2. Als een belangrijke speler geblesseerd is, pas het vertrouwen omlaag aan.")
            appendLine("3. Als er positief nieuws is (bijv. thuissupport), pas het vertrouwen omhoog aan.")
            appendLine("4. Identificeer één 'Killer Scenario' (Risico) waarom deze voorspelling zou kunnen mislukken.")
            
            // Phase 4: Add favorite team specific instructions
            if (favoriteTeamName != null) {
                val isFavoriteTeamPlaying = match.homeTeam.contains(favoriteTeamName, ignoreCase = true) ||
                                           match.awayTeam.contains(favoriteTeamName, ignoreCase = true)
                
                if (isFavoriteTeamPlaying) {
                    appendLine("5. SPECIALE INSTRUCTIE: Deze wedstrijd bevat het favoriete team van de gebruiker.")
                    appendLine("   - Gebruik 'we' en 'ons' wanneer je verwijst naar $favoriteTeamName.")
                    appendLine("   - Wees realistisch maar positief over hun kansen.")
                    appendLine("   - Noem specifiek nieuws over $favoriteTeamName als dat relevant is.")
                }
            }
            
            // 4. Special handling for postponed matches
            if (match.status == "PST") {
                appendLine()
                appendLine("⏸️ UITGESTELDE WEDSTRIJD:")
                appendLine("Deze wedstrijd is uitgesteld. Leg uit waarom (op basis van Tavily nieuws) in plaats van een score te voorspellen.")
            }
            
            // 5. Output format
            appendLine()
            appendLine("📋 OUTPUT FORMAT:")
            appendLine("Geef je antwoord in JSON met dit formaat:")
            appendLine("{")
            appendLine("  \"analysis\": \"Jouw gedetailleerde analyse in het Nederlands...\",")
            appendLine("  \"adjustedHomeChance\": 55, // Aangepaste kans voor thuiswinst (0-100)")
            appendLine("  \"adjustedAwayChance\": 30, // Aangepaste kans voor uitwinst (0-100)")
            appendLine("  \"adjustedDrawChance\": 15, // Aangepaste kans voor gelijkspel (0-100)")
            appendLine("  \"riskFactor\": \"Beschrijving van het belangrijkste risico...\",")
            appendLine("  \"confidenceScore\": 75, // Vertrouwensscore (0-100)")
            appendLine("  \"keyInsights\": [\"Inzicht 1\", \"Inzicht 2\", \"Inzicht 3\"],")
            appendLine("  \"recommendation\": \"Jouw aanbeveling (bijv. 'Thuiswinst', 'Uitwinst', 'Gelijk')\"")
            appendLine("}")
            
            // 6. Language reminder
            appendLine()
            appendLine("🌐 TAAL: Antwoord altijd in het NEDERLANDS.")
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
}

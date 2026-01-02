    package com.Lyno.matchmindai.domain.ai

import com.Lyno.matchmindai.data.dto.football.FixtureLineupsResponse
import com.Lyno.matchmindai.data.dto.football.TeamStatisticsResponse
import com.Lyno.matchmindai.data.dto.FixtureResponse
import kotlinx.serialization.Serializable

/**
 * DeepChi Prompts for generating AI analysis of football matches.
 * Constructs structured prompts for DeepSeek LLM to analyze fatigue, style matchup, and lineup strength.
 */
object DeepChiPrompts {

    /**
     * Data container for team analysis input.
     */
    @Serializable
    data class TeamAnalysisInput(
        val teamName: String,
        val lastMatches: List<FixtureResponse>,
        val teamStats: TeamStatisticsResponse?,
        val lineup: List<FixtureLineupsResponse>?
    )

    /**
     * Data container for match analysis input.
     */
    @Serializable
    data class MatchAnalysisInput(
        val homeTeam: TeamAnalysisInput,
        val awayTeam: TeamAnalysisInput,
        val fixtureDate: String,
        val venueCity: String
    )

    /**
     * Builds the complete prompt for DeepSeek analysis.
     * @param input The match analysis input data
     * @return The formatted prompt string
     */
    fun buildAnalysisPrompt(input: MatchAnalysisInput): String {
        return """
            JIJ BENT EEN VOETBAL DATA ANALIST.
            
            Input Data:
            
            1. SCHEMA & FATIGUE ANALYSE
            - ${input.homeTeam.teamName} laatste 5 wedstrijden:
            ${formatLastMatches(input.homeTeam.lastMatches)}
            - ${input.awayTeam.teamName} laatste 5 wedstrijden:
            ${formatLastMatches(input.awayTeam.lastMatches)}
            
            2. STIJL & TACTIEK ANALYSE
            - ${input.homeTeam.teamName} statistieken:
            ${formatTeamStats(input.homeTeam.teamStats)}
            - ${input.awayTeam.teamName} statistieken:
            ${formatTeamStats(input.awayTeam.teamStats)}
            
            3. OPSTELLING & SPELERS ANALYSE
            - ${input.homeTeam.teamName} opstelling:
            ${formatLineup(input.homeTeam.lineup)}
            - ${input.awayTeam.teamName} opstelling:
            ${formatLineup(input.awayTeam.lineup)}
            
            TAAK:
            
            1. Bereken FatigueScore (0-100) voor beide teams gebaseerd op:
               - Rustdagen tussen wedstrijden
               - Reisafstanden (thuis/uit)
               - Wedstrijdintensiteit (aantal wedstrijden in korte tijd)
               - 0 = Perfect uitgerust, 100 = Volledig uitgeput
            
            2. Bereken StyleMatchup (0.5-1.5) voor de thuisploeg:
               - 0.5 = Sterk nadeel, 1.0 = Neutraal, 1.5 = Sterk voordeel
               - Baseer op formatie clash, scorende fases, defensieve stabiliteit
               - Voorbeeld: Als Team A veel scoort in minuut 60-75 en Team B zwak is in die fase → voordeel
            
            3. Bereken LineupStrength (0-100) voor beide teams:
               - 0 = Zwakste mogelijke opstelling, 100 = Sterkste mogelijke opstelling
               - Baseer op ontbrekende sleutelspelers, formatie stabiliteit, coach kwaliteit
            
            4. Geef een korte, zakelijke reden voor elke berekening.
            
            OUTPUT (JSON FORMAT):
            {
              "fatigue_home": 45,
              "fatigue_away": 60,
              "style_matchup": 1.1,
              "lineup_strength_home": 90,
              "lineup_strength_away": 85,
              "reasoning": "Team A heeft 2 dagen rust minder, maar Team B speelt met een B-keeper."
            }
            
            REGELS:
            - Gebruik alleen de gegeven data
            - Wees objectief en data-gedreven
            - Geef concrete cijfers, geen vage taal
            - Output MOET geldige JSON zijn
        """.trimIndent()
    }

    /**
     * Formats last matches for prompt.
     */
    private fun formatLastMatches(matches: List<FixtureResponse>): String {
        if (matches.isEmpty()) return "Geen data beschikbaar"
        
        return matches.joinToString("\n") { match ->
            val date = match.fixture?.date?.substringBefore("T") ?: "Onbekend"
            val home = match.teams?.home?.name ?: "Onbekend"
            val away = match.teams?.away?.name ?: "Onbekend"
            "- $date: $home vs $away"
        }
    }

    /**
     * Formats team statistics for prompt.
     */
    private fun formatTeamStats(stats: TeamStatisticsResponse?): String {
        if (stats == null) return "Geen statistieken beschikbaar"
        
        return buildString {
            appendLine("  - Meest gebruikte formaties:")
            stats.lineups?.take(3)?.forEach { lineup ->
                appendLine("    * ${lineup.formation} (${lineup.played} wedstrijden)")
            }
            
            stats.goals?.let { goals ->
                appendLine("  - Scorende fases (goals per minuut):")
                goals.`for`.minute?.entries?.sortedBy { it.key }?.forEach { (minute, minuteStats) ->
                    val percentage = minuteStats.percentage ?: "0%"
                    appendLine("    * $minute': ${minuteStats.total ?: 0} goals ($percentage)")
                }
                
                appendLine("  - Defensieve stabiliteit:")
                val cleanSheets = goals.against.total.total - goals.against.total.home - goals.against.total.away
                appendLine("    * Clean sheets: $cleanSheets")
            }
            
            stats.cards?.let { cards ->
                appendLine("  - Discipline:")
                val yellowCards = sumCardStats(cards.yellow)
                val redCards = sumCardStats(cards.red)
                appendLine("    * Gele kaarten: $yellowCards")
                appendLine("    * Rode kaarten: $redCards")
            }
        }
    }

    /**
     * Sums card statistics across all minute intervals.
     */
    private fun sumCardStats(cardStats: com.Lyno.matchmindai.data.dto.football.TeamStatsCardStats): Int {
        return listOf(
            cardStats.`0-15`?.total,
            cardStats.`16-30`?.total,
            cardStats.`31-45`?.total,
            cardStats.`46-60`?.total,
            cardStats.`61-75`?.total,
            cardStats.`76-90`?.total,
            cardStats.`91-105`?.total,
            cardStats.`106-120`?.total
        ).sumOf { it ?: 0 }
    }

    /**
     * Formats lineup for prompt.
     */
    private fun formatLineup(lineup: List<FixtureLineupsResponse>?): String {
        if (lineup == null || lineup.isEmpty()) return "Opstelling nog niet bekend"
        
        return buildString {
            lineup.forEachIndexed { index, teamLineup ->
                appendLine("  - Team ${index + 1}:")
                appendLine("    * Formaties: ${teamLineup.formation}")
                appendLine("    * Coach: ${teamLineup.coach?.name ?: "Onbekend"}")
                
                if (teamLineup.startXI.isNotEmpty()) {
                    appendLine("    * Startende XI:")
                    teamLineup.startXI.take(3).forEach { playerPos ->
                        val player = playerPos.player
                        appendLine("      - ${player.name} (${player.pos ?: "Onbekend"})")
                    }
                    if (teamLineup.startXI.size > 3) {
                        appendLine("      - ... en ${teamLineup.startXI.size - 3} andere spelers")
                    }
                }
                
                if (!teamLineup.missing.isNullOrEmpty()) {
                    appendLine("    * Ontbrekende spelers:")
                    teamLineup.missing.take(2).forEach { missing ->
                        appendLine("      - ${missing.player.name} (${missing.reason ?: "Onbekende reden"})")
                    }
                    if (teamLineup.missing.size > 2) {
                        appendLine("      - ... en ${teamLineup.missing.size - 2} andere afwezigen")
                    }
                }
                if (index < lineup.size - 1) appendLine()
            }
        }
    }

    /**
     * Parses the AI response JSON into structured data.
     */
    @Serializable
    data class DeepChiResponse(
        val fatigue_home: Int,
        val fatigue_away: Int,
        val style_matchup: Double,
        val lineup_strength_home: Int,
        val lineup_strength_away: Int,
        val reasoning: String
    ) {
        init {
            require(fatigue_home in 0..100) { "fatigue_home must be between 0 and 100" }
            require(fatigue_away in 0..100) { "fatigue_away must be between 0 and 100" }
            require(style_matchup in 0.5..1.5) { "style_matchup must be between 0.5 and 1.5" }
            require(lineup_strength_home in 0..100) { "lineup_strength_home must be between 0 and 100" }
            require(lineup_strength_away in 0..100) { "lineup_strength_away must be between 0 and 100" }
        }
    }
}

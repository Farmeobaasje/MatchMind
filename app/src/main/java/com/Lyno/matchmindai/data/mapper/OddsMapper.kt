package com.Lyno.matchmindai.data.mapper

import com.Lyno.matchmindai.data.dto.football.OddsResponseDto
import com.Lyno.matchmindai.data.dto.football.LiveOddsResponseDto
import com.Lyno.matchmindai.data.dto.football.SimplifiedOddsDto
import com.Lyno.matchmindai.data.dto.football.OddsFixtureDto
import com.Lyno.matchmindai.data.dto.football.BookmakerDto
import com.Lyno.matchmindai.data.dto.football.BetDto
import com.Lyno.matchmindai.data.dto.football.BetValueDto
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.domain.model.OddsData
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

/**
 * Mapper for converting odds DTOs to domain models and simplified representations.
 * This mapper focuses on extracting beginner-friendly betting information.
 */
object OddsMapper {

    private val dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    /**
     * Convert OddsResponseDto to a list of SimplifiedOddsDto for AI analysis.
     * This extracts key betting information suitable for beginners.
     */
    fun toSimplifiedOddsList(
        oddsResponse: OddsResponseDto,
        matchFixtures: List<MatchFixture> = emptyList()
    ): List<SimplifiedOddsDto> {
        return oddsResponse.response.mapNotNull { oddsFixture ->
            toSimplifiedOdds(oddsFixture, matchFixtures)
        }
    }

    /**
     * Convert OddsResponseDto to domain OddsData model for a specific fixture.
     * This creates the domain model used in MatchDetail.
     */
    fun toOddsData(
        oddsResponse: OddsResponseDto,
        fixtureId: Int,
        homeTeam: String,
        awayTeam: String
    ): OddsData? {
        val oddsFixture = oddsResponse.response.find { it.fixture.id == fixtureId } ?: return null
        val bestBookmaker = findBestBookmakerForBeginners(oddsFixture.bookmakers)
        
        val (homeWinOdds, drawOdds, awayWinOdds) = extractMatchResultOdds(bestBookmaker)
        val overUnderOdds = extractOverUnderOdds(bestBookmaker)
        val bothTeamsToScoreOdds = extractBothTeamsToScoreOdds(bestBookmaker)
        
        val valueRating = calculateValueRating(homeWinOdds, drawOdds, awayWinOdds)
        val safetyRating = calculateSafetyRating(homeWinOdds, drawOdds, awayWinOdds)
        val highestOdds = listOfNotNull(homeWinOdds, drawOdds, awayWinOdds).maxOrNull()
        
        return OddsData(
            fixtureId = fixtureId,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            bookmakerName = bestBookmaker?.name ?: "Unknown",
            homeWinOdds = homeWinOdds,
            drawOdds = drawOdds,
            awayWinOdds = awayWinOdds,
            overUnderOdds = overUnderOdds,
            bothTeamsToScoreOdds = bothTeamsToScoreOdds,
            valueRating = valueRating,
            safetyRating = safetyRating,
            highestOdds = highestOdds,
            lastUpdated = oddsFixture.fixture.date
        )
    }

    /**
     * Convert a single OddsFixtureDto to SimplifiedOddsDto.
     */
    fun toSimplifiedOdds(
        oddsFixture: OddsFixtureDto,
        matchFixtures: List<MatchFixture> = emptyList()
    ): SimplifiedOddsDto? {
        val fixtureId = oddsFixture.fixture.id
        
        // Try to find match details from provided fixtures
        val matchFixture = matchFixtures.find { it.fixtureId == fixtureId }
        
        // Extract team names from match fixture or use placeholder
        val homeTeam = matchFixture?.homeTeam ?: "Home Team"
        val awayTeam = matchFixture?.awayTeam ?: "Away Team"
        
        // Find the most popular bookmaker (Bet365, Unibet, etc.)
        val bestBookmaker = findBestBookmakerForBeginners(oddsFixture.bookmakers)
        
        // Extract key odds from the best bookmaker
        val (homeWinOdds, drawOdds, awayWinOdds) = extractMatchResultOdds(bestBookmaker)
        val overUnderOdds = extractOverUnderOdds(bestBookmaker)
        val bothTeamsToScoreOdds = extractBothTeamsToScoreOdds(bestBookmaker)
        
        // Calculate ratings for value and safety
        val valueRating = calculateValueRating(homeWinOdds, drawOdds, awayWinOdds)
        val safetyRating = calculateSafetyRating(homeWinOdds, drawOdds, awayWinOdds)
        val highestOdds = listOfNotNull(homeWinOdds, drawOdds, awayWinOdds).maxOrNull()
        
        return SimplifiedOddsDto(
            fixtureId = fixtureId,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            leagueName = oddsFixture.league.name,
            matchTime = oddsFixture.fixture.date,
            bookmakerName = bestBookmaker?.name ?: "Unknown",
            homeWinOdds = homeWinOdds,
            drawOdds = drawOdds,
            awayWinOdds = awayWinOdds,
            overUnderOdds = overUnderOdds,
            bothTeamsToScoreOdds = bothTeamsToScoreOdds,
            valueRating = valueRating,
            safetyRating = safetyRating,
            highestOdds = highestOdds
        )
    }

    /**
     * Find the best bookmaker for beginners based on popularity and reliability.
     */
    private fun findBestBookmakerForBeginners(bookmakers: List<BookmakerDto>): BookmakerDto? {
        // Priority list of bookmakers known for beginner-friendly odds
        val preferredBookmakers = listOf(
            "Bet365", "Unibet", "William Hill", "Betfair", "Pinnacle",
            "888sport", "Betway", "10Bet", "Marathon Bet", "Betsson"
        )
        
        // Try to find preferred bookmakers first
        for (preferredName in preferredBookmakers) {
            bookmakers.find { it.name.contains(preferredName, ignoreCase = true) }?.let {
                return it
            }
        }
        
        // Fallback to the first bookmaker with match result bets
        return bookmakers.firstOrNull { bookmaker ->
            bookmaker.bets.any { bet -> 
                bet.name.contains("Match Result", ignoreCase = true) ||
                bet.name.contains("1X2", ignoreCase = true)
            }
        } ?: bookmakers.firstOrNull()
    }

    /**
     * Extract match result odds (1X2) from a bookmaker.
     */
    private fun extractMatchResultOdds(bookmaker: BookmakerDto?): Triple<Double?, Double?, Double?> {
        if (bookmaker == null) return Triple(null, null, null)
        
        var homeWin: Double? = null
        var draw: Double? = null
        var awayWin: Double? = null
        
        // Look for Match Result or 1X2 bets
        val matchResultBet = bookmaker.bets.find { bet ->
            bet.name.contains("Match Result", ignoreCase = true) ||
            bet.name.contains("1X2", ignoreCase = true)
        }
        
        matchResultBet?.values?.forEach { value ->
            when {
                value.value.contains("Home", ignoreCase = true) || 
                value.value.contains("1", ignoreCase = true) -> {
                    homeWin = value.odd.toDoubleOrNull()
                }
                value.value.contains("Draw", ignoreCase = true) || 
                value.value.contains("X", ignoreCase = true) -> {
                    draw = value.odd.toDoubleOrNull()
                }
                value.value.contains("Away", ignoreCase = true) || 
                value.value.contains("2", ignoreCase = true) -> {
                    awayWin = value.odd.toDoubleOrNull()
                }
            }
        }
        
        return Triple(homeWin, draw, awayWin)
    }

    /**
     * Extract Over/Under odds from a bookmaker.
     */
    private fun extractOverUnderOdds(bookmaker: BookmakerDto?): Map<String, Double>? {
        if (bookmaker == null) return null
        
        val overUnderOdds = mutableMapOf<String, Double>()
        
        // Look for Over/Under bets
        val overUnderBet = bookmaker.bets.find { bet ->
            bet.name.contains("Over/Under", ignoreCase = true)
        }
        
        overUnderBet?.values?.forEach { value ->
            val key = when {
                value.value.contains("Over", ignoreCase = true) -> "Over ${value.handicap ?: "2.5"}"
                value.value.contains("Under", ignoreCase = true) -> "Under ${value.handicap ?: "2.5"}"
                else -> null
            }
            key?.let {
                value.odd.toDoubleOrNull()?.let { odd ->
                    overUnderOdds[it] = odd
                }
            }
        }
        
        return if (overUnderOdds.isNotEmpty()) overUnderOdds else null
    }

    /**
     * Extract Both Teams to Score odds from a bookmaker.
     */
    private fun extractBothTeamsToScoreOdds(bookmaker: BookmakerDto?): Map<String, Double>? {
        if (bookmaker == null) return null
        
        val bttsOdds = mutableMapOf<String, Double>()
        
        // Look for Both Teams to Score bets
        val bttsBet = bookmaker.bets.find { bet ->
            bet.name.contains("Both Teams to Score", ignoreCase = true) ||
            bet.name.contains("BTTS", ignoreCase = true)
        }
        
        bttsBet?.values?.forEach { value ->
            val key = when {
                value.value.contains("Yes", ignoreCase = true) -> "Yes"
                value.value.contains("No", ignoreCase = true) -> "No"
                else -> null
            }
            key?.let {
                value.odd.toDoubleOrNull()?.let { odd ->
                    bttsOdds[it] = odd
                }
            }
        }
        
        return if (bttsOdds.isNotEmpty()) bttsOdds else null
    }

    /**
     * Calculate value rating (0-100) based on odds distribution.
     * Higher rating indicates better value bets.
     */
    private fun calculateValueRating(
        homeWinOdds: Double?,
        drawOdds: Double?,
        awayWinOdds: Double?
    ): Double {
        val odds = listOfNotNull(homeWinOdds, drawOdds, awayWinOdds)
        if (odds.isEmpty()) return 0.0
        
        // Calculate implied probabilities
        val impliedProbabilities = odds.map { 1.0 / it }
        val totalProbability = impliedProbabilities.sum()
        
        // Calculate overround (bookmaker margin)
        val overround = totalProbability - 1.0
        
        // Value rating is higher when overround is lower (better for bettor)
        val valueRating = 100.0 * (1.0 - overround.coerceAtLeast(0.0))
        
        return valueRating.coerceIn(0.0, 100.0)
    }

    /**
     * Calculate safety rating (0-100) based on odds distribution.
     * Higher rating indicates safer bets (lower odds variance).
     */
    private fun calculateSafetyRating(
        homeWinOdds: Double?,
        drawOdds: Double?,
        awayWinOdds: Double?
    ): Double {
        val odds = listOfNotNull(homeWinOdds, drawOdds, awayWinOdds)
        if (odds.size < 2) return 50.0
        
        // Calculate standard deviation of odds
        val mean = odds.average()
        val variance = odds.map { (it - mean) * (it - mean) }.average()
        val stdDev = Math.sqrt(variance)
        
        // Lower standard deviation = safer bets (more predictable)
        val maxStdDev = 5.0 // Arbitrary maximum for normalization
        val safetyRating = 100.0 * (1.0 - (stdDev / maxStdDev).coerceIn(0.0, 1.0))
        
        return safetyRating.coerceIn(0.0, 100.0)
    }


    /**
     * Filter and sort simplified odds for beginners.
     * Prioritizes safe bets with good value.
     */
    fun filterForBeginners(
        oddsList: List<SimplifiedOddsDto>,
        minSafetyRating: Double = 60.0,
        minValueRating: Double = 40.0
    ): List<SimplifiedOddsDto> {
        return oddsList
            .filter { it.safetyRating >= minSafetyRating }
            .filter { it.valueRating >= minValueRating }
            .sortedByDescending { it.safetyRating * 0.7 + it.valueRating * 0.3 }
    }

    /**
     * Generate beginner-friendly betting advice based on simplified odds.
     */
    fun generateBettingAdvice(odds: SimplifiedOddsDto): String {
        val advice = StringBuilder()
        
        advice.append("**${odds.homeTeam} vs ${odds.awayTeam}**\n")
        advice.append("League: ${odds.leagueName}\n")
        advice.append("Time: ${formatMatchTime(odds.matchTime)}\n")
        advice.append("Bookmaker: ${odds.bookmakerName}\n\n")
        
        // Match result odds
        if (odds.homeWinOdds != null && odds.drawOdds != null && odds.awayWinOdds != null) {
            advice.append("**Match Result Odds:**\n")
            advice.append("- ${odds.homeTeam} Win: ${String.format("%.2f", odds.homeWinOdds)}\n")
            advice.append("- Draw: ${String.format("%.2f", odds.drawOdds)}\n")
            advice.append("- ${odds.awayTeam} Win: ${String.format("%.2f", odds.awayWinOdds)}\n\n")
        }
        
        // Over/Under odds
        odds.overUnderOdds?.let { overUnder ->
            advice.append("**Over/Under 2.5 Goals:**\n")
            overUnder.forEach { (key, value) ->
                advice.append("- $key: ${String.format("%.2f", value)}\n")
            }
            advice.append("\n")
        }
        
        // Both Teams to Score odds
        odds.bothTeamsToScoreOdds?.let { btts ->
            advice.append("**Both Teams to Score:**\n")
            btts.forEach { (key, value) ->
                advice.append("- $key: ${String.format("%.2f", value)}\n")
            }
            advice.append("\n")
        }
        
        // Ratings and recommendation
        advice.append("**Analysis:**\n")
        advice.append("- Safety Rating: ${String.format("%.0f", odds.safetyRating)}/100\n")
        advice.append("- Value Rating: ${String.format("%.0f", odds.valueRating)}/100\n")
        
        val recommendation = when {
            odds.safetyRating >= 80 && odds.valueRating >= 60 -> "⭐ **Strong Bet** - Good balance of safety and value"
            odds.safetyRating >= 70 -> "✅ **Safe Bet** - Lower risk, good for beginners"
            odds.valueRating >= 70 -> "💰 **Value Bet** - Higher potential return, moderate risk"
            else -> "⚠️ **Caution Advised** - Consider other matches"
        }
        
        advice.append("- Recommendation: $recommendation\n")
        
        return advice.toString()
    }

    /**
     * Format match time for display.
     */
    private fun formatMatchTime(isoTime: String): String {
        return try {
            dateTimeFormatter.parse(isoTime)?.let { date ->
                val outputFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                outputFormat.format(date)
            } ?: isoTime
        } catch (e: Exception) {
            isoTime
        }
    }
}

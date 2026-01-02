package com.Lyno.matchmindai.data.mapper

import com.Lyno.matchmindai.data.dto.football.PredictionsResponse
import com.Lyno.matchmindai.data.dto.football.PredictionItemDto
import com.Lyno.matchmindai.data.dto.football.WinningPercentDto
import com.Lyno.matchmindai.data.dto.football.PredictionsDto
import com.Lyno.matchmindai.data.dto.football.ComparisonDto
import com.Lyno.matchmindai.data.dto.football.H2HMatchDto
import com.Lyno.matchmindai.domain.model.MatchPredictionData
import com.Lyno.matchmindai.domain.model.WinningPercent
import com.Lyno.matchmindai.domain.model.ExpectedGoals
import com.Lyno.matchmindai.domain.model.MatchPredictionDataUtils

/**
 * Mapper for converting API-SPORTS predictions response to domain models.
 * Handles the mapping of prediction data including win probabilities and analysis.
 */
object PredictionsMapper {

    /**
     * Map a PredictionsResponse to a MatchPredictionData domain model.
     * 
     * @param response The API response containing prediction data
     * @param fixtureId The fixture ID for the prediction
     * @return MatchPredictionData domain model or null if response is empty
     */
    fun toMatchPredictionData(response: PredictionsResponse, fixtureId: Int): MatchPredictionData? {
        if (response.response.isEmpty()) {
            return null
        }

        val predictionItem = response.response.first()
        return toMatchPredictionData(predictionItem, fixtureId)
    }

    /**
     * Map a PredictionItemDto to a MatchPredictionData domain model.
     * Handles nullable fields for cases where API returns incomplete data.
     * 
     * @param predictionItem The prediction item DTO (may have null fields)
     * @param fixtureId The fixture ID for the prediction
     * @return MatchPredictionData domain model or null if insufficient data
     */
    fun toMatchPredictionData(predictionItem: PredictionItemDto, fixtureId: Int): MatchPredictionData? {
        // Check if we have minimal required data
        if (predictionItem.predictions == null || predictionItem.teams == null || predictionItem.league == null) {
            return null
        }

        val predictions = predictionItem.predictions
        val teams = predictionItem.teams
        val league = predictionItem.league

        // Extract team names with null safety
        val homeTeam = teams.home.name ?: "Home Team"
        val awayTeam = teams.away.name ?: "Away Team"
        val leagueName = league.name ?: "Unknown League"

        // Check if this is a "No predictions available" case
        val advice = predictions.advice ?: ""
        val isNoPredictionsAvailable = advice.contains("No predictions available", ignoreCase = true) ||
                                      advice.contains("Geen voorspellingen beschikbaar", ignoreCase = true)

        // Parse winning percentages - use computed property that handles both 'percent' and 'winning_percent'
        val winningPercentDto = predictions.winningPercentData
        if (winningPercentDto == null || isNoPredictionsAvailable) {
            // Return null for "No predictions available" cases - repository will use empty()
            return null
        }

        val winningPercent = parseWinningPercent(winningPercentDto)

        // Generate expected goals based on winning percentages
        val expectedGoals = calculateExpectedGoals(winningPercent)

        // Use advice as primary prediction
        val primaryPrediction = if (advice.isNotBlank()) advice else "Geen advies beschikbaar"

        // Generate analysis from prediction data
        val analysis = generateAnalysis(predictions, winningPercent, predictionItem.comparison, predictionItem.h2h)

        return MatchPredictionData(
            fixtureId = fixtureId,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            league = leagueName,
            primaryPrediction = primaryPrediction,
            winningPercent = winningPercent,
            expectedGoals = expectedGoals,
            analysis = analysis,
            lastUpdated = "" // No fixture date in new structure
        )
    }

    /**
     * Parse winning percentages from DTO to domain model.
     * Handles string percentages like "45.5%" and converts to Double.
     */
    private fun parseWinningPercent(winningPercentDto: WinningPercentDto): WinningPercent {
        val homePercent = parsePercentage(winningPercentDto.home)
        val drawPercent = parsePercentage(winningPercentDto.draw)
        val awayPercent = parsePercentage(winningPercentDto.away)

        // Normalize to ensure total is 100%
        val total = homePercent + drawPercent + awayPercent
        val normalizedHome = if (total > 0) (homePercent / total) * 100 else 33.3
        val normalizedDraw = if (total > 0) (drawPercent / total) * 100 else 33.3
        val normalizedAway = if (total > 0) (awayPercent / total) * 100 else 33.3

        return WinningPercent(
            home = normalizedHome,
            draw = normalizedDraw,
            away = normalizedAway
        )
    }

    /**
     * Parse percentage string to Double.
     * Handles formats like "45.5%", "45.5", or "45".
     */
    private fun parsePercentage(percentageStr: String): Double {
        return try {
            // Remove percentage sign and trim
            val cleanStr = percentageStr.replace("%", "").trim()
            cleanStr.toDouble()
        } catch (e: Exception) {
            0.0
        }
    }

    /**
     * Calculate expected goals based on winning percentages.
     * This is a simplified model - in a real implementation, you would use
     * more sophisticated algorithms or get expected goals from the API.
     */
    private fun calculateExpectedGoals(winningPercent: WinningPercent): ExpectedGoals {
        // Base expected goals calculation based on win probabilities
        // Higher win probability for a team suggests they're more likely to score
        val homeBase = 1.0 + (winningPercent.home / 100.0) * 2.0
        val awayBase = 1.0 + (winningPercent.away / 100.0) * 2.0

        // Add some randomness and adjust for draw probability
        val drawFactor = winningPercent.draw / 100.0
        val homeGoals = homeBase * (1.0 - drawFactor * 0.3)
        val awayGoals = awayBase * (1.0 - drawFactor * 0.3)

        return ExpectedGoals(
            home = homeGoals.coerceIn(0.0, 5.0),
            away = awayGoals.coerceIn(0.0, 5.0)
        )
    }

    /**
     * Generate analysis text from prediction data.
     */
    private fun generateAnalysis(
        predictions: PredictionsDto,
        winningPercent: WinningPercent,
        comparison: ComparisonDto?,
        h2hMatches: List<H2HMatchDto>?
    ): String {
        val analysisBuilder = StringBuilder()

        // Add winning percentages analysis
        analysisBuilder.append("Win kansen: ")
        analysisBuilder.append("${winningPercent.home.toInt()}% thuis, ")
        analysisBuilder.append("${winningPercent.draw.toInt()}% gelijkspel, ")
        analysisBuilder.append("${winningPercent.away.toInt()}% uit.\n\n")

        // Add winner prediction if available
        predictions.winner?.let { winner ->
            analysisBuilder.append("Voorspelde winnaar: ${winner.name ?: "Onbekend"}")
            winner.comment?.let { comment ->
                analysisBuilder.append(" ($comment)")
            }
            analysisBuilder.append("\n\n")
        }

        // Add under/over prediction if available
        predictions.underOver?.let { underOver ->
            analysisBuilder.append("Under/Over voorspelling: $underOver\n")
        }

        // Add goals prediction if available
        predictions.goals?.let { goals ->
            goals.home?.let { homeGoals ->
                goals.away?.let { awayGoals ->
                    analysisBuilder.append("Goals voorspelling: Thuis $homeGoals, Uit $awayGoals\n")
                }
            }
        }

        // Add advice with null safety
        val advice = predictions.advice ?: "Geen advies beschikbaar"
        analysisBuilder.append("\nAdvies: $advice\n\n")

        // Add comparison analysis if available
        comparison?.let { comp ->
            analysisBuilder.append("Vergelijking:\n")
            comp.form?.let { form ->
                analysisBuilder.append("- Vorm: Thuis ${form.home ?: "?"}%, Uit ${form.away ?: "?"}%\n")
            }
            comp.att?.let { att ->
                analysisBuilder.append("- Aanval: Thuis ${att.home ?: "?"}%, Uit ${att.away ?: "?"}%\n")
            }
            comp.def?.let { def ->
                analysisBuilder.append("- Verdediging: Thuis ${def.home ?: "?"}%, Uit ${def.away ?: "?"}%\n")
            }
            comp.poissonDistribution?.let { poisson ->
                analysisBuilder.append("- Poisson distributie: Thuis ${poisson.home ?: "?"}%, Uit ${poisson.away ?: "?"}%\n")
            }
            comp.total?.let { total ->
                analysisBuilder.append("- Totaal: Thuis ${total.home ?: "?"}%, Uit ${total.away ?: "?"}%\n")
            }
            analysisBuilder.append("\n")
        }

        // Add head-to-head analysis if available
        h2hMatches?.let { matches ->
            if (matches.isNotEmpty()) {
                analysisBuilder.append("Head-to-head historie (laatste ${matches.size} wedstrijden):\n")
                matches.take(3).forEachIndexed { index, match ->
                    val homeTeam = match.teams?.home?.name ?: "Thuis"
                    val awayTeam = match.teams?.away?.name ?: "Uit"
                    val homeGoals = match.goals?.home ?: 0
                    val awayGoals = match.goals?.away ?: 0
                    val date = match.fixture?.date?.substring(0, 10) ?: "Onbekende datum"
                    
                    analysisBuilder.append("${index + 1}. $date: $homeTeam $homeGoals-$awayGoals $awayTeam\n")
                }
                if (matches.size > 3) {
                    analysisBuilder.append("... en nog ${matches.size - 3} eerdere ontmoetingen\n")
                }
                analysisBuilder.append("\n")
            }
        }

        // Add expected goals analysis
        val expectedGoals = calculateExpectedGoals(winningPercent)
        analysisBuilder.append("Verwachte goals: ${expectedGoals.home.toInt()}-${expectedGoals.away.toInt()}")
        if (expectedGoals.isHighScoring) {
            analysisBuilder.append(" (hoogscorende wedstrijd verwacht)")
        }

        return analysisBuilder.toString()
    }

    /**
     * Create an empty prediction for matches without prediction data.
     */
    fun empty(fixtureId: Int, homeTeam: String, awayTeam: String, league: String): MatchPredictionData {
        return MatchPredictionDataUtils.empty(fixtureId, homeTeam, awayTeam, league)
    }
}

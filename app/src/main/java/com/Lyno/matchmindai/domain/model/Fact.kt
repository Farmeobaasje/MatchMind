package com.Lyno.matchmindai.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents a dynamic fact to display during app loading.
 * Each fact has a category, text, and optional icon/emoji.
 */
data class Fact(
    val id: Int,
    val category: FactCategory,
    val text: String,
    val emoji: String = "",
    val isDynamic: Boolean = false,
    val dynamicValue: String? = null
) {
    /**
     * Returns the full text with emoji and dynamic value if applicable.
     */
    fun getDisplayText(): String {
        val emojiPrefix = if (emoji.isNotEmpty()) "$emoji " else ""
        val baseText = emojiPrefix + text
        
        return if (isDynamic && dynamicValue != null) {
            baseText.replace("{value}", dynamicValue)
        } else {
            baseText
        }
    }
}

/**
 * Categories for different types of facts.
 */
enum class FactCategory {
    TICKET_SALES,      // 🎫 Kaartjes gaan in de verkoop...
    TEAM_LINEUP,       // ⚽ Spelers worden opgesteld...
    WEEKEND_STATS,     // ⚡ Goals gescoord dit weekend
    AI_ANALYSIS,       // 📊 AI analyseert wedstrijden
    DERBY_HIGHLIGHT,   // 🏆 Derby van de week
    LEAGUE_FACT,       // 🏅 League interessante feiten
    PLAYER_SPOTLIGHT,  // 👤 Speler in de spotlight
    BETTING_TIP        // 💰 AI betting tip
}

/**
 * Static collection of football facts for the loading screen.
 */
object FactCollection {
    val allFacts = listOf(
        // 🎫 TICKET SALES
        Fact(1, FactCategory.TICKET_SALES, "🎫 Kaartjes gaan in de verkoop voor de klassieker..."),
        Fact(2, FactCategory.TICKET_SALES, "🎫 Stadion uitverkocht voor de topwedstrijd!"),
        Fact(3, FactCategory.TICKET_SALES, "🎫 Laatste tickets beschikbaar voor de finale!"),
        Fact(4, FactCategory.TICKET_SALES, "🎫 Premium seats gereserveerd voor VIP's..."),
        Fact(5, FactCategory.TICKET_SALES, "🎫 Online ticketverkoop breekt records!"),

        // ⚽ TEAM LINEUP
        Fact(6, FactCategory.TEAM_LINEUP, "⚽ Spelers worden opgesteld in de kleedkamer..."),
        Fact(7, FactCategory.TEAM_LINEUP, "⚽ Coach beslist over de tactische formatie"),
        Fact(8, FactCategory.TEAM_LINEUP, "⚽ Wisselspelers warmen op langs de lijn"),
        Fact(9, FactCategory.TEAM_LINEUP, "⚽ Aanvoerder geeft laatste peptalk"),
        Fact(10, FactCategory.TEAM_LINEUP, "⚽ Keepers trainen op penalty's"),

        // ⚡ WEEKEND STATS (dynamic values will be injected)
        Fact(11, FactCategory.WEEKEND_STATS, "⚡ Er zijn {value} goals gescoord dit weekend!", isDynamic = true),
        Fact(12, FactCategory.WEEKEND_STATS, "⚡ {value} gele kaarten getrokken afgelopen ronde", isDynamic = true),
        Fact(13, FactCategory.WEEKEND_STATS, "⚡ {value} assists geregistreerd in de competitie", isDynamic = true),
        Fact(14, FactCategory.WEEKEND_STATS, "⚡ {value} penalty's toegekend afgelopen speelronde", isDynamic = true),
        Fact(15, FactCategory.WEEKEND_STATS, "⚡ {value} corners genomen in de Eredivisie", isDynamic = true),

        // 📊 AI ANALYSIS
        Fact(16, FactCategory.AI_ANALYSIS, "📊 AI analyseert {value} wedstrijden voor voorspellingen...", isDynamic = true),
        Fact(17, FactCategory.AI_ANALYSIS, "📊 Neural network traint op historische data"),
        Fact(18, FactCategory.AI_ANALYSIS, "📊 Machine learning model berekent kansen"),
        Fact(19, FactCategory.AI_ANALYSIS, "📊 Deep learning algoritme optimaliseert voorspellingen"),
        Fact(20, FactCategory.AI_ANALYSIS, "📊 AI engine scant {value} statistieken...", isDynamic = true),

        // 🏆 DERBY HIGHLIGHT
        Fact(21, FactCategory.DERBY_HIGHLIGHT, "🏆 Derby van de week: Ajax vs Feyenoord"),
        Fact(22, FactCategory.DERBY_HIGHLIGHT, "🏆 Lokale rivaliteit: PSV vs FC Utrecht"),
        Fact(23, FactCategory.DERBY_HIGHLIGHT, "🏆 Klassieker: Real Madrid vs Barcelona"),
        Fact(24, FactCategory.DERBY_HIGHLIGHT, "🏆 Derby della Madonnina: Inter vs AC Milan"),
        Fact(25, FactCategory.DERBY_HIGHLIGHT, "🏆 North London Derby: Arsenal vs Tottenham"),

        // 🏅 LEAGUE FACTS
        Fact(26, FactCategory.LEAGUE_FACT, "🏅 Eredivisie: Meeste goals per wedstrijd in Europa"),
        Fact(27, FactCategory.LEAGUE_FACT, "🏅 Premier League: Hoogste TV-rechten ter wereld"),
        Fact(28, FactCategory.LEAGUE_FACT, "🏅 Bundesliga: Beste stadionbezoek percentage"),
        Fact(29, FactCategory.LEAGUE_FACT, "🏅 Serie A: Meeste tactische innovaties"),
        Fact(30, FactCategory.LEAGUE_FACT, "🏅 Ligue 1: Jongste gemiddelde leeftijd spelers"),

        // 👤 PLAYER SPOTLIGHT
        Fact(31, FactCategory.PLAYER_SPOTLIGHT, "👤 Speler in de spotlight: Brian Brobbey (Ajax)"),
        Fact(32, FactCategory.PLAYER_SPOTLIGHT, "👤 Jonge talent: Xavi Simons (PSV)"),
        Fact(33, FactCategory.PLAYER_SPOTLIGHT, "👤 Topscorer: Luuk de Jong (PSV)"),
        Fact(34, FactCategory.PLAYER_SPOTLIGHT, "👤 Meeste assists: Steven Berghuis (Ajax)"),
        Fact(35, FactCategory.PLAYER_SPOTLIGHT, "👤 Clean sheet koning: Bart Verbruggen (Brighton)"),

        // 💰 BETTING TIPS
        Fact(36, FactCategory.BETTING_TIP, "💰 AI tip: Over 2.5 goals in 78% van de wedstrijden"),
        Fact(37, FactCategory.BETTING_TIP, "💰 Value bet gevonden: Both teams to score"),
        Fact(38, FactCategory.BETTING_TIP, "💰 Kelly Criterion adviseert: Fractionele inzet"),
        Fact(39, FactCategory.BETTING_TIP, "💰 Expected Value positief voor thuisteam"),
        Fact(40, FactCategory.BETTING_TIP, "💰 AI ziet value in de Asian handicap markt")
    )

    /**
     * Get a random fact from the collection.
     */
    fun getRandomFact(): Fact {
        return allFacts.random()
    }

    /**
     * Get facts by category.
     */
    fun getFactsByCategory(category: FactCategory): List<Fact> {
        return allFacts.filter { it.category == category }
    }

    /**
     * Get a random fact with dynamic values injected.
     */
    fun getRandomDynamicFact(): Fact {
        val dynamicFacts = allFacts.filter { it.isDynamic }
        return if (dynamicFacts.isNotEmpty()) {
            dynamicFacts.random().copy(
                dynamicValue = generateDynamicValue()
            )
        } else {
            getRandomFact()
        }
    }

    /**
     * Generate realistic dynamic values for facts.
     */
    private fun generateDynamicValue(): String {
        return when ((1..5).random()) {
            1 -> (120..180).random().toString() // goals
            2 -> (40..80).random().toString()   // yellow cards
            3 -> (60..100).random().toString()  // assists
            4 -> (8..15).random().toString()    // penalties
            5 -> (300..400).random().toString() // matches analyzed
            else -> (150..250).random().toString()
        }
    }
}

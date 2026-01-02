package com.Lyno.matchmindai.data.templates

import com.Lyno.matchmindai.domain.model.ScenarioCategory
import com.Lyno.matchmindai.domain.model.ScenarioEventType
import com.Lyno.matchmindai.domain.model.ScenarioTimelineEvent

/**
 * Natural language templates for scenario generation.
 * These templates convert raw data into human-readable Dutch scenario narratives.
 */
object ScenarioTemplates {
    
    // ==================== SCENARIO TITLE TEMPLATES ====================
    
    fun getScenarioTitle(category: ScenarioCategory, homeTeam: String, awayTeam: String): String {
        return when (category) {
            ScenarioCategory.DOMINANT_HOME_WIN -> "🏠 $homeTeam Domineert Thuis"
            ScenarioCategory.NARROW_HOME_WIN -> "⚔️ Krappe Zege voor $homeTeam"
            ScenarioCategory.HIGH_SCORING_DRAW -> "🤝 Goalrijke Gelijkmaker"
            ScenarioCategory.LOW_SCORING_DRAW -> "🛡️ Tactische Patstelling"
            ScenarioCategory.AWAY_TEAM_SURPRISE -> "🎯 $awayTeam Verrast Uit"
            ScenarioCategory.GOAL_FEST -> "🎆 Goal Festival"
            ScenarioCategory.DEFENSIVE_BATTLE -> "🛡️ Verdedigingsslag"
            ScenarioCategory.CONTROVERSIAL_OUTCOME -> "⚖️ Controversiële Uitslag"
            ScenarioCategory.LATE_DRAMA -> "⌛ Laat Drama"
            ScenarioCategory.ONE_SIDED_AFFAIR -> "💥 Eenzijdige Affaire"
        }
    }
    
    // ==================== SCENARIO DESCRIPTION TEMPLATES ====================
    
    fun getScenarioDescription(
        category: ScenarioCategory,
        homeTeam: String,
        awayTeam: String,
        predictedScore: String
    ): String {
        return when (category) {
            ScenarioCategory.DOMINANT_HOME_WIN ->
                "$homeTeam controleert de wedstrijd van begin tot eind en wint overtuigend met $predictedScore. " +
                "Het thuisvoordeel is beslissend en de kwaliteitsverschillen zijn duidelijk zichtbaar."
                
            ScenarioCategory.NARROW_HOME_WIN ->
                "Een nek-aan-nek race die $homeTeam nipt wint met $predictedScore. " +
                "Een individuele actie of set-piece maakt het verschil in deze evenwichtige confrontatie."
                
            ScenarioCategory.HIGH_SCORING_DRAW ->
                "Een spektakelstuk met veel doelpunten eindigt in een $predictedScore gelijkspel. " +
                "Beide teams tonen offensieve kwaliteiten maar verdedigen matig."
                
            ScenarioCategory.LOW_SCORING_DRAW ->
                "Een tactische patstelling eindigt in een $predictedScore gelijkspel. " +
                "Beide coaches zijn te voorzichtig en nemen weinig risico."
                
            ScenarioCategory.AWAY_TEAM_SURPRISE ->
                "$awayTeam verrast door uit te winnen met $predictedScore. " +
                "Het thuisvoordeel werkt niet en de uitploeg toont meer karakter."
                
            ScenarioCategory.GOAL_FEST ->
                "Een goal festival met $predictedScore als eindstand. " +
                "Beide verdedigingen staan wijd open en aanvallers hebben een feestje."
                
            ScenarioCategory.DEFENSIVE_BATTLE ->
                "Een verdedigingsslag eindigt in een $predictedScore. " +
                "Kansen zijn schaars en beide keepers staan sterk."
                
            ScenarioCategory.CONTROVERSIAL_OUTCOME ->
                "Een controversiële uitslag van $predictedScore na discutabele beslissingen. " +
                "De scheidsrechter speelt een hoofdrol in dit scenario."
                
            ScenarioCategory.LATE_DRAMA ->
                "Laat drama leidt tot $predictedScore. " +
                "De wedstrijd kantelt in de slotfase na een onverwachte wending."
                
            ScenarioCategory.ONE_SIDED_AFFAIR ->
                "Een eenzijdige affaire eindigt in $predictedScore. " +
                "Het verschil in klasse is te groot en één team is duidelijk superieur."
        }
    }
    
    // ==================== TIMELINE EVENT TEMPLATES ====================
    
    fun getTimelineEvents(
        category: ScenarioCategory,
        homeTeam: String,
        awayTeam: String
    ): List<ScenarioTimelineEvent> {
        return when (category) {
            ScenarioCategory.DOMINANT_HOME_WIN -> listOf(
                ScenarioTimelineEvent(15, ScenarioEventType.GOAL, 
                    "$homeTeam scoort vroeg en neemt controle", 70),
                ScenarioTimelineEvent(40, ScenarioEventType.GOAL, 
                    "2-0 voor rust bevestigt dominantie", 85),
                ScenarioTimelineEvent(75, ScenarioEventType.TACTICAL_CHANGE, 
                    "$awayTeam wisselt offensief maar zonder effect", 40)
            )
            
            ScenarioCategory.NARROW_HOME_WIN -> listOf(
                ScenarioTimelineEvent(35, ScenarioEventType.GOAL, 
                    "$homeTeam opent de score na drukke fase", 60),
                ScenarioTimelineEvent(65, ScenarioEventType.MOMENTUM_SHIFT, 
                    "$awayTeam komt beter in de wedstrijd", 50),
                ScenarioTimelineEvent(85, ScenarioEventType.GOAL, 
                    "$homeTeam scoort beslissend in slotfase", 90)
            )
            
            ScenarioCategory.HIGH_SCORING_DRAW -> listOf(
                ScenarioTimelineEvent(10, ScenarioEventType.GOAL, 
                    "Vroeg doelpunt zet toon voor goalrijke wedstrijd", 60),
                ScenarioTimelineEvent(30, ScenarioEventType.GOAL, 
                    "Gelijkmaker na mooie combinatie", 50),
                ScenarioTimelineEvent(55, ScenarioEventType.GOAL, 
                    "$homeTeam neemt weer voorsprong", 65),
                ScenarioTimelineEvent(75, ScenarioEventType.GOAL, 
                    "$awayTeam maakt gelijk in spannende slotfase", 70)
            )
            
            ScenarioCategory.LOW_SCORING_DRAW -> listOf(
                ScenarioTimelineEvent(25, ScenarioEventType.TACTICAL_CHANGE, 
                    "Beide teams zetten defensief", 40),
                ScenarioTimelineEvent(60, ScenarioEventType.MOMENTUM_SHIFT, 
                    "Wedstrijd komt niet echt op gang", 30),
                ScenarioTimelineEvent(85, ScenarioEventType.CONTROVERSY, 
                    "Geschonden penalty claim niet gegeven", 55)
            )
            
            ScenarioCategory.AWAY_TEAM_SURPRISE -> listOf(
                ScenarioTimelineEvent(20, ScenarioEventType.GOAL, 
                    "$awayTeam verrast met vroeg uitdoelpunt", 75),
                ScenarioTimelineEvent(45, ScenarioEventType.CROWD_INFLUENCE, 
                    "Thuis publiek wordt ongeduldig", 50),
                ScenarioTimelineEvent(70, ScenarioEventType.GOAL, 
                    "$awayTeam besluit de wedstrijd", 85)
            )
            
            ScenarioCategory.GOAL_FEST -> listOf(
                ScenarioTimelineEvent(5, ScenarioEventType.GOAL, 
                    "Supersnelle openingstreffer", 65),
                ScenarioTimelineEvent(25, ScenarioEventType.GOAL, 
                    "Directe gelijkmaker", 55),
                ScenarioTimelineEvent(40, ScenarioEventType.GOAL, 
                    "Derde goal voor rust", 60),
                ScenarioTimelineEvent(65, ScenarioEventType.GOAL, 
                    "Vierde goal in tweede helft", 50),
                ScenarioTimelineEvent(80, ScenarioEventType.GOAL, 
                    "Laatste goal voor de cijfers", 45)
            )
            
            ScenarioCategory.DEFENSIVE_BATTLE -> listOf(
                ScenarioTimelineEvent(35, ScenarioEventType.INJURY, 
                    "Blessure sleutelspeler verstoort ritme", 40),
                ScenarioTimelineEvent(60, ScenarioEventType.TACTICAL_CHANGE, 
                    "Beide coaches versterken defensie", 35),
                ScenarioTimelineEvent(85, ScenarioEventType.RED_CARD, 
                    "Rode kaart in slotfase", 60)
            )
            
            ScenarioCategory.CONTROVERSIAL_OUTCOME -> listOf(
                ScenarioTimelineEvent(30, ScenarioEventType.CONTROVERSY, 
                    "Discutabele penalty voor $homeTeam", 70),
                ScenarioTimelineEvent(55, ScenarioEventType.RED_CARD, 
                    "Rode kaart na tweede gele voor $awayTeam", 65),
                ScenarioTimelineEvent(75, ScenarioEventType.CONTROVERSY, 
                    "Doelpunt afgekeurd wegens buitenspel", 75)
            )
            
            ScenarioCategory.LATE_DRAMA -> listOf(
                ScenarioTimelineEvent(70, ScenarioEventType.GOAL, 
                    "Laat doelpunt lijkt beslissend", 80),
                ScenarioTimelineEvent(85, ScenarioEventType.GOAL, 
                    "Onverwachte gelijkmaker", 90),
                ScenarioTimelineEvent(90, ScenarioEventType.MOMENTUM_SHIFT, 
                    "Extra tijd vol spanning", 60)
            )
            
            ScenarioCategory.ONE_SIDED_AFFAIR -> listOf(
                ScenarioTimelineEvent(10, ScenarioEventType.GOAL, 
                    "Vroege voorsprong zet toon", 70),
                ScenarioTimelineEvent(35, ScenarioEventType.GOAL, 
                    "Tweede goal bevestigt superioriteit", 80),
                ScenarioTimelineEvent(60, ScenarioEventType.GOAL, 
                    "Derde goal maakt wedstrijd beslist", 85),
                ScenarioTimelineEvent(75, ScenarioEventType.TACTICAL_CHANGE, 
                    "Winnend team wisselt uit voor rust", 30)
            )
        }
    }
    
    // ==================== KEY FACTOR TEMPLATES ====================
    
    fun getKeyFactors(category: ScenarioCategory): List<String> {
        return when (category) {
            ScenarioCategory.DOMINANT_HOME_WIN -> listOf(
                "Sterk thuisvoordeel",
                "Kwaliteitsverschil in selectie",
                "Historische superioriteit thuis"
            )
            ScenarioCategory.NARROW_HOME_WIN -> listOf(
                "Individuele klasse maakt verschil",
                "Set-piece effectiviteit",
                "Thuispubliek als extra man"
            )
            ScenarioCategory.HIGH_SCORING_DRAW -> listOf(
                "Offensieve mentaliteit beide teams",
                "Zwakke verdedigingen",
                "Open spel met ruimtes"
            )
            ScenarioCategory.LOW_SCORING_DRAW -> listOf(
                "Tactische voorzichtigheid",
                "Angst voor verlies",
                "Gebrek aan creativiteit"
            )
            ScenarioCategory.AWAY_TEAM_SURPRISE -> listOf(
                "Counter-attack effectiviteit",
                "Thuisploeg onder druk",
                "Uitploeg meer karakter"
            )
            ScenarioCategory.GOAL_FEST -> listOf(
                "Offensieve kwaliteiten",
                "Defensieve zwaktes",
                "Hoog tempo en risico"
            )
            ScenarioCategory.DEFENSIVE_BATTLE -> listOf(
                "Defensieve organisatie",
                "Beperkte kansen",
                "Keepers in vorm"
            )
            ScenarioCategory.CONTROVERSIAL_OUTCOME -> listOf(
                "Scheidsrechterlijke beslissingen",
                "VAR interventies",
                "Emotionele reacties"
            )
            ScenarioCategory.LATE_DRAMA -> listOf(
                "Conditieverschillen",
                "Tactische aanpassingen",
                "Mentale sterkte"
            )
            ScenarioCategory.ONE_SIDED_AFFAIR -> listOf(
                "Klasseverschil",
                "Motivatie verschil",
                "Tactische superioriteit"
            )
        }
    }
    
    // ==================== TRIGGER EVENT TEMPLATES ====================
    
    fun getTriggerEvents(category: ScenarioCategory): List<String> {
        return when (category) {
            ScenarioCategory.DOMINANT_HOME_WIN -> listOf(
                "Vroeg doelpunt thuisploeg",
                "Rode kaart uitploeg",
                "Publiek creëert heksenketel"
            )
            ScenarioCategory.NARROW_HOME_WIN -> listOf(
                "Individuele actie sterspeler",
                "Set-piece goal",
                "Keepersfout"
            )
            ScenarioCategory.HIGH_SCORING_DRAW -> listOf(
                "Vroeg doelpunt",
                "Defensieve fouten",
                "Offensieve mentaliteit coaches"
            )
            ScenarioCategory.LOW_SCORING_DRAW -> listOf(
                "Tactische voorzichtigheid",
                "Blessure sleutelspeler",
                "Weersomstandigheden"
            )
            ScenarioCategory.AWAY_TEAM_SURPRISE -> listOf(
                "Counter-attack goal",
                "Thuisploeg onder druk",
                "Uitploeg vroeg voor"
            )
            ScenarioCategory.GOAL_FEST -> listOf(
                "Open begin",
                "Defensieve zwaktes",
                "Aanvallende kwaliteiten"
            )
            ScenarioCategory.DEFENSIVE_BATTLE -> listOf(
                "Blessure aanvallers",
                "Defensieve organisatie",
                "Angst voor verlies"
            )
            ScenarioCategory.CONTROVERSIAL_OUTCOME -> listOf(
                "Penalty beslissing",
                "Rode kaart",
                "VAR interventie"
            )
            ScenarioCategory.LATE_DRAMA -> listOf(
                "Vermoeidheid",
                "Tactische wissels",
                "Mentale druk"
            )
            ScenarioCategory.ONE_SIDED_AFFAIR -> listOf(
                "Klasseverschil",
                "Motivatie gap",
                "Tactisch plan"
            )
        }
    }
    
    // ==================== DATA SOURCE TEMPLATES ====================
    
    fun getDataSources(category: ScenarioCategory): List<String> {
        val baseSources = listOf(
            "Team form laatste 5 wedstrijden",
            "Head-to-head statistieken",
            "Thuis/uit performance data"
        )
        
        val categorySources = when (category) {
            ScenarioCategory.DOMINANT_HOME_WIN -> listOf("Thuis dominantie metrics", "Kwaliteitsindicatoren")
            ScenarioCategory.NARROW_HOME_WIN -> listOf("Set-piece analytics", "Individuele performance data")
            ScenarioCategory.HIGH_SCORING_DRAW -> listOf("xG (expected goals) data", "Defensieve zwakte metrics")
            ScenarioCategory.LOW_SCORING_DRAW -> listOf("Tactical analysis", "Risk aversion metrics")
            ScenarioCategory.AWAY_TEAM_SURPRISE -> listOf("Counter-attack efficiency", "Pressure performance data")
            ScenarioCategory.GOAL_FEST -> listOf("Goal expectation models", "Defensive vulnerability scores")
            ScenarioCategory.DEFENSIVE_BATTLE -> listOf("Defensive organization scores", "Chance creation metrics")
            ScenarioCategory.CONTROVERSIAL_OUTCOME -> listOf("Referee decision history", "VAR impact analysis")
            ScenarioCategory.LATE_DRAMA -> listOf("Fitness and endurance data", "Mental strength metrics")
            ScenarioCategory.ONE_SIDED_AFFAIR -> listOf("Team strength differentials", "Motivation analysis")
        }
        
        return baseSources + categorySources
    }
    
    // ==================== PROBABILITY DISTRIBUTION ====================
    
    /**
     * Get base probability for a scenario category based on match context.
     */
    fun getBaseProbability(
        category: ScenarioCategory,
        homeStrength: Double,
        awayStrength: Double,
        isHomeGame: Boolean = true
    ): Int {
        val baseProb = when (category) {
            ScenarioCategory.DOMINANT_HOME_WIN -> 
                if (isHomeGame && homeStrength > awayStrength + 0.3) 35 else 15
            ScenarioCategory.NARROW_HOME_WIN -> 
                if (isHomeGame && homeStrength > awayStrength) 25 else 20
            ScenarioCategory.HIGH_SCORING_DRAW -> 
                if (homeStrength > 0.6 && awayStrength > 0.6) 20 else 15
            ScenarioCategory.LOW_SCORING_DRAW -> 
                if (homeStrength < 0.5 && awayStrength < 0.5) 25 else 15
            ScenarioCategory.AWAY_TEAM_SURPRISE -> 
                if (!isHomeGame && awayStrength > homeStrength) 30 else 15
            ScenarioCategory.GOAL_FEST -> 
                if (homeStrength > 0.7 || awayStrength > 0.7) 20 else 10
            ScenarioCategory.DEFENSIVE_BATTLE -> 
                if (homeStrength < 0.4 && awayStrength < 0.4) 25 else 15
            ScenarioCategory.CONTROVERSIAL_OUTCOME -> 10
            ScenarioCategory.LATE_DRAMA -> 15
            ScenarioCategory.ONE_SIDED_AFFAIR -> 
                if (Math.abs(homeStrength - awayStrength) > 0.5) 30 else 15
        }
        
        return baseProb.coerceIn(5, 40)
    }
    
    /**
     * Get predicted score for a scenario category.
     */
    fun getPredictedScore(
        category: ScenarioCategory,
        homeTeam: String,
        awayTeam: String,
        homeStrength: Double,
        awayStrength: Double
    ): String {
        return when (category) {
            ScenarioCategory.DOMINANT_HOME_WIN -> {
                val homeGoals = when {
                    homeStrength > 0.8 -> 3
                    homeStrength > 0.6 -> 2
                    else -> 1
                }
                val awayGoals = when {
                    awayStrength > 0.6 -> 1
                    else -> 0
                }
                "$homeGoals-$awayGoals"
            }
            
            ScenarioCategory.NARROW_HOME_WIN -> {
                val homeGoals = when {
                    homeStrength > awayStrength + 0.2 -> 2
                    else -> 1
                }
                val awayGoals = homeGoals - 1
                "$homeGoals-$awayGoals"
            }
            
            ScenarioCategory.HIGH_SCORING_DRAW -> {
                val goals = when {
                    homeStrength > 0.7 && awayStrength > 0.7 -> 3
                    else -> 2
                }
                "$goals-$goals"
            }
            
            ScenarioCategory.LOW_SCORING_DRAW -> {
                val goals = when {
                    homeStrength < 0.4 && awayStrength < 0.4 -> 0
                    else -> 1
                }
                "$goals-$goals"
            }
            
            ScenarioCategory.AWAY_TEAM_SURPRISE -> {
                val awayGoals = when {
                    awayStrength > 0.7 -> 2
                    else -> 1
                }
                val homeGoals = awayGoals - 1
                "$homeGoals-$awayGoals"
            }
            
            ScenarioCategory.GOAL_FEST -> {
                val totalGoals = when {
                    homeStrength > 0.8 || awayStrength > 0.8 -> 5
                    else -> 4
                }
                val homeGoals = (totalGoals * 0.6).toInt()
                val awayGoals = totalGoals - homeGoals
                "$homeGoals-$awayGoals"
            }
            
            ScenarioCategory.DEFENSIVE_BATTLE -> {
                val totalGoals = when {
                    homeStrength < 0.3 && awayStrength < 0.3 -> 0
                    else -> 1
                }
                if (totalGoals == 0) "0-0" else "1-0"
            }
            
            ScenarioCategory.CONTROVERSIAL_OUTCOME -> {
                val homeGoals = if (homeStrength > awayStrength) 2 else 1
                val awayGoals = if (homeStrength > awayStrength) 1 else 2
                "$homeGoals-$awayGoals"
            }
            
            ScenarioCategory.LATE_DRAMA -> {
                val homeGoals = 2
                val awayGoals = 2
                "$homeGoals-$awayGoals"
            }
            
            ScenarioCategory.ONE_SIDED_AFFAIR -> {
                val strongerTeamGoals = when {
                    Math.abs(homeStrength - awayStrength) > 0.6 -> 4
                    Math.abs(homeStrength - awayStrength) > 0.4 -> 3
                    else -> 2
                }
                val weakerTeamGoals = 0
                if (homeStrength > awayStrength) "$strongerTeamGoals-$weakerTeamGoals" else "$weakerTeamGoals-$strongerTeamGoals"
            }
        }
    }
    
    /**
     * Get confidence score for a scenario based on data quality.
     */
    fun getConfidenceScore(
        category: ScenarioCategory,
        dataQuality: Double, // 0.0 to 1.0
        matchImportance: Double // 0.0 to 1.0
    ): Int {
        var confidence = when (category) {
            ScenarioCategory.DOMINANT_HOME_WIN -> 75
            ScenarioCategory.NARROW_HOME_WIN -> 65
            ScenarioCategory.HIGH_SCORING_DRAW -> 60
            ScenarioCategory.LOW_SCORING_DRAW -> 70
            ScenarioCategory.AWAY_TEAM_SURPRISE -> 55
            ScenarioCategory.GOAL_FEST -> 50
            ScenarioCategory.DEFENSIVE_BATTLE -> 70
            ScenarioCategory.CONTROVERSIAL_OUTCOME -> 40
            ScenarioCategory.LATE_DRAMA -> 45
            ScenarioCategory.ONE_SIDED_AFFAIR -> 80
        }
        
        // Adjust based on data quality
        confidence = (confidence * (0.5 + dataQuality * 0.5)).toInt()
        
        // Adjust based on match importance (more important matches have more data)
        confidence = (confidence * (0.7 + matchImportance * 0.3)).toInt()
        
        return confidence.coerceIn(30, 90)
    }
    
    /**
     * Get impact scores for a scenario.
     */
    fun getImpactScores(category: ScenarioCategory): Pair<Int, Int> {
        return when (category) {
            ScenarioCategory.DOMINANT_HOME_WIN -> Pair(30, 80) // Low chaos, high atmosphere
            ScenarioCategory.NARROW_HOME_WIN -> Pair(50, 70) // Medium chaos, high atmosphere
            ScenarioCategory.HIGH_SCORING_DRAW -> Pair(60, 60) // Medium chaos, medium atmosphere
            ScenarioCategory.LOW_SCORING_DRAW -> Pair(20, 30) // Low chaos, low atmosphere
            ScenarioCategory.AWAY_TEAM_SURPRISE -> Pair(70, 40) // High chaos, low atmosphere
            ScenarioCategory.GOAL_FEST -> Pair(80, 90) // High chaos, high atmosphere
            ScenarioCategory.DEFENSIVE_BATTLE -> Pair(40, 20) // Medium chaos, low atmosphere
            ScenarioCategory.CONTROVERSIAL_OUTCOME -> Pair(90, 50) // Very high chaos, medium atmosphere
            ScenarioCategory.LATE_DRAMA -> Pair(85, 75) // High chaos, high atmosphere
            ScenarioCategory.ONE_SIDED_AFFAIR -> Pair(25, 60) // Low chaos, medium atmosphere
        }
    }
    
    /**
     * Get betting value score for a scenario.
     */
    fun getBettingValue(
        category: ScenarioCategory,
        probability: Int,
        odds: Double? = null
    ): Double {
        val baseValue = when (category) {
            ScenarioCategory.DOMINANT_HOME_WIN -> 0.6
            ScenarioCategory.NARROW_HOME_WIN -> 0.7
            ScenarioCategory.HIGH_SCORING_DRAW -> 0.8
            ScenarioCategory.LOW_SCORING_DRAW -> 0.4
            ScenarioCategory.AWAY_TEAM_SURPRISE -> 0.9
            ScenarioCategory.GOAL_FEST -> 0.5
            ScenarioCategory.DEFENSIVE_BATTLE -> 0.3
            ScenarioCategory.CONTROVERSIAL_OUTCOME -> 0.2
            ScenarioCategory.LATE_DRAMA -> 0.7
            ScenarioCategory.ONE_SIDED_AFFAIR -> 0.4
        }
        
        // Adjust based on probability (lower probability = higher value if it happens)
        val probabilityAdjustment = 1.0 - (probability / 100.0)
        
        // Adjust based on odds if available
        val oddsAdjustment = odds?.let { 
            if (it > 2.0) 1.2 else if (it > 1.5) 1.0 else 0.8 
        } ?: 1.0
        
        return (baseValue * probabilityAdjustment * oddsAdjustment).coerceIn(0.0, 1.0)
    }
}

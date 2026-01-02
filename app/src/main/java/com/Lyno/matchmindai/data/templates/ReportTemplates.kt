package com.Lyno.matchmindai.data.templates

import com.Lyno.matchmindai.domain.model.ReportStyle

/**
 * Natural language templates for match report generation.
 * These templates convert raw Mastermind AI data into human-readable Dutch narratives.
 */
object ReportTemplates {
    
    // ==================== TITLE TEMPLATES ====================
    
    fun getTitleTemplate(style: ReportStyle, homeTeam: String, awayTeam: String): String {
        return when (style) {
            ReportStyle.HIGH_CHAOS -> "⚔️ TOTALE OORLOG: $homeTeam vs $awayTeam"
            ReportStyle.MEDIUM_CHAOS -> "⚠️ HOOG RISICO: $homeTeam vs $awayTeam"
            ReportStyle.FORTRESS -> "🏰 HEKSENKETEL: $homeTeam vs $awayTeam"
            ReportStyle.DEAD_STADIUM -> "🌫️ DOODS STADION: $homeTeam vs $awayTeam"
            ReportStyle.HIGH_CONFIDENCE -> "🎯 DUÏDELIJKE VERWACHTING: $homeTeam vs $awayTeam"
            ReportStyle.LOW_CONFIDENCE -> "❓ ONZEKERE STRIJD: $homeTeam vs $awayTeam"
            ReportStyle.STANDARD -> "📊 ANALYSE VERSLAG: $homeTeam vs $awayTeam"
        }
    }
    
    // ==================== INTRODUCTION TEMPLATES ====================
    
    fun getIntroductionTemplate(
        style: ReportStyle,
        homeTeam: String,
        awayTeam: String,
        league: String
    ): String {
        return when (style) {
            ReportStyle.HIGH_CHAOS -> 
                "Een intense derby of rivaliteitswedstrijd waar alles mogelijk is. " +
                "Veel kaarten, emoties en onvoorspelbaarheid verwacht in deze $league confrontatie."
                
            ReportStyle.MEDIUM_CHAOS ->
                "Een geforceerde wedstrijd met risico's aan beide kanten. " +
                "Beide teams hebben punten nodig in deze belangrijke $league wedstrijd."
                
            ReportStyle.FORTRESS ->
                "Een klassieke thuiswedstrijd waar het publiek de twaalfde man wordt. " +
                "$homeTeam speelt in hun fort en zal alles op alles zetten in de $league."
                
            ReportStyle.DEAD_STADIUM ->
                "Een wedstrijd zonder veel publieksdruk of sfeer. " +
                "Beide teams moeten hun eigen motivatie vinden in deze $league ontmoeting."
                
            ReportStyle.HIGH_CONFIDENCE ->
                "Een duidelijke verwachting op basis van sterke data. " +
                "De $league statistieken wijzen in een specifieke richting voor deze wedstrijd."
                
            ReportStyle.LOW_CONFIDENCE ->
                "Een onzekere strijd met veel variabelen. " +
                "De $league data geeft geen duidelijke richting voor deze wedstrijd."
                
            ReportStyle.STANDARD ->
                "Een standaard $league confrontatie tussen $homeTeam en $awayTeam. " +
                "De Mastermind AI analyse geeft inzicht in de belangrijkste factoren."
        }
    }
    
    // ==================== SITUATION ANALYSIS TEMPLATES ====================
    
    fun getSituationAnalysisTemplate(
        style: ReportStyle,
        chaosLevel: String,
        chaosScore: Int,
        atmosphereLevel: String,
        atmosphereScore: Int,
        homeTeam: String,
        awayTeam: String
    ): String {
        val chaosDescription = when {
            chaosScore >= 80 -> "totale oorlogssfeer"
            chaosScore >= 60 -> "hoog risico met veel onvoorspelbaarheid"
            chaosScore >= 40 -> "gemiddelde variatie"
            chaosScore >= 20 -> "gecontroleerde omstandigheden"
            else -> "voorspelbare omgeving"
        }
        
        val atmosphereDescription = when {
            atmosphereScore >= 80 -> "een heksenketel waar het thuisvoordeel cruciaal wordt"
            atmosphereScore >= 60 -> "sterke thuissteun die het verschil kan maken"
            atmosphereScore >= 40 -> "neutrale omstandigheden zonder duidelijk voordeel"
            atmosphereScore >= 20 -> "rustige sfeer met beperkt publieksinvloed"
            else -> "een dood stadion zonder noemenswaardige sfeer"
        }
        
        return when (style) {
            ReportStyle.HIGH_CHAOS ->
                "De chaos meter staat op $chaosScore/100 wat wijst op $chaosDescription. " +
                "Dit betekent veel kaarten, emotionele uitbarstingen en onvoorspelbare momenten. " +
                "De atmosfeer is $atmosphereDescription, wat de intensiteit verder opvoert."
                
            ReportStyle.MEDIUM_CHAOS ->
                "Met een chaos score van $chaosScore/100 ($chaosLevel) verwachten we $chaosDescription. " +
                "De wedstrijd speelt zich af in $atmosphereDescription, wat de dynamiek beïnvloedt."
                
            ReportStyle.FORTRESS ->
                "Het stadion is een fort ($atmosphereScore/100) wat betekent $atmosphereDescription. " +
                "Dit geeft $homeTeam een significant voordeel. " +
                "De chaos niveau is $chaosLevel ($chaosScore/100) wat $chaosDescription betekent."
                
            ReportStyle.DEAD_STADIUM ->
                "Het stadion is dood ($atmosphereScore/100) wat resulteert in $atmosphereDescription. " +
                "Teams moeten hun eigen motivatie vinden. " +
                "Chaos niveau: $chaosLevel ($chaosScore/100) - $chaosDescription."
                
            else ->
                "Chaos niveau: $chaosLevel ($chaosScore/100) - $chaosDescription. " +
                "Atmosfeer: $atmosphereLevel ($atmosphereScore/100) - $atmosphereDescription. " +
                "Deze factoren bepalen het verloop van $homeTeam vs $awayTeam."
        }
    }
    
    // ==================== KEY FACTOR TEMPLATES ====================
    
    fun getKeyFactorTemplates(
        style: ReportStyle,
        scenarioTitle: String,
        scenarioDescription: String,
        hasBreakingNews: Boolean,
        breakingNewsCount: Int
    ): List<String> {
        val factors = mutableListOf<String>()
        
        // Add scenario factor
        if (scenarioTitle.isNotEmpty()) {
            factors.add("Primair scenario: \"$scenarioTitle\"")
        } else if (scenarioDescription.isNotEmpty()) {
            val shortDesc = scenarioDescription.take(100) + if (scenarioDescription.length > 100) "..." else ""
            factors.add("AI analyse: $shortDesc")
        }
        
        // Add breaking news factor
        if (hasBreakingNews) {
            val newsText = when (breakingNewsCount) {
                1 -> "1 belangrijk nieuwsitem verwerkt in de analyse"
                in 2..3 -> "$breakingNewsCount recente nieuwsitems beïnvloeden de verwachting"
                else -> "Meerdere nieuwsbronnen gebruikt voor enhanced analyse"
            }
            factors.add(newsText)
        }
        
        // Add style-specific factors
        when (style) {
            ReportStyle.HIGH_CHAOS -> {
                factors.add("Hoge kaarten verwachting door emotionele lading")
                factors.add("Onvoorspelbare momenten kunnen de wedstrijd kantelen")
            }
            ReportStyle.MEDIUM_CHAOS -> {
                factors.add("Risicovolle situaties aan beide kanten")
                factors.add("Fouten kunnen duur komen te staan")
            }
            ReportStyle.FORTRESS -> {
                factors.add("Thuisvoordeel is beslissende factor")
                factors.add("Publiek kan momentum verschuiven")
            }
            ReportStyle.DEAD_STADIUM -> {
                factors.add("Beperkt publieksinvloed op spelers")
                factors.add("Technische kwaliteit boven emotie")
            }
            ReportStyle.HIGH_CONFIDENCE -> {
                factors.add("Duidelijke data-ondersteunde verwachting")
                factors.add("Beperkte verrassingen verwacht")
            }
            ReportStyle.LOW_CONFIDENCE -> {
                factors.add("Veel variabelen maken voorspelling moeilijk")
                factors.add("Kleine details kunnen groot verschil maken")
            }
            else -> {
                // Standard factors
                if (factors.size < 2) {
                    factors.add("Teamform en recente prestaties")
                    factors.add("Blessures en beschikbaarheid van sleutelspelers")
                }
            }
        }
        
        return factors
    }
    
    // ==================== TACTICAL INSIGHT TEMPLATES ====================
    
    fun getTacticalInsightTemplate(
        tacticalKey: String,
        style: ReportStyle,
        homeTeam: String,
        awayTeam: String
    ): String {
        if (tacticalKey.isNotEmpty()) {
            return tacticalKey
        }
        
        return when (style) {
            ReportStyle.HIGH_CHAOS ->
                "De wedstrijd wordt beslist in de middenveldslag waar de meeste duels plaatsvinden. " +
                "Wie de controle behoudt onder druk wint deze emotionele strijd."
                
            ReportStyle.MEDIUM_CHAOS ->
                "Balbezit en positionele discipline zijn cruciaal. " +
                "Het team dat de minste fouten maakt heeft de grootste kans."
                
            ReportStyle.FORTRESS ->
                "$homeTeam moet het thuisvoordeel maximaliseren door vroeg te scoren. " +
                "$awayTeam moet de eerste 20 minuten overleven zonder tegengoals."
                
            ReportStyle.DEAD_STADIUM ->
                "Technische kwaliteit boven fysieke kracht. " +
                "Het team met de beste individuele klasse zal waarschijnlijk winnen."
                
            ReportStyle.HIGH_CONFIDENCE ->
                "De verwachting is duidelijk: het team met de beste statistieken zal waarschijnlijk winnen. " +
                "Tactische verrassingen zijn onwaarschijnlijk."
                
            ReportStyle.LOW_CONFIDENCE ->
                "Tactische flexibiliteit is belangrijk. " +
                "Het team dat zich het beste kan aanpassen aan onverwachte situaties heeft een voordeel."
                
            ReportStyle.STANDARD ->
                "De wedstrijd wordt waarschijnlijk beslist door individuele acties en set-pieces. " +
                "Beide teams hebben vergelijkbare sterktes en zwaktes."
        }
    }
    
    // ==================== PLAYER FOCUS TEMPLATES ====================
    
    fun getPlayerFocusTemplate(
        keyPlayerWatch: String,
        style: ReportStyle,
        homeTeam: String,
        awayTeam: String
    ): String {
        if (keyPlayerWatch.isNotEmpty()) {
            return keyPlayerWatch
        }
        
        return when (style) {
            ReportStyle.HIGH_CHAOS ->
                "De aanvoerders en ervaren spelers moeten de rust bewaren in emotionele situaties. " +
                "Discipline is belangrijker dan individuele klasse."
                
            ReportStyle.MEDIUM_CHAOS ->
                "Creatieve spelers kunnen het verschil maken in krappe situaties. " +
                "De nummer 10 van beide teams zijn de sleutelfiguren."
                
            ReportStyle.FORTRESS ->
                "De thuisaanvaller moet vroeg scoren om het publiek achter het team te krijgen. " +
                "De uitverdediger moet de eerste storm overleven."
                
            ReportStyle.DEAD_STADIUM ->
                "Individuele klasse wordt belangrijker zonder publiekssteun. " +
                "De meest technisch begaafde spelers zullen waarschijnlijk uitblinken."
                
            ReportStyle.HIGH_CONFIDENCE ->
                "De verwachte matchwinnaar heeft consistente prestaties van hun sleutelspelers nodig. " +
                "Geen individuele helden nodig, wel teamdiscipline."
                
            ReportStyle.LOW_CONFIDENCE ->
                "Onverwachte helden kunnen opstaan. " +
                "Spelers die normaal niet scoren kunnen beslissend zijn in deze onzekere wedstrijd."
                
            ReportStyle.STANDARD ->
                "Beide teams zijn afhankelijk van hun sterspelers. " +
                "Wie van hen de beste dag heeft, bepaalt waarschijnlijk de uitslag."
        }
    }
    
    // ==================== CONCLUSION TEMPLATES ====================
    
    fun getConclusionTemplate(
        style: ReportStyle,
        confidence: Int,
        homeTeam: String,
        awayTeam: String,
        bettingTip: String
    ): String {
        val confidenceDescription = when {
            confidence >= 80 -> "hoge zekerheid"
            confidence >= 60 -> "redelijke zekerheid"
            confidence >= 40 -> "matige zekerheid"
            else -> "lage zekerheid"
        }
        
        val baseConclusion = when (style) {
            ReportStyle.HIGH_CHAOS ->
                "Gebaseerd op de totale oorlogssfeer en hoge chaos verwacht ik een intense wedstrijd " +
                "met veel kaarten en emotionele momenten, waarin het team dat de kalmte bewaart " +
                "uiteindelijk met $confidenceDescription zegeviert."
                
            ReportStyle.MEDIUM_CHAOS ->
                "Met het hoge risico niveau verwacht ik een geforceerde wedstrijd waarin fouten " +
                "duur komen te staan. Het team met de beste discipline wint met $confidenceDescription."
                
            ReportStyle.FORTRESS ->
                "Het thuisvoordeel in deze heksenketel is beslissend. $homeTeam zal profiteren " +
                "van het publiek en met $confidenceDescription de overwinning pakken."
                
            ReportStyle.DEAD_STADIUM ->
                "Zonder publieksinvloed wordt dit een technische aangelegenheid. " +
                "Het team met de meeste individuele klasse wint met $confidenceDescription."
                
            ReportStyle.HIGH_CONFIDENCE ->
                "De data wijst duidelijk in één richting. Ik verwacht met $confidenceDescription " +
                "dat de voorspelling uitkomt, tenzij onverwachte factoren een rol spelen."
                
            ReportStyle.LOW_CONFIDENCE ->
                "Met zoveel variabelen is elke uitslag mogelijk. Mijn verwachting heeft $confidenceDescription " +
                "en kleine details kunnen de wedstrijd kantelen."
                
            ReportStyle.STANDARD ->
                "Gebaseerd op alle beschikbare data verwacht ik met $confidenceDescription " +
                "dat de meest waarschijnlijke uitkomst zich voordoet in deze evenwichtige confrontatie."
        }
        
        if (bettingTip.isNotEmpty()) {
            return "$baseConclusion Betting perspectief: $bettingTip."
        }
        
        return baseConclusion
    }
}

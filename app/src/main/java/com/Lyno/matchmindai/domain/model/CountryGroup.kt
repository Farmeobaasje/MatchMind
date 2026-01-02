package com.Lyno.matchmindai.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a group of leagues organized by country.
 * Used for displaying leagues grouped by country in a hierarchical menu.
 */
@Serializable
data class CountryGroup(
    val countryCode: String,
    val countryName: String,
    val flagEmoji: String,
    val leagues: List<LeagueGroup>,
    var isExpanded: Boolean = false,
    var isSelected: Boolean = false
) {
    /**
     * Display name for the country group.
     */
    val displayName: String
        get() = "$flagEmoji $countryName"

    /**
     * Number of leagues in this country group.
     */
    val leagueCount: Int
        get() = leagues.size

    /**
     * Total number of matches across all leagues in this country.
     */
    val totalMatchCount: Int
        get() = leagues.sumOf { it.matchCount }

    /**
     * Toggle the expanded state of this country group.
     */
    fun toggleExpanded() {
        isExpanded = !isExpanded
    }

    /**
     * Toggle the selected state of this country group.
     * When selected, all leagues in this country are considered selected.
     */
    fun toggleSelected() {
        isSelected = !isSelected
        // Optionally select/deselect all leagues when country is toggled
        leagues.forEach { league ->
            league.isExpanded = isSelected
        }
    }

    /**
     * Get selected leagues from this country.
     */
    val selectedLeagues: List<LeagueGroup>
        get() = leagues.filter { it.isExpanded }

    companion object {
        /**
         * Creates an empty country group for error cases.
         */
        fun empty(): CountryGroup = CountryGroup(
            countryCode = "",
            countryName = "",
            flagEmoji = "",
            leagues = emptyList()
        )

        /**
         * Map of country codes to flag emojis.
         */
        val COUNTRY_FLAGS = mapOf(
            "NL" to "🇳🇱", // Netherlands
            "GB" to "🇬🇧", // United Kingdom
            "ES" to "🇪🇸", // Spain
            "DE" to "🇩🇪", // Germany
            "IT" to "🇮🇹", // Italy
            "FR" to "🇫🇷", // France
            "PT" to "🇵🇹", // Portugal
            "BE" to "🇧🇪", // Belgium
            "TR" to "🇹🇷", // Turkey
            "RU" to "🇷🇺", // Russia
            "US" to "🇺🇸", // United States
            "BR" to "🇧🇷", // Brazil
            "AR" to "🇦🇷", // Argentina
            "MX" to "🇲🇽", // Mexico
            "JP" to "🇯🇵", // Japan
            "KR" to "🇰🇷", // South Korea
            "CN" to "🇨🇳", // China
            "AU" to "🇦🇺", // Australia
            "SA" to "🇸🇦", // Saudi Arabia
            "AE" to "🇦🇪"  // United Arab Emirates
        )

        /**
         * Get flag emoji for a country code.
         * @param countryCode The ISO country code
         * @return Flag emoji or empty string if not found
         */
        fun getFlagEmoji(countryCode: String): String {
            return COUNTRY_FLAGS[countryCode.uppercase()] ?: "🏴"
        }

        /**
         * Priority order for countries to determine sorting.
         * Higher priority countries appear first.
         */
        val COUNTRY_PRIORITY = mapOf(
            "NL" to 1,   // Netherlands (Eredivisie)
            "GB" to 2,   // United Kingdom (Premier League)
            "ES" to 3,   // Spain (La Liga)
            "DE" to 4,   // Germany (Bundesliga)
            "IT" to 5,   // Italy (Serie A)
            "FR" to 6,   // France (Ligue 1)
            "PT" to 7,   // Portugal (Primeira Liga)
            "BE" to 8,   // Belgium (Jupiler Pro League)
            "TR" to 9,   // Turkey (Süper Lig)
            "US" to 10   // United States (MLS)
        )

        /**
         * Get priority for a country code.
         * @param countryCode The country code
         * @return Priority value (lower = higher priority), or Int.MAX_VALUE if not in priority list
         */
        fun getCountryPriority(countryCode: String): Int {
            return COUNTRY_PRIORITY[countryCode.uppercase()] ?: Int.MAX_VALUE
        }

        /**
         * Group leagues by country.
         * @param leagues List of leagues to group
         * @return List of CountryGroup objects
         */
        fun groupLeaguesByCountry(leagues: List<LeagueGroup>): List<CountryGroup> {
            val leaguesByCountry = mutableMapOf<String, MutableList<LeagueGroup>>()
            
            // Group leagues by country
            leagues.forEach { league ->
                val countryKey = league.country.ifEmpty { "Unknown" }
                leaguesByCountry.getOrPut(countryKey) { mutableListOf() }.add(league)
            }
            
            // Convert to CountryGroup objects
            return leaguesByCountry.map { (countryName, countryLeagues) ->
                // Try to extract country code from country name
                val countryCode = extractCountryCode(countryName)
                
                CountryGroup(
                    countryCode = countryCode,
                    countryName = countryName,
                    flagEmoji = getFlagEmoji(countryCode),
                    leagues = countryLeagues.sortedBy { LeagueGroup.getLeaguePriority(it.leagueId) },
                    isExpanded = false,
                    isSelected = false
                )
            }.sortedBy { getCountryPriority(it.countryCode) }
        }

        /**
         * Extract country code from country name.
         * @param countryName The full country name
         * @return Country code or empty string
         */
        private fun extractCountryCode(countryName: String): String {
            return when {
                countryName.contains("Netherlands", ignoreCase = true) -> "NL"
                countryName.contains("England", ignoreCase = true) -> "GB"
                countryName.contains("United Kingdom", ignoreCase = true) -> "GB"
                countryName.contains("Spain", ignoreCase = true) -> "ES"
                countryName.contains("Germany", ignoreCase = true) -> "DE"
                countryName.contains("Italy", ignoreCase = true) -> "IT"
                countryName.contains("France", ignoreCase = true) -> "FR"
                countryName.contains("Portugal", ignoreCase = true) -> "PT"
                countryName.contains("Belgium", ignoreCase = true) -> "BE"
                countryName.contains("Turkey", ignoreCase = true) -> "TR"
                countryName.contains("USA", ignoreCase = true) -> "US"
                countryName.contains("United States", ignoreCase = true) -> "US"
                countryName.contains("Brazil", ignoreCase = true) -> "BR"
                countryName.contains("Argentina", ignoreCase = true) -> "AR"
                countryName.contains("Mexico", ignoreCase = true) -> "MX"
                countryName.contains("Japan", ignoreCase = true) -> "JP"
                countryName.contains("South Korea", ignoreCase = true) -> "KR"
                countryName.contains("China", ignoreCase = true) -> "CN"
                countryName.contains("Australia", ignoreCase = true) -> "AU"
                countryName.contains("Saudi Arabia", ignoreCase = true) -> "SA"
                countryName.contains("UAE", ignoreCase = true) -> "AE"
                countryName.contains("United Arab Emirates", ignoreCase = true) -> "AE"
                else -> ""
            }
        }
    }
}

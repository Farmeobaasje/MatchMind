package com.Lyno.matchmindai.common.utils

/**
 * String utilities for fuzzy matching and text processing.
 * Used by the smart team extractor to handle typos and variations.
 */
object StringUtils {

    /**
     * Simple fuzzy matching that checks for similarity between two strings.
     * Handles:
     * 1. Exact match
     * 2. Contains match (either direction)
     * 3. Word boundary matching
     * 
     * @param str1 First string to compare
     * @param str2 Second string to compare
     * @return true if strings are similar enough to be considered a match
     */
    fun isSimilar(str1: String, str2: String): Boolean {
        val clean1 = str1.lowercase().trim()
        val clean2 = str2.lowercase().trim()
        
        // 1. Exact match
        if (clean1 == clean2) return true
        
        // 2. Contains match (either direction)
        if (clean1.contains(clean2) || clean2.contains(clean1)) return true
        
        // 3. Word boundary matching
        val words1 = clean1.split(" ").filter { it.isNotBlank() }
        val words2 = clean2.split(" ").filter { it.isNotBlank() }
        
        // Check if any word from str1 is contained in str2 or vice versa
        if (words1.any { word1 -> words2.any { word2 -> word1.contains(word2) || word2.contains(word1) } }) {
            return true
        }
        
        // 4. Levenshtein distance for short strings (typo handling)
        if (clean1.length <= 10 && clean2.length <= 10) {
            val distance = levenshteinDistance(clean1, clean2)
            val maxLength = maxOf(clean1.length, clean2.length)
            // Allow 1-2 character typos for short team names
            if (maxLength > 0 && distance <= 2 && distance.toDouble() / maxLength <= 0.3) {
                return true
            }
        }
        
        return false
    }

    /**
     * Extension function for String to check similarity.
     */
    fun String.isSimilarTo(other: String): Boolean = isSimilar(this, other)

    /**
     * Calculate Levenshtein distance between two strings.
     * Used for fuzzy matching with typos.
     */
    private fun levenshteinDistance(str1: String, str2: String): Int {
        val m = str1.length
        val n = str2.length
        val dp = Array(m + 1) { IntArray(n + 1) }
        
        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j
        
        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        
        return dp[m][n]
    }

    /**
     * Clean a query by removing common stopwords.
     * Helps the team extractor focus on the important parts.
     */
    fun cleanQuery(query: String): List<String> {
        val stopWords = listOf(
            "voorspel", "analyseer", "wedstrijd", "tegen", "vs", "match", "check",
            "wat", "denk", "je", "van", "hoe", "gaat", "het", "met", "die", "deze",
            "de", "het", "een", "van", "voor", "aan", "in", "op", "uit", "met"
        )
        
        return query.lowercase()
            .split(" ")
            .filter { it.isNotBlank() && it !in stopWords }
            .map { it.trim() }
    }
}

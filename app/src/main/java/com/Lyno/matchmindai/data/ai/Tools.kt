package com.Lyno.matchmindai.data.ai

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.add

/**
 * Tool definitions for DeepSeek function calling.
 * These tools allow the AI to autonomously fetch data from external APIs.
 */
object Tools {

    /**
     * Creates the get_fixtures tool definition for fetching match fixtures.
     */
    fun createGetFixturesTool(): JsonObject {
        return buildJsonObject {
            put("type", "function")
            putJsonObject("function") {
                put("name", "get_fixtures")
                put("description", "Get football match fixtures for a specific date, league, or live matches. Use this tool whenever the user mentions a team name (e.g., 'Ajax', 'PSV') without context, or asks for a schedule/calendar. DO NOT ask for a second team. Just search for the team provided.")
                putJsonObject("parameters") {
                    put("type", "object")
                    putJsonObject("properties") {
                        putJsonObject("date") {
                            put("type", "string")
                            put("description", "Date in YYYY-MM-DD format. Use today's date if not specified.")
                        }
                        putJsonObject("live") {
                            put("type", "boolean")
                            put("description", "Whether to get live matches only. Default is false.")
                        }
                        putJsonObject("league_id") {
                            put("type", "integer")
                            put("description", "League ID to filter matches. Optional.")
                        }
                        putJsonObject("search_query") {
                            put("type", "string")
                            put("description", "The name of a team if the user is searching specifically (e.g., 'Ajax'). Leave empty if the user wants to see everything.")
                        }
                    }
                    putJsonArray("required") {
                        // No required parameters, all are optional
                    }
                }
            }
        }
    }

    /**
     * Creates the tavily_search tool definition for searching the internet.
     */
    fun createTavilySearchTool(): JsonObject {
        return buildJsonObject {
            put("type", "function")
            putJsonObject("function") {
                put("name", "tavily_search")
                put("description", "Search the internet for real-time information about teams, players, injuries, transfers, and news. Use this when you need up-to-date information not available in your training data.")
                putJsonObject("parameters") {
                    put("type", "object")
                    putJsonObject("properties") {
                        putJsonObject("query") {
                            put("type", "string")
                            put("description", "Search query for match results, scores, injuries, transfers, and news")
                        }
                        putJsonObject("focus") {
                            put("type", "string")
                            putJsonArray("enum") {
                                add("stats")
                                add("news")
                                add("general")
                            }
                            put("description", "Choose 'stats' for scores/standings, 'news' for injuries/lineups, 'general' for mixed results.")
                        }
                    }
                    putJsonArray("required") {
                        add("query")
                        add("focus")
                    }
                }
            }
        }
    }

    /**
     * Creates the get_live_scores tool definition for fetching live match scores.
     */
    fun createGetLiveScoresTool(): JsonObject {
        return buildJsonObject {
            put("type", "function")
            putJsonObject("function") {
                put("name", "get_live_scores")
                put("description", "Get live football match scores and updates. Use this when the user asks about current matches, live scores, or what's happening right now.")
                putJsonObject("parameters") {
                    put("type", "object")
                    putJsonObject("properties") {
                        putJsonObject("league_id") {
                            put("type", "integer")
                            put("description", "League ID to filter live matches. Optional.")
                        }
                    }
                    putJsonArray("required") {
                        // No required parameters
                    }
                }
            }
        }
    }

    /**
     * Creates the get_match_prediction tool definition for match predictions.
     */
    fun createGetMatchPredictionTool(): JsonObject {
        return buildJsonObject {
            put("type", "function")
            putJsonObject("function") {
                put("name", "get_match_prediction")
                put("description", "Gebruik deze flow voor een analyse:\n1. Haal data op via `get_fixtures` (voor H2H/Vorm).\n2. Haal nieuws op via `tavily_search` (query: 'blessures [Thuis] [Uit] voorbeschouwing').\n3. Genereer dan pas je antwoord.\n\nUse this when the user asks about who will win, predictions, or match analysis.")
                putJsonObject("parameters") {
                    put("type", "object")
                    putJsonObject("properties") {
                        putJsonObject("fixture_id") {
                            put("type", "integer")
                            put("description", "Fixture ID of the match to analyze. Required for accurate predictions.")
                        }
                    }
                    putJsonArray("required") {
                        add("fixture_id")
                    }
                }
            }
        }
    }

    /**
     * Creates the get_standings tool definition for league standings.
     */
    fun createGetStandingsTool(): JsonObject {
        return buildJsonObject {
            put("type", "function")
            putJsonObject("function") {
                put("name", "get_standings")
                put("description", "Get the OFFICIAL and LIVE league table/standings. Use this for ANY question about 'who is first', 'points', 'ranking', or 'coach'. This is the source of truth for factual data like standings, points, and current coaches. NEVER use tavily_search for this type of information.")
                putJsonObject("parameters") {
                    put("type", "object")
                    putJsonObject("properties") {
                        putJsonObject("league_id") {
                            put("type", "integer")
                            put("description", "League ID to get standings for. Required.")
                        }
                        putJsonObject("season") {
                            put("type", "integer")
                            put("description", "Season year. Defaults to current season.")
                        }
                    }
                    putJsonArray("required") {
                        add("league_id")
                    }
                }
            }
        }
    }

    /**
     * Creates the get_best_odds tool definition for fetching beginner-friendly betting odds.
     */
    fun createGetBestOddsTool(): JsonObject {
        return buildJsonObject {
            put("type", "function")
            putJsonObject("function") {
                put("name", "get_best_odds")
                put("description", "Get beginner-friendly betting odds with safety and value ratings. Use this when the user asks about betting, odds, or wants to know which matches have good betting opportunities. This tool filters for safe bets with good value for beginners.")
                putJsonObject("parameters") {
                    put("type", "object")
                    putJsonObject("properties") {
                        putJsonObject("date") {
                            put("type", "string")
                            put("description", "Date in YYYY-MM-DD format. Defaults to today.")
                        }
                        putJsonObject("league_id") {
                            put("type", "integer")
                            put("description", "League ID to filter odds. Optional.")
                        }
                        putJsonObject("team_name") {
                            put("type", "string")
                            put("description", "Team name to filter odds for a specific team. Optional.")
                        }
                        putJsonObject("limit") {
                            put("type", "integer")
                            put("description", "Maximum number of odds to return. Default is 10.")
                        }
                    }
                    putJsonArray("required") {
                        // No required parameters, all are optional
                    }
                }
            }
        }
    }

    /**
     * Creates the get_injuries tool definition for fetching player injuries for a specific fixture.
     */
    fun createGetInjuriesTool(): JsonObject {
        return buildJsonObject {
            put("type", "function")
            putJsonObject("function") {
                put("name", "get_injuries")
                put("description", "Get player injuries for a specific football match. Use this when the user asks about injured players, team availability, or why certain players are missing from a match.")
                putJsonObject("parameters") {
                    put("type", "object")
                    putJsonObject("properties") {
                        putJsonObject("fixture_id") {
                            put("type", "integer")
                            put("description", "Fixture ID of the match to get injuries for. Required.")
                        }
                    }
                    putJsonArray("required") {
                        add("fixture_id")
                    }
                }
            }
        }
    }

    /**
     * Creates the get_predictions tool definition for fetching match predictions.
     */
    fun createGetPredictionsTool(): JsonObject {
        return buildJsonObject {
            put("type", "function")
            putJsonObject("function") {
                put("name", "get_predictions")
                put("description", "Get detailed match predictions including win probability, expected goals, and analysis. Use this when the user asks for predictions, expected outcomes, or statistical analysis of a match.")
                putJsonObject("parameters") {
                    put("type", "object")
                    putJsonObject("properties") {
                        putJsonObject("fixture_id") {
                            put("type", "integer")
                            put("description", "Fixture ID of the match to get predictions for. Required.")
                        }
                    }
                    putJsonArray("required") {
                        add("fixture_id")
                    }
                }
            }
        }
    }

    /**
     * Creates the get_odds tool definition for fetching match-specific odds.
     */
    fun createGetOddsTool(): JsonObject {
        return buildJsonObject {
            put("type", "function")
            putJsonObject("function") {
                put("name", "get_odds")
                put("description", "Get detailed betting odds for a specific match including match result, over/under, and both teams to score odds. Use this when the user asks about specific match odds or wants detailed betting information for a particular game.")
                putJsonObject("parameters") {
                    put("type", "object")
                    putJsonObject("properties") {
                        putJsonObject("fixture_id") {
                            put("type", "integer")
                            put("description", "Fixture ID of the match to get odds for. Required.")
                        }
                    }
                    putJsonArray("required") {
                        add("fixture_id")
                    }
                }
            }
        }
    }

    /**
     * Gets all available tools as a list of JsonObjects for DeepSeek API.
     */
    fun getAllTools(): List<JsonObject> {
        return listOf(
            createGetFixturesTool(),
            createTavilySearchTool(),
            createGetLiveScoresTool(),
            createGetMatchPredictionTool(),
            createGetStandingsTool(),
            createGetBestOddsTool(),
            createGetInjuriesTool(),
            createGetPredictionsTool(),
            createGetOddsTool()
        )
    }

    /**
     * Gets tool names as a list for logging and debugging.
     */
    fun getToolNames(): List<String> {
        return listOf(
            "get_fixtures",
            "tavily_search",
            "get_live_scores",
            "get_match_prediction",
            "get_standings",
            "get_best_odds",
            "get_injuries",
            "get_predictions",
            "get_odds"
        )
    }
}

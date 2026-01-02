package com.Lyno.matchmindai.data.dto

import com.Lyno.matchmindai.data.dto.football.FixtureLineupsResponse
import com.Lyno.matchmindai.data.dto.football.FixtureLineupsWrapper
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FixtureLineupsResponseTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun `should parse player with null id`() {
        val jsonString = """
            {
                "response": [
                    {
                        "team": {
                            "id": 1,
                            "name": "Team A",
                            "logo": "logo.png"
                        },
                        "formation": "4-3-3",
                        "startXI": [
                            {
                                "player": {
                                    "id": null,
                                    "name": "Rendijs Mihelsons",
                                    "number": 10,
                                    "pos": "M",
                                    "grid": "C4",
                                    "photo": "photo.png"
                                },
                                "position": "CM"
                            }
                        ],
                        "substitutes": [],
                        "missing": []
                    }
                ]
            }
        """.trimIndent()

        val wrapper = json.decodeFromString<FixtureLineupsWrapper>(jsonString)
        val response = wrapper.response.firstOrNull()
        
        assertEquals(1, response?.team?.id)
        assertEquals("Team A", response?.team?.name)
        
        val player = response?.startXI?.firstOrNull()?.player
        assertEquals("Rendijs Mihelsons", player?.name)
        assertNull(player?.id) // Should be null
        assertEquals(10, player?.number)
    }

    @Test
    fun `should parse player with valid id`() {
        val jsonString = """
            {
                "response": [
                    {
                        "team": {
                            "id": 2,
                            "name": "Team B",
                            "logo": "logo2.png"
                        },
                        "formation": "4-4-2",
                        "startXI": [
                            {
                                "player": {
                                    "id": 12345,
                                    "name": "John Doe",
                                    "number": 7,
                                    "pos": "F",
                                    "grid": "A1",
                                    "photo": "photo2.png"
                                },
                                "position": "ST"
                            }
                        ],
                        "substitutes": [],
                        "missing": []
                    }
                ]
            }
        """.trimIndent()

        val wrapper = json.decodeFromString<FixtureLineupsWrapper>(jsonString)
        val response = wrapper.response.firstOrNull()
        
        val player = response?.startXI?.firstOrNull()?.player
        assertEquals(12345, player?.id)
        assertEquals("John Doe", player?.name)
    }

    @Test
    fun `should parse missing player with null id`() {
        val jsonString = """
            {
                "response": [
                    {
                        "team": {
                            "id": 3,
                            "name": "Team C",
                            "logo": "logo3.png"
                        },
                        "formation": "3-5-2",
                        "startXI": [],
                        "substitutes": [],
                        "missing": [
                            {
                                "player": {
                                    "id": null,
                                    "name": "Injured Player",
                                    "photo": "injured.png"
                                },
                                "reason": "Injury"
                            }
                        ]
                    }
                ]
            }
        """.trimIndent()

        val wrapper = json.decodeFromString<FixtureLineupsWrapper>(jsonString)
        val response = wrapper.response.firstOrNull()
        
        val missingPlayer = response?.missing?.firstOrNull()?.player
        assertEquals("Injured Player", missingPlayer?.name)
        assertNull(missingPlayer?.id)
        assertEquals("Injured.png", missingPlayer?.photo)
    }
}

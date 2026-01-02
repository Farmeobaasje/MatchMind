package com.Lyno.matchmindai.domain.service

import com.Lyno.matchmindai.domain.model.LiveMatchData
import com.Lyno.matchmindai.domain.model.LiveEvent
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.domain.repository.MatchRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

/**
 * Service for real-time live match updates in DashX dashboard.
 * Provides WebSocket/polling infrastructure for live score updates and events.
 */
class LiveMatchUpdateService(
    private val matchRepository: MatchRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val _liveUpdates = MutableSharedFlow<LiveMatchUpdate>()
    val liveUpdates: SharedFlow<LiveMatchUpdate> = _liveUpdates.asSharedFlow()

    private val activePollingJobs = ConcurrentHashMap<Int, Job>()
    private val lastUpdates = ConcurrentHashMap<Int, LiveMatchData>()

    /**
     * Start live updates for a specific fixture.
     * @param fixtureId The ID of the fixture to track
     * @param pollingIntervalSeconds Interval between polls in seconds (default: 30)
     */
    fun startLiveUpdates(fixtureId: Int, pollingIntervalSeconds: Int = 30) {
        // Stop existing polling job if any
        stopLiveUpdates(fixtureId)

        // Start new polling job
        val job = coroutineScope.launch {
            while (true) {
                try {
                    pollLiveMatchData(fixtureId)
                    delay(pollingIntervalSeconds * 1000L)
                } catch (e: Exception) {
                    // Log error and continue polling
                    emitErrorUpdate(fixtureId, e)
                    delay(60.seconds.inWholeMilliseconds) // Wait longer on error
                }
            }
        }

        activePollingJobs[fixtureId] = job
    }

    /**
     * Start live updates for multiple fixtures.
     * @param fixtureIds List of fixture IDs to track
     * @param pollingIntervalSeconds Interval between polls in seconds (default: 30)
     */
    fun startLiveUpdates(fixtureIds: List<Int>, pollingIntervalSeconds: Int = 30) {
        fixtureIds.forEach { fixtureId ->
            startLiveUpdates(fixtureId, pollingIntervalSeconds)
        }
    }

    /**
     * Stop live updates for a specific fixture.
     * @param fixtureId The ID of the fixture to stop tracking
     */
    fun stopLiveUpdates(fixtureId: Int) {
        activePollingJobs[fixtureId]?.cancel()
        activePollingJobs.remove(fixtureId)
        lastUpdates.remove(fixtureId)
    }

    /**
     * Stop all live updates.
     */
    fun stopAllLiveUpdates() {
        activePollingJobs.values.forEach { it.cancel() }
        activePollingJobs.clear()
        lastUpdates.clear()
    }

    /**
     * Get current live data for a fixture (cached).
     * @param fixtureId The ID of the fixture
     * @return Cached LiveMatchData or null if not available
     */
    fun getCurrentLiveData(fixtureId: Int): LiveMatchData? {
        return lastUpdates[fixtureId]
    }

    /**
     * Check if a fixture is being tracked.
     * @param fixtureId The ID of the fixture
     * @return True if the fixture is being tracked
     */
    fun isTrackingFixture(fixtureId: Int): Boolean {
        return activePollingJobs.containsKey(fixtureId)
    }

    /**
     * Get all tracked fixture IDs.
     * @return List of tracked fixture IDs
     */
    fun getTrackedFixtures(): List<Int> {
        return activePollingJobs.keys.toList()
    }

    /**
     * Poll live match data from repository and emit updates.
     * @param fixtureId The ID of the fixture to poll
     */
    private suspend fun pollLiveMatchData(fixtureId: Int) {
        withContext(Dispatchers.IO) {
            val result = matchRepository.getLiveMatchData(fixtureId)
            
            result.onSuccess { liveMatchData ->
                val previousData = lastUpdates[fixtureId]
                lastUpdates[fixtureId] = liveMatchData

                // Check if there are new events
                val hasNewEvents = previousData?.events?.size != liveMatchData.events.size
                val newEvents = if (hasNewEvents && previousData != null) {
                    liveMatchData.events.filter { newEvent ->
                        !previousData.events.any { it.id == newEvent.id }
                    }
                } else {
                    emptyList()
                }

                // Emit update
                val update = LiveMatchUpdate(
                    fixtureId = fixtureId,
                    liveMatchData = liveMatchData,
                    hasNewEvents = hasNewEvents,
                    newEvents = newEvents,
                    previousData = previousData,
                    timestamp = System.currentTimeMillis()
                )

                _liveUpdates.emit(update)
            }.onFailure { error ->
                emitErrorUpdate(fixtureId, error)
            }
        }
    }

    /**
     * Emit an error update.
     * @param fixtureId The ID of the fixture
     * @param error The error that occurred
     */
    private suspend fun emitErrorUpdate(fixtureId: Int, error: Throwable) {
        val update = LiveMatchUpdate(
            fixtureId = fixtureId,
            liveMatchData = null,
            hasNewEvents = false,
            newEvents = emptyList(),
            previousData = null,
            timestamp = System.currentTimeMillis(),
            error = error
        )
        _liveUpdates.emit(update)
    }

    /**
     * Start live updates for all live matches in a list of fixtures.
     * @param fixtures List of match fixtures
     * @param pollingIntervalSeconds Interval between polls in seconds (default: 30)
     */
    fun startLiveUpdatesForLiveMatches(
        fixtures: List<MatchFixture>,
        pollingIntervalSeconds: Int = 30
    ) {
        val liveFixtureIds = fixtures
            .filter { it.status in listOf("1H", "2H", "HT", "LIVE", "IN_PLAY") }
            .mapNotNull { it.fixtureId }
        
        startLiveUpdates(liveFixtureIds, pollingIntervalSeconds)
    }

    /**
     * Convert LiveMatchData to simplified update for dashboard display.
     * @param liveMatchData The live match data
     * @return Simplified update object
     */
    fun toDashboardUpdate(liveMatchData: LiveMatchData): DashboardLiveUpdate {
        return DashboardLiveUpdate(
            fixtureId = liveMatchData.fixtureId,
            homeScore = liveMatchData.homeScore,
            awayScore = liveMatchData.awayScore,
            status = liveMatchData.status,
            elapsedTime = liveMatchData.elapsedTime,
            extraTime = liveMatchData.extraTime,
            hasNewEvents = liveMatchData.events.isNotEmpty(),
            latestEvent = liveMatchData.events.lastOrNull(),
            possession = liveMatchData.possession,
            shotsOnTarget = liveMatchData.shotsOnTarget,
            lastUpdated = liveMatchData.lastUpdated
        )
    }
}

/**
 * Simplified dashboard live update for UI display.
 */
data class DashboardLiveUpdate(
    val fixtureId: Int,
    val homeScore: Int,
    val awayScore: Int,
    val status: com.Lyno.matchmindai.domain.model.MatchStatus,
    val elapsedTime: Int?,
    val extraTime: Int?,
    val hasNewEvents: Boolean,
    val latestEvent: LiveEvent?,
    val possession: com.Lyno.matchmindai.domain.model.LiveStatistic?,
    val shotsOnTarget: com.Lyno.matchmindai.domain.model.LiveStatistic?,
    val lastUpdated: Long
) {
    /**
     * Get score display.
     */
    val scoreDisplay: String
        get() = "$homeScore-$awayScore"

    /**
     * Get elapsed time display.
     */
    val elapsedTimeDisplay: String
        get() = if (extraTime != null && extraTime > 0) {
            "${elapsedTime ?: 0}+${extraTime}'"
        } else {
            "${elapsedTime ?: 0}'"
        }

    /**
     * Check if match is live.
     */
    val isLive: Boolean
        get() = status in listOf(
            com.Lyno.matchmindai.domain.model.MatchStatus.FIRST_HALF,
            com.Lyno.matchmindai.domain.model.MatchStatus.HALFTIME,
            com.Lyno.matchmindai.domain.model.MatchStatus.SECOND_HALF,
            com.Lyno.matchmindai.domain.model.MatchStatus.EXTRA_TIME
        )

    /**
     * Get possession percentage for home team.
     */
    val homePossession: Int
        get() = possession?.homePercentage ?: 50

    /**
     * Get possession percentage for away team.
     */
    val awayPossession: Int
        get() = possession?.awayPercentage ?: 50
}

/**
 * Live match update data class.
 */
data class LiveMatchUpdate(
    val fixtureId: Int,
    val liveMatchData: LiveMatchData?,
    val hasNewEvents: Boolean,
    val newEvents: List<LiveEvent>,
    val previousData: LiveMatchData?,
    val timestamp: Long,
    val error: Throwable? = null
) {
    /**
     * Check if this update contains valid data.
     */
    val isValid: Boolean
        get() = liveMatchData != null && error == null

    /**
     * Check if this is an error update.
     */
    val isError: Boolean
        get() = error != null

    /**
     * Get score display if available.
     */
    val scoreDisplay: String?
        get() = liveMatchData?.scoreDisplay

    /**
     * Get elapsed time display if available.
     */
    val elapsedTimeDisplay: String?
        get() = liveMatchData?.elapsedTimeDisplay
}

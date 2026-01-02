package com.Lyno.matchmindai.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.Lyno.matchmindai.domain.model.HistoricalFixture
import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.domain.repository.MatchRepository
import com.Lyno.matchmindai.domain.service.MatchCacheManager
import com.Lyno.matchmindai.domain.usecase.GetHybridPredictionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class MatchDetailViewModelDataFlowTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var viewModel: MatchDetailViewModel
    private lateinit var mockRepository: MatchRepository
    private lateinit var mockCacheManager: MatchCacheManager
    private lateinit var mockUseCase: GetHybridPredictionUseCase

    private val testFixtureId = 12345
    private val testMatchDetail = MatchDetail(
        fixtureId = testFixtureId,
        homeTeam = "Test Home Team",
        awayTeam = "Test Away Team",
        homeTeamId = 100,
        awayTeamId = 200,
        leagueId = 10,
        league = "Test League",
        status = null,
        score = null,
        stats = emptyList(),
        events = emptyList(),
        lineups = emptyList(),
        venue = null,
        referee = null,
        date = "2025-12-21T20:00:00+00:00"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockRepository = mock()
        mockCacheManager = mock()
        mockUseCase = mock()

        // Mock successful historical fixtures response
        val historicalFixtures = listOf(
            HistoricalFixture(
                fixtureId = 1,
                homeTeamId = 100,
                awayTeamId = 300,
                homeTeamName = "Test Home Team",
                awayTeamName = "Team C",
                homeGoals = 2,
                awayGoals = 1,
                date = "2025-12-01T20:00:00+00:00",
                leagueId = 10,
                leagueName = "Test League",
                season = 2025,
                status = "FT"
            ),
            HistoricalFixture(
                fixtureId = 2,
                homeTeamId = 200,
                awayTeamId = 100,
                homeTeamName = "Team B",
                awayTeamName = "Test Home Team",
                homeGoals = 0,
                awayGoals = 2,
                date = "2025-12-08T20:00:00+00:00",
                leagueId = 10,
                leagueName = "Test League",
                season = 2025,
                status = "FT"
            ),
            HistoricalFixture(
                fixtureId = 3,
                homeTeamId = 200,
                awayTeamId = 400,
                homeTeamName = "Test Away Team",
                awayTeamName = "Team D",
                homeGoals = 3,
                awayGoals = 1,
                date = "2025-12-01T20:00:00+00:00",
                leagueId = 10,
                leagueName = "Test League",
                season = 2025,
                status = "FT"
            )
        )

        whenever(mockRepository.getHistoricalFixturesForPrediction(
            homeTeamId = 100,
            awayTeamId = 200,
            leagueId = 10,
            season = Calendar.getInstance().get(Calendar.YEAR)
        )).thenReturn(Result.success(historicalFixtures))

        // Mock match details response
        whenever(mockRepository.getMatchDetails(testFixtureId))
            .thenReturn(flowOf(com.Lyno.matchmindai.common.Resource.Success(testMatchDetail)))

        viewModel = MatchDetailViewModel(
            fixtureId = testFixtureId,
            matchRepository = mockRepository,
            matchCacheManager = mockCacheManager,
            getHybridPredictionUseCase = mockUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadHybridPrediction should fetch historical fixtures from repository`() = testScope.runTest {
        // Given
        val mockEnhancedPrediction = mock<com.Lyno.matchmindai.domain.model.EnhancedPrediction>()
        whenever(mockUseCase.invoke(
            fixtureId = testFixtureId,
            matchDetail = testMatchDetail,
            homeTeamFixtures = any(),
            awayTeamFixtures = any(),
            leagueFixtures = any()
        )).thenReturn(Result.success(mockEnhancedPrediction))

        // When
        viewModel.loadHybridPrediction(testMatchDetail)
        advanceUntilIdle()

        // Then
        verify(mockRepository).getHistoricalFixturesForPrediction(
            homeTeamId = 100,
            awayTeamId = 200,
            leagueId = 10,
            season = Calendar.getInstance().get(Calendar.YEAR)
        )
    }

    @Test
    fun `loadHybridPrediction should filter home team fixtures correctly`() = testScope.runTest {
        // Given
        val mockEnhancedPrediction = mock<com.Lyno.matchmindai.domain.model.EnhancedPrediction>()
        whenever(mockUseCase.invoke(
            fixtureId = testFixtureId,
            matchDetail = testMatchDetail,
            homeTeamFixtures = any(),
            awayTeamFixtures = any(),
            leagueFixtures = any()
        )).thenReturn(Result.success(mockEnhancedPrediction))

        // When
        viewModel.loadHybridPrediction(testMatchDetail)
        advanceUntilIdle()

        // Then - Verify that home team fixtures are filtered correctly
        // Home team ID is 100, so we expect 2 fixtures where homeTeamId = 100 OR awayTeamId = 100
        verify(mockUseCase).invoke(
            fixtureId = testFixtureId,
            matchDetail = testMatchDetail,
            homeTeamFixtures = listOf(
                HistoricalFixture(
                    fixtureId = 1,
                    homeTeamId = 100,
                    awayTeamId = 300,
                    homeTeamName = "Test Home Team",
                    awayTeamName = "Team C",
                    homeGoals = 2,
                    awayGoals = 1,
                    date = "2025-12-01T20:00:00+00:00",
                    leagueId = 10,
                    leagueName = "Test League",
                    season = 2025,
                    status = "FT"
                ),
                HistoricalFixture(
                    fixtureId = 2,
                    homeTeamId = 200,
                    awayTeamId = 100,
                    homeTeamName = "Team B",
                    awayTeamName = "Test Home Team",
                    homeGoals = 0,
                    awayGoals = 2,
                    date = "2025-12-08T20:00:00+00:00",
                    leagueId = 10,
                    leagueName = "Test League",
                    season = 2025,
                    status = "FT"
                )
            ),
            awayTeamFixtures = listOf(
                HistoricalFixture(
                    fixtureId = 3,
                    homeTeamId = 200,
                    awayTeamId = 400,
                    homeTeamName = "Test Away Team",
                    awayTeamName = "Team D",
                    homeGoals = 3,
                    awayGoals = 1,
                    date = "2025-12-01T20:00:00+00:00",
                    leagueId = 10,
                    leagueName = "Test League",
                    season = 2025,
                    status = "FT"
                )
            ),
            leagueFixtures = listOf(
                HistoricalFixture(
                    fixtureId = 1,
                    homeTeamId = 100,
                    awayTeamId = 300,
                    homeTeamName = "Test Home Team",
                    awayTeamName = "Team C",
                    homeGoals = 2,
                    awayGoals = 1,
                    date = "2025-12-01T20:00:00+00:00",
                    leagueId = 10,
                    leagueName = "Test League",
                    season = 2025,
                    status = "FT"
                ),
                HistoricalFixture(
                    fixtureId = 2,
                    homeTeamId = 200,
                    awayTeamId = 100,
                    homeTeamName = "Team B",
                    awayTeamName = "Test Home Team",
                    homeGoals = 0,
                    awayGoals = 2,
                    date = "2025-12-08T20:00:00+00:00",
                    leagueId = 10,
                    leagueName = "Test League",
                    season = 2025,
                    status = "FT"
                ),
                HistoricalFixture(
                    fixtureId = 3,
                    homeTeamId = 200,
                    awayTeamId = 400,
                    homeTeamName = "Test Away Team",
                    awayTeamName = "Team D",
                    homeGoals = 3,
                    awayGoals = 1,
                    date = "2025-12-01T20:00:00+00:00",
                    leagueId = 10,
                    leagueName = "Test League",
                    season = 2025,
                    status = "FT"
                )
            )
        )
    }

    @Test
    fun `loadHybridPrediction should handle repository failure gracefully`() = testScope.runTest {
        // Given
        whenever(mockRepository.getHistoricalFixturesForPrediction(
            homeTeamId = 100,
            awayTeamId = 200,
            leagueId = 10,
            season = Calendar.getInstance().get(Calendar.YEAR)
        )).thenReturn(Result.failure(Exception("Network error")))

        // When
        viewModel.loadHybridPrediction(testMatchDetail)
        advanceUntilIdle()

        // Then - Should set error state and not crash
        assert(viewModel.hybridError.value?.contains("Kon historische data niet ophalen") == true)
        assert(viewModel.isHybridLoading().not())
        assert(viewModel.hybridPrediction.value == null)
    }

    @Test
    fun `loadHybridPrediction should pass correct data sizes to EnhancedScorePredictor`() = testScope.runTest {
        // Given
        val mockEnhancedPrediction = mock<com.Lyno.matchmindai.domain.model.EnhancedPrediction>()
        whenever(mockUseCase.invoke(
            fixtureId = testFixtureId,
            matchDetail = testMatchDetail,
            homeTeamFixtures = any(),
            awayTeamFixtures = any(),
            leagueFixtures = any()
        )).thenReturn(Result.success(mockEnhancedPrediction))

        // When
        viewModel.loadHybridPrediction(testMatchDetail)
        advanceUntilIdle()

        // Then - Verify the data sizes are correct
        // Home team fixtures: 2 fixtures (fixture 1 and 2)
        // Away team fixtures: 1 fixture (fixture 3)
        // League fixtures: 3 fixtures total
        verify(mockUseCase).invoke(
            fixtureId = testFixtureId,
            matchDetail = testMatchDetail,
            homeTeamFixtures = any { it.size == 2 },
            awayTeamFixtures = any { it.size == 1 },
            leagueFixtures = any { it.size == 3 }
        )
    }

    @Test
    fun `loadHybridPrediction should log data sizes for debugging`() = testScope.runTest {
        // Given
        val mockEnhancedPrediction = mock<com.Lyno.matchmindai.domain.model.EnhancedPrediction>()
        whenever(mockUseCase.invoke(
            fixtureId = testFixtureId,
            matchDetail = testMatchDetail,
            homeTeamFixtures = any(),
            awayTeamFixtures = any(),
            leagueFixtures = any()
        )).thenReturn(Result.success(mockEnhancedPrediction))

        // When
        viewModel.loadHybridPrediction(testMatchDetail)
        advanceUntilIdle()

        // Then - The logging should happen in the ViewModel
        // We can't directly test logging, but we can verify the data flow
        // The important thing is that the data is passed correctly
        verify(mockRepository).getHistoricalFixturesForPrediction(
            homeTeamId = 100,
            awayTeamId = 200,
            leagueId = 10,
            season = Calendar.getInstance().get(Calendar.YEAR)
        )
    }
}

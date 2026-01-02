package com.Lyno.matchmindai.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.Lyno.matchmindai.MatchMindApplication
import com.Lyno.matchmindai.domain.repository.ChatRepository
import com.Lyno.matchmindai.domain.repository.MatchRepository
import com.Lyno.matchmindai.domain.repository.SettingsRepository
import com.Lyno.matchmindai.domain.service.MatchCuratorService
import com.Lyno.matchmindai.domain.usecase.GetApiKeyUseCase
import com.Lyno.matchmindai.domain.usecase.GetPredictionUseCase
import com.Lyno.matchmindai.domain.usecase.GetTodaysMatchesUseCase
import com.Lyno.matchmindai.domain.usecase.GetUpcomingMatchesUseCase
import com.Lyno.matchmindai.domain.usecase.SaveApiKeyUseCase
import com.Lyno.matchmindai.presentation.viewmodel.ChatViewModel
import com.Lyno.matchmindai.presentation.viewmodel.DashboardViewModel
import com.Lyno.matchmindai.presentation.viewmodel.FavoritesViewModel
import com.Lyno.matchmindai.presentation.viewmodel.HistoryDetailViewModel
import com.Lyno.matchmindai.presentation.viewmodel.KaptigunViewModel
import com.Lyno.matchmindai.presentation.viewmodel.MatchViewModel
import com.Lyno.matchmindai.presentation.viewmodel.MatchesViewModel
import com.Lyno.matchmindai.presentation.viewmodel.SettingsViewModel
import com.Lyno.matchmindai.presentation.viewmodel.MatchDetailViewModel
import com.Lyno.matchmindai.presentation.viewmodel.PredictionViewModel
import com.Lyno.matchmindai.presentation.viewmodel.PredictionHistoryViewModel
import com.Lyno.matchmindai.presentation.viewmodel.TeamSelectionViewModel

/**
 * Modern ViewModel provider using the viewModelFactory DSL.
 * This provides a cleaner, more type-safe way to create ViewModels with dependencies.
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for MatchViewModel
        initializer {
            val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MatchMindApplication)
            val container = application.appContainer
            
            // Create use cases with dependencies from AppContainer
            val getPredictionUseCase = GetPredictionUseCase(container.matchRepository)
            val getTodaysMatchesUseCase = GetTodaysMatchesUseCase(container.matchRepository)
            val getUpcomingMatchesUseCase = GetUpcomingMatchesUseCase(container.matchRepository)
            
            MatchViewModel(
                getPredictionUseCase = getPredictionUseCase,
                getTodaysMatchesUseCase = getTodaysMatchesUseCase,
                getUpcomingMatchesUseCase = getUpcomingMatchesUseCase,
                matchRepository = container.matchRepository,
                chatRepository = container.chatRepository
            )
        }
        
        // Initializer for SettingsViewModel
        initializer {
            val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MatchMindApplication)
            val container = application.appContainer
            
            // Create use cases with dependencies from AppContainer
            val saveApiKeyUseCase = SaveApiKeyUseCase(container.settingsRepository)
            
            SettingsViewModel(
                saveApiKeyUseCase = saveApiKeyUseCase,
                settingsRepository = container.settingsRepository
            )
        }
        
        // Initializer for ChatViewModel
        initializer {
            val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MatchMindApplication)
            val container = application.appContainer
            
            ChatViewModel(
                matchRepository = container.matchRepository,
                chatRepository = container.chatRepository
            )
        }
        
        // Initializer for DashboardViewModel
        initializer {
            val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MatchMindApplication)
            val container = application.appContainer
            
            // Create required dependency
            val matchCuratorService = MatchCuratorService()
            
            DashboardViewModel(
                matchRepository = container.matchRepository,
                favoritesManager = container.favoritesManager,
                matchCuratorService = matchCuratorService
            )
        }
        
        // Initializer for MatchesViewModel
        initializer {
            val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MatchMindApplication)
            val container = application.appContainer
            
            MatchesViewModel(
                matchRepository = container.matchRepository
            )
        }
        
        // Initializer for FavoritesViewModel
        initializer {
            val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MatchMindApplication)
            val container = application.appContainer
            
            FavoritesViewModel(
                matchRepository = container.matchRepository,
                settingsRepository = container.settingsRepository
            )
        }
        
        // Initializer for PredictionViewModel
        initializer {
            val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MatchMindApplication)
            val container = application.appContainer
            
            PredictionViewModel(
                oracleRepository = container.oracleRepository,
                getOraclePredictionUseCase = container.getOraclePredictionUseCase,
                newsImpactAnalyzer = container.newsImpactAnalyzer,
                getHybridPredictionUseCase = container.getHybridPredictionUseCase,
                apiKeyStorage = container.apiKeyStorage
            )
        }
        
        // Initializer for PredictionHistoryViewModel
        initializer<PredictionHistoryViewModel> {
            val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MatchMindApplication)
            val container = application.appContainer
            
            PredictionHistoryViewModel(
                oracleRepository = container.oracleRepository,
                predictionDao = container.predictionDao
            )
        }
        
        // Initializer for KaptigunViewModel
        initializer {
            val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MatchMindApplication)
            val container = application.appContainer
            
            KaptigunViewModel(
                kaptigunRepository = container.kaptigunRepository
            )
        }
        
        // Initializer for HistoryDetailViewModel
        initializer<HistoryDetailViewModel> {
            val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MatchMindApplication)
            val container = application.appContainer
            
            HistoryDetailViewModel(
                predictionDao = container.predictionDao,
                matchRepository = container.matchRepository,
                kaptigunRepository = container.kaptigunRepository
            )
        }
        
        // Note: MatchDetailViewModel is created directly in MatchDetailScreen with custom factory
        // because it requires fixtureId parameter which is not available in this context
        
        // Initializer for TeamSelectionViewModel
        initializer {
            val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MatchMindApplication)
            val container = application.appContainer
            
            TeamSelectionViewModel(
                searchTeamsUseCase = container.searchTeamsUseCase,
                settingsRepository = container.settingsRepository
            )
        }
        
        // Note: HistoryDetailViewModel is created with custom factory because it requires
        // predictionId and fixtureId parameters which are not available in this context
    }
    
    // Separate factory for FavoritesViewModel (used by AppContainer)
    val FavoritesFactory = viewModelFactory {
        initializer {
            val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MatchMindApplication)
            val container = application.appContainer
            
            FavoritesViewModel(
                matchRepository = container.matchRepository,
                settingsRepository = container.settingsRepository
            )
        }
    }
    
    // Separate factory for TeamSelectionViewModel (used by AppContainer)
    val TeamSelectionFactory = viewModelFactory {
        initializer {
            val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MatchMindApplication)
            val container = application.appContainer
            
            TeamSelectionViewModel(
                searchTeamsUseCase = container.searchTeamsUseCase,
                settingsRepository = container.settingsRepository
            )
        }
    }
    
    /**
     * Factory for MatchDetailViewModel with fixtureId parameter.
     * This is used by MatchDetailScreen to create the ViewModel with proper dependencies.
     */
    class MatchDetailFactory(
        private val fixtureId: Int,
        private val application: MatchMindApplication
    ) : ViewModelProvider.Factory {
        
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val container = application.appContainer
            
            return MatchDetailViewModel(
                fixtureId = fixtureId,
                matchRepository = container.matchRepository,
                matchCacheManager = container.matchCacheManager,
                getHybridPredictionUseCase = container.getHybridPredictionUseCase,
                matchReportGenerator = container.matchReportGenerator,
                mastermindAnalysisUseCase = container.mastermindAnalysisUseCase
            ) as T
        }
    }
    
    /**
     * Factory for HistoryDetailViewModel with predictionId and fixtureId parameters.
     * This is used by HistoryDetailScreen to create the ViewModel with proper dependencies.
     */
    class HistoryDetailFactory(
        private val predictionId: Int,
        private val fixtureId: Int,
        private val application: MatchMindApplication
    ) : ViewModelProvider.Factory {
        
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val container = application.appContainer
            
            return HistoryDetailViewModel(
                predictionDao = container.predictionDao,
                matchRepository = container.matchRepository,
                kaptigunRepository = container.kaptigunRepository
            ) as T
        }
    }
}

/**
 * Helper function to get the ViewModel factory from the application context.
 * This can be used as a drop-in replacement for the old getViewModelFactory function.
 */
fun getViewModelFactory(application: MatchMindApplication): ViewModelProvider.Factory {
    return AppViewModelProvider.Factory
}

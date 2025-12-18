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
import com.Lyno.matchmindai.presentation.viewmodel.MatchViewModel
import com.Lyno.matchmindai.presentation.viewmodel.MatchesViewModel
import com.Lyno.matchmindai.presentation.viewmodel.SettingsViewModel
import com.Lyno.matchmindai.presentation.viewmodel.MatchDetailViewModel

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
                settingsRepository = container.settingsRepository,
                matchRepository = container.matchRepository
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
        
        // Note: MatchDetailViewModel is created directly in MatchDetailScreen with custom factory
        // because it requires fixtureId parameter which is not available in this context
    }
}

/**
 * Helper function to get the ViewModel factory from the application context.
 * This can be used as a drop-in replacement for the old getViewModelFactory function.
 */
fun getViewModelFactory(application: MatchMindApplication): ViewModelProvider.Factory {
    return AppViewModelProvider.Factory
}

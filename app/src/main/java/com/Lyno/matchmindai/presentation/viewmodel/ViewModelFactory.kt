package com.Lyno.matchmindai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.Lyno.matchmindai.MatchMindApplication
import com.Lyno.matchmindai.domain.usecase.GetPredictionUseCase
import com.Lyno.matchmindai.domain.usecase.SaveApiKeyUseCase

/**
 * Factory for creating ViewModels with dependencies from the AppContainer.
 */
class ViewModelFactory(
    private val saveApiKeyUseCase: SaveApiKeyUseCase,
    private val getPredictionUseCase: GetPredictionUseCase,
    private val settingsRepository: com.Lyno.matchmindai.domain.repository.SettingsRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(saveApiKeyUseCase, settingsRepository) as T
            }
            modelClass.isAssignableFrom(MatchViewModel::class.java) -> {
                MatchViewModel(getPredictionUseCase) as T
            }
            else -> {
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}

/**
 * Helper function to get a ViewModelFactory from the application context.
 */
fun getViewModelFactory(application: MatchMindApplication): ViewModelFactory {
    val appContainer = application.appContainer
    
    // Create use cases with dependencies from AppContainer
    val saveApiKeyUseCase = SaveApiKeyUseCase(appContainer.settingsRepository)
    val getPredictionUseCase = GetPredictionUseCase(appContainer.matchRepository)
    
    return ViewModelFactory(saveApiKeyUseCase, getPredictionUseCase, appContainer.settingsRepository)
}

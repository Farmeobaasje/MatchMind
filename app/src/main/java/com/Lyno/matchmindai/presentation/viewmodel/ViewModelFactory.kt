package com.Lyno.matchmindai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.Lyno.matchmindai.MatchMindApplication
import com.Lyno.matchmindai.presentation.AppViewModelProvider

/**
 * Factory for creating ViewModels with dependencies from the AppContainer.
 * 
 * @deprecated Use AppViewModelProvider.Factory instead
 */
@Deprecated("Use AppViewModelProvider.Factory instead", replaceWith = ReplaceWith("AppViewModelProvider.Factory"))
class ViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        throw IllegalStateException("ViewModelFactory is deprecated. Use AppViewModelProvider.Factory instead")
    }
}

/**
 * Helper function to get a ViewModelFactory from the application context.
 */
fun getViewModelFactory(application: MatchMindApplication): ViewModelProvider.Factory {
    // Return the new AppViewModelProvider factory
    return AppViewModelProvider.Factory
}

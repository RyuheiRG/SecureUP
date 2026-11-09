package com.secureup.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.secureup.app.data.repository.SessionManager
import com.secureup.app.di.AppGraph
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ProfileUiState(
    val email: String = "",
    val versionName: String = "",
)

/**
 * ViewModel del perfil: expone el usuario de la sesión en memoria y permite
 * cerrar sesión. No hay backend todavía, así que no se edita ningún dato.
 */
class ProfileViewModel(
    private val sessionManager: SessionManager,
    private val versionName: String,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = sessionManager.currentUser
        .map { user ->
            ProfileUiState(
                email = user?.email.orEmpty(),
                versionName = versionName,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProfileUiState(
                email = sessionManager.currentUser.value?.email.orEmpty(),
                versionName = versionName,
            ),
        )

    fun logout() {
        sessionManager.signOut()
    }

    class Factory(
        private val sessionManager: SessionManager = AppGraph.container.sessionManager,
        private val versionName: String = AppGraph.appVersionName,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(sessionManager, versionName) as T
        }
    }
}

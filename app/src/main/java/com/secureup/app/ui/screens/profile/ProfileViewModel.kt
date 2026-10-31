package com.secureup.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secureup.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel que gestiona la lógica del perfil de usuario y las acciones de edición y cierre de sesión.
 */
class ProfileViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.getUser().collect { user ->
                _uiState.update { currentState ->
                    currentState.copy(
                        user = user,
                        nameInput = if (!currentState.isEditingName) user.name else currentState.nameInput,
                    )
                }
            }
        }
    }

    fun onToggleEditName() {
        _uiState.update {
            val newEditing = !it.isEditingName
            it.copy(
                isEditingName = newEditing,
                nameInput = if (newEditing) it.user.name else "",
                nameError = null,
            )
        }
    }

    fun onNameInputChange(name: String) {
        _uiState.update { it.copy(nameInput = name, nameError = null) }
    }

    fun onSaveName() {
        val currentInput = _uiState.value.nameInput.trim()
        if (currentInput.isEmpty()) {
            _uiState.update { it.copy(nameError = "El nombre no puede estar vacío.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingName = true, nameError = null) }
            val result = userRepository.updateName(currentInput)
            result.fold(
                onSuccess = { updatedUser ->
                    _uiState.update {
                        it.copy(
                            user = updatedUser,
                            isEditingName = false,
                            isSavingName = false,
                            userMessage = "Nombre actualizado correctamente.",
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSavingName = false,
                            nameError = error.message ?: "Error al actualizar el nombre.",
                        )
                    }
                },
            )
        }
    }

    fun onShowLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = true) }
    }

    fun onDismissLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = false) }
    }

    fun onConfirmLogout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true, showLogoutDialog = false) }
            val result = userRepository.logout()
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoggingOut = false) }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoggingOut = false,
                            userMessage = error.message ?: "Error al cerrar sesión.",
                        )
                    }
                },
            )
        }
    }

    fun onUserMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }
}

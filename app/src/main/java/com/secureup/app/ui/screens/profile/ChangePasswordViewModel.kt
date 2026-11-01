package com.secureup.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secureup.app.data.repository.UserRepository
import com.secureup.app.ui.components.calculatePasswordStrength
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel que gestiona la validación y el proceso de cambio de contraseña maestra.
 */
class ChangePasswordViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun onCurrentPasswordChange(value: String) {
        _uiState.update { it.copy(currentPassword = value, currentPasswordError = null, errorMessage = null) }
    }

    fun onNewPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                newPassword = value,
                newPasswordStrength = calculatePasswordStrength(value),
                newPasswordError = null,
                errorMessage = null,
            )
        }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null, errorMessage = null) }
    }

    fun onChangePassword(onSuccess: () -> Unit) {
        val state = _uiState.value
        var hasError = false

        var currentErr: String? = null
        var newErr: String? = null
        var confirmErr: String? = null

        if (state.currentPassword.isBlank()) {
            currentErr = "Ingresa la contraseña maestra actual."
            hasError = true
        }

        if (state.newPassword.isBlank()) {
            newErr = "Ingresa la nueva contraseña maestra."
            hasError = true
        } else if (state.newPassword.length < 8) {
            newErr = "La contraseña debe tener al menos 8 caracteres."
            hasError = true
        } else if (state.newPassword == state.currentPassword) {
            newErr = "La nueva contraseña debe ser diferente a la actual."
            hasError = true
        }

        if (state.confirmPassword.isBlank()) {
            confirmErr = "Confirma la nueva contraseña."
            hasError = true
        } else if (state.confirmPassword != state.newPassword) {
            confirmErr = "Las contraseñas no coinciden."
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    currentPasswordError = currentErr,
                    newPasswordError = newErr,
                    confirmPasswordError = confirmErr,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = userRepository.changeMasterPassword(
                currentPassword = state.currentPassword,
                newPassword = state.newPassword,
            )
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            currentPassword = "",
                            newPassword = "",
                            confirmPassword = "",
                            newPasswordStrength = calculatePasswordStrength(""),
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error al actualizar la contraseña.",
                        )
                    }
                },
            )
        }
    }

    fun onErrorMessageShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

package com.secureup.app.ui.screens.profile

import com.secureup.app.ui.components.PasswordStrength

/**
 * Estado de la interfaz de usuario para la pantalla de Cambiar contraseña maestra.
 */
data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val newPasswordStrength: PasswordStrength = PasswordStrength.None,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
)

package com.secureup.app.ui.screens.profile

import com.secureup.app.model.User

/**
 * Estado de la interfaz de usuario para la pantalla de Mi Perfil.
 */
data class ProfileUiState(
    val user: User = User(),
    val isEditingName: Boolean = false,
    val nameInput: String = "",
    val nameError: String? = null,
    val isSavingName: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val isLoggingOut: Boolean = false,
    val userMessage: String? = null,
)

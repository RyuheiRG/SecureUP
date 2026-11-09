package com.secureup.app.data.repository

import com.secureup.app.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Sesión activa en memoria.
 *
 * La contraseña maestra y las claves derivadas nunca se guardan aquí: solo el
 * usuario autenticado. Al cerrar sesión se limpia todo el estado en RAM.
 */
class SessionManager {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    val isAuthenticated: Boolean
        get() = _currentUser.value != null

    fun signIn(user: User) {
        _currentUser.value = user
    }

    fun signOut() {
        _currentUser.value = null
    }
}

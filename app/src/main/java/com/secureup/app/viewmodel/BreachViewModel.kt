package com.secureup.app.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.secureup.app.R
import com.secureup.app.di.AppGraph
import com.secureup.app.domain.repository.BreachFailureKind
import com.secureup.app.domain.repository.BreachRepository
import com.secureup.app.domain.repository.BreachResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Resultado visible del verificador de brechas.
 */
sealed interface BreachCheckOutcome {
    data object NotFound : BreachCheckOutcome
    data class Found(val occurrences: Long) : BreachCheckOutcome
}

data class BreachUiState(
    val password: String = "",
    val isLoading: Boolean = false,
    val outcome: BreachCheckOutcome? = null,
    @get:StringRes val errorRes: Int? = null,
)

/**
 * ViewModel del verificador de brechas.
 *
 * La contraseña solo vive en memoria mientras se escribe y se borra del estado
 * en cuanto termina la consulta, haya resultado o error.
 */
class BreachViewModel(
    private val breachRepository: BreachRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BreachUiState())
    val uiState: StateFlow<BreachUiState> = _uiState.asStateFlow()

    fun onPasswordChanged(password: String) {
        _uiState.update {
            it.copy(password = password, outcome = null, errorRes = null)
        }
    }

    fun checkPassword() {
        val current = _uiState.value
        if (current.password.isBlank()) {
            _uiState.update { it.copy(errorRes = R.string.breach_error_password_required) }
            return
        }
        if (current.isLoading) return

        _uiState.update {
            it.copy(isLoading = true, outcome = null, errorRes = null)
        }

        viewModelScope.launch(ioDispatcher) {
            val result = breachRepository.checkPassword(current.password)
            _uiState.update { state ->
                state.copy(
                    password = "",
                    isLoading = false,
                    outcome = when (result) {
                        BreachResult.NotFound -> BreachCheckOutcome.NotFound
                        is BreachResult.Found -> BreachCheckOutcome.Found(result.occurrences)
                        is BreachResult.Failure -> null
                    },
                    errorRes = when (result) {
                        is BreachResult.Failure -> result.kind.toMessageRes()
                        else -> null
                    },
                )
            }
        }
    }

    fun clear() {
        _uiState.value = BreachUiState()
    }

    private fun BreachFailureKind.toMessageRes(): Int = when (this) {
        BreachFailureKind.NETWORK -> R.string.breach_error_network
        BreachFailureKind.SERVER -> R.string.breach_error_server
        BreachFailureKind.INVALID_RESPONSE -> R.string.breach_error_invalid_response
    }

    class Factory(
        private val repository: BreachRepository = AppGraph.container.breachRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BreachViewModel(repository) as T
        }
    }
}

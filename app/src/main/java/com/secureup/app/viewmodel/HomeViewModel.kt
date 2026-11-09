package com.secureup.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.secureup.app.di.AppGraph
import com.secureup.app.domain.model.ScanVerdict
import com.secureup.app.domain.repository.ScanHistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val scanCount: Int = 0,
    val alerts: Int = 0,
    val securityScore: Int = 100,
    val lastScanUrl: String? = null,
    val lastScanVerdict: ScanVerdict? = null,
)

/**
 * ViewModel del Inicio. El score se calcula solo con datos reales del historial
 * de escaneos: cada URL sospechosa resta 10 puntos y cada peligrosa 30.
 *
 * La fórmula de la bóveda (fortaleza de contraseñas) queda pendiente para la
 * entrega 26.3.1, cuando exista la bóveda; hoy se documenta esta adaptación.
 */
class HomeViewModel(
    repository: ScanHistoryRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = repository.observeAll()
        .map { records ->
            val dangerous = records.count { it.verdict == ScanVerdict.DANGEROUS }
            val suspicious = records.count { it.verdict == ScanVerdict.SUSPICIOUS }
            HomeUiState(
                scanCount = records.size,
                alerts = dangerous + suspicious,
                securityScore = (100 - dangerous * 30 - suspicious * 10).coerceIn(0, 100),
                lastScanUrl = records.firstOrNull()?.url,
                lastScanVerdict = records.firstOrNull()?.verdict,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )

    class Factory(
        private val repository: ScanHistoryRepository = AppGraph.container.scanHistoryRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}

package com.secureup.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.secureup.app.R
import com.secureup.app.data.scanner.QrCodeReader
import com.secureup.app.di.AppGraph
import com.secureup.app.domain.model.ScanRecord
import com.secureup.app.domain.model.ScanVerdict
import com.secureup.app.domain.repository.ScanHistoryRepository
import com.secureup.app.domain.security.UrlAnalysis
import com.secureup.app.domain.security.UrlAnalyzer
import com.secureup.app.domain.security.UrlRiskReason
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ScannerUiState(
    val isAnalyzing: Boolean = false,
    val errorRes: Int? = null,
    /** URL ya analizada y guardada en el historial; dispara la navegación. */
    val detectedUrl: String? = null,
)

/**
 * ViewModel del escáner QR.
 *
 * Analiza la URL en el dispositivo (`UrlAnalyzer`) y guarda el resultado en Room.
 * No se abre ninguna URL desde aquí: la decisión es de la pantalla de resultado.
 */
class ScannerViewModel(
    private val scanHistoryRepository: ScanHistoryRepository,
    private val qrCodeReader: QrCodeReader,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    fun onQrDetected(rawValue: String) {
        analyze(rawValue)
    }

    fun onImageSelected(uri: Uri) {
        if (_uiState.value.isAnalyzing) return
        _uiState.update { it.copy(isAnalyzing = true, errorRes = null) }
        viewModelScope.launch(ioDispatcher) {
            val rawValue = qrCodeReader.read(uri)
            if (rawValue.isNullOrBlank()) {
                _uiState.update {
                    it.copy(isAnalyzing = false, errorRes = R.string.scanner_error_no_qr)
                }
                return@launch
            }
            analyzeInternal(rawValue)
        }
    }

    private fun analyze(rawValue: String) {
        if (_uiState.value.isAnalyzing || _uiState.value.detectedUrl != null) return
        _uiState.update { it.copy(isAnalyzing = true, errorRes = null) }
        viewModelScope.launch(ioDispatcher) {
            analyzeInternal(rawValue)
        }
    }

    private suspend fun analyzeInternal(rawValue: String) {
        val analysis = withContext(Dispatchers.Default) { UrlAnalyzer.analyze(rawValue) }
        saveToHistory(analysis)
        _uiState.update {
            it.copy(isAnalyzing = false, detectedUrl = analysis.url)
        }
    }

    private suspend fun saveToHistory(analysis: UrlAnalysis) {
        runCatching {
            scanHistoryRepository.add(
                ScanRecord(
                    url = analysis.url,
                    verdict = analysis.verdict,
                    scannedAt = System.currentTimeMillis(),
                )
            )
        }
        // Un fallo al guardar no debe impedir mostrar el resultado al usuario.
    }

    /** Tras navegar hay que limpiar la URL para no volver a navegar al recomponer. */
    fun consumeDetection() {
        _uiState.update { it.copy(detectedUrl = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorRes = null) }
    }

    class Factory(
        private val repository: ScanHistoryRepository = AppGraph.container.scanHistoryRepository,
        private val reader: QrCodeReader = AppGraph.container.qrCodeReader,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ScannerViewModel(repository, reader) as T
        }
    }
}

/**
 * Veredicto calculado para una URL concreta (pantalla de resultado).
 */
data class ScanResultUiState(
    val url: String,
    val verdict: ScanVerdict,
    val reasons: List<UrlRiskReason>,
)

/**
 * ViewModel de la pantalla de resultado. El análisis es determinista, así que
 * se recalcula a partir de la URL recibida por navegación.
 */
class ScanResultViewModel(url: String) : ViewModel() {

    private val analysis = UrlAnalyzer.analyze(url)

    val uiState: ScanResultUiState = ScanResultUiState(
        url = analysis.url,
        verdict = analysis.verdict,
        reasons = analysis.reasons,
    )

    companion object {
        fun factory(url: String): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ScanResultViewModel(url) as T
            }
        }
    }
}

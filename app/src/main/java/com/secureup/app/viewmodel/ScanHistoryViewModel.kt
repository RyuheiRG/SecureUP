package com.secureup.app.viewmodel

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.secureup.app.R
import com.secureup.app.data.export.ScanHistoryPdfWriter
import com.secureup.app.di.AppGraph
import com.secureup.app.domain.model.ScanRecord
import com.secureup.app.domain.repository.ScanHistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScanHistoryUiState(
    val items: List<ScanRecord> = emptyList(),
    val isLoading: Boolean = true,
)

/**
 * Estado de la exportación a PDF.
 */
sealed interface PdfExportState {
    data object Idle : PdfExportState
    data object Working : PdfExportState
    data object Done : PdfExportState
    data class Error(@param:StringRes val messageRes: Int) : PdfExportState
}

class ScanHistoryViewModel(
    private val repository: ScanHistoryRepository,
    private val pdfWriter: ScanHistoryPdfWriter,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    val uiState: StateFlow<ScanHistoryUiState> = repository.observeAll()
        .map { ScanHistoryUiState(items = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ScanHistoryUiState(),
        )

    private val _exportState = MutableStateFlow<PdfExportState>(PdfExportState.Idle)
    val exportState: StateFlow<PdfExportState> = _exportState.asStateFlow()

    fun exportPdf(uri: Uri) {
        if (_exportState.value == PdfExportState.Working) return
        val records = uiState.value.items
        _exportState.value = PdfExportState.Working
        viewModelScope.launch(ioDispatcher) {
            val ok = pdfWriter.write(uri, records)
            _exportState.value = if (ok) {
                PdfExportState.Done
            } else {
                PdfExportState.Error(R.string.history_export_error)
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch(ioDispatcher) {
            repository.clear()
        }
    }

    fun consumeExportState() {
        _exportState.update {
            if (it == PdfExportState.Working) it else PdfExportState.Idle
        }
    }

    class Factory(
        private val repository: ScanHistoryRepository = AppGraph.container.scanHistoryRepository,
        private val pdfWriter: ScanHistoryPdfWriter = AppGraph.container.scanHistoryPdfWriter,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ScanHistoryViewModel(repository, pdfWriter) as T
        }
    }
}

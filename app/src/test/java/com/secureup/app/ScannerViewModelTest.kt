package com.secureup.app

import com.secureup.app.domain.model.ScanRecord
import com.secureup.app.domain.repository.ScanHistoryRepository
import com.secureup.app.viewmodel.ScannerViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Regresión: un solo escaneo guardaba varias filas en el historial porque la
 * cámara entrega el mismo QR en fotogramas consecutivos, incluso después de
 * navegar al resultado.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ScannerViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    private val repository = RecordingScanHistoryRepository()
    private var nowMillis = 0L

    private val viewModel = ScannerViewModel(
        scanHistoryRepository = repository,
        qrImageReader = { null },
        ioDispatcher = dispatcher,
        analysisDispatcher = dispatcher,
        nowMillis = { nowMillis },
    )

    @Test
    fun duplicateDetectionsWhileAnalyzing_saveOnlyOnce() = runTest(dispatcher) {
        viewModel.onQrDetected(URL)
        viewModel.onQrDetected(URL)
        advanceUntilIdle()

        assertEquals(1, repository.records.size)
    }

    @Test
    fun sameQrRightAfterDetectionIsConsumed_doesNotSaveAgain() = runTest(dispatcher) {
        viewModel.onQrDetected(URL)
        advanceUntilIdle()
        viewModel.consumeDetection()

        // Fotogramas extra durante la transición a la pantalla de resultado.
        viewModel.onQrDetected(URL)
        advanceUntilIdle()

        assertEquals(1, repository.records.size)
    }

    @Test
    fun differentQrAfterDetectionIsConsumed_savesAgain() = runTest(dispatcher) {
        viewModel.onQrDetected(URL)
        advanceUntilIdle()
        viewModel.consumeDetection()

        viewModel.onQrDetected("https://otro.example")
        advanceUntilIdle()

        assertEquals(2, repository.records.size)
    }

    @Test
    fun sameQrAfterDedupeWindow_savesAgain() = runTest(dispatcher) {
        viewModel.onQrDetected(URL)
        advanceUntilIdle()
        viewModel.consumeDetection()

        nowMillis += 2_000
        viewModel.onQrDetected(URL)
        advanceUntilIdle()

        assertEquals(2, repository.records.size)
    }

    private class RecordingScanHistoryRepository : ScanHistoryRepository {
        val records = mutableListOf<ScanRecord>()

        override fun observeAll(): Flow<List<ScanRecord>> = flowOf(records)

        override suspend fun add(record: ScanRecord): Long {
            records += record
            return records.size.toLong()
        }

        override suspend fun clear() {
            records.clear()
        }
    }

    private companion object {
        const val URL = "https://example.com"
    }
}

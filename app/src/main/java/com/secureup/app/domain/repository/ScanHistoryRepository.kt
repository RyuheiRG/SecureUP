package com.secureup.app.domain.repository

import com.secureup.app.domain.model.ScanRecord
import kotlinx.coroutines.flow.Flow

/**
 * Historial de escaneos QR. La caché local es la única fuente de verdad:
 * el historial no se sincroniza con ningún servidor.
 */
interface ScanHistoryRepository {
    fun observeAll(): Flow<List<ScanRecord>>
    suspend fun add(record: ScanRecord): Long
    suspend fun clear()
}

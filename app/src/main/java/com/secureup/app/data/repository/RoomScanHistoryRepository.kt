package com.secureup.app.data.repository

import com.secureup.app.data.local.ScanHistoryDao
import com.secureup.app.data.local.ScanHistoryEntity
import com.secureup.app.domain.model.ScanRecord
import com.secureup.app.domain.model.ScanVerdict
import com.secureup.app.domain.repository.ScanHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementación de Room del historial de escaneos.
 */
class RoomScanHistoryRepository(
    private val dao: ScanHistoryDao,
) : ScanHistoryRepository {

    override fun observeAll(): Flow<List<ScanRecord>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun add(record: ScanRecord): Long =
        dao.insert(record.toEntity())

    override suspend fun clear() = dao.clear()

    private fun ScanHistoryEntity.toDomain(): ScanRecord = ScanRecord(
        id = id,
        url = url,
        verdict = runCatching { ScanVerdict.valueOf(verdict) }.getOrDefault(ScanVerdict.SUSPICIOUS),
        scannedAt = scannedAt,
    )

    private fun ScanRecord.toEntity(): ScanHistoryEntity = ScanHistoryEntity(
        id = id,
        url = url,
        verdict = verdict.name,
        scannedAt = scannedAt,
    )
}

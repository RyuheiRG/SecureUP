package com.secureup.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Acceso al historial de escaneos. Todas las consultas observables usan Flow
 * para que la UI se actualice sola cuando cambia la tabla.
 */
@Dao
interface ScanHistoryDao {

    @Query("SELECT * FROM scan_history ORDER BY scannedAt DESC")
    fun observeAll(): Flow<List<ScanHistoryEntity>>

    @Insert
    suspend fun insert(entity: ScanHistoryEntity): Long

    @Query("DELETE FROM scan_history")
    suspend fun clear()
}

package com.secureup.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Fila del historial de escaneos QR.
 *
 * `verdict` se guarda como texto (`SAFE`, `SUSPICIOUS`, `DANGEROUS`) para que la
 * base siga siendo legible si mañana cambia el orden del enum.
 */
@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val verdict: String,
    val scannedAt: Long,
)

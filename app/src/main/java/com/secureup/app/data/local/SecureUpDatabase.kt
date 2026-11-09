package com.secureup.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos local de SecureUP.
 *
 * Solo contiene el historial de escaneos QR. La bóveda de credenciales llega en
 * la entrega 26.3.1; cuando se añada, tendrá su propia entidad y su migración.
 */
@Database(
    entities = [ScanHistoryEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class SecureUpDatabase : RoomDatabase() {

    abstract fun scanHistoryDao(): ScanHistoryDao

    companion object {
        private const val DATABASE_NAME = "secureup.db"

        @Volatile
        private var instance: SecureUpDatabase? = null

        fun getInstance(context: Context): SecureUpDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    SecureUpDatabase::class.java,
                    DATABASE_NAME,
                ).build().also { instance = it }
            }
        }
    }
}

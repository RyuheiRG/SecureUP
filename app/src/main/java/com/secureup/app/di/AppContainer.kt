package com.secureup.app.di

import android.content.Context
import com.secureup.app.data.export.ScanHistoryPdfWriter
import com.secureup.app.data.local.SecureUpDatabase
import com.secureup.app.data.remote.NetworkModule
import com.secureup.app.data.repository.FakeAuthRepository
import com.secureup.app.data.repository.HibpBreachRepository
import com.secureup.app.data.repository.RoomScanHistoryRepository
import com.secureup.app.data.repository.SessionManager
import com.secureup.app.data.scanner.QrCodeReader
import com.secureup.app.domain.repository.AuthRepository
import com.secureup.app.domain.repository.BreachRepository
import com.secureup.app.domain.repository.ScanHistoryRepository

/**
 * Contenedor de dependencias de la app (service locator).
 *
 * Se crea una sola vez en `SecureUpApplication` y los ViewModel reciben sus
 * dependencias por constructor. Así las pruebas pueden inyectar dobles sin
 * arrancar Android.
 */
class AppContainer(context: Context) {

    val sessionManager = SessionManager()

    val authRepository: AuthRepository = FakeAuthRepository()

    val breachRepository: BreachRepository =
        HibpBreachRepository(api = NetworkModule.createHibpApi())

    private val database = SecureUpDatabase.getInstance(context)

    val scanHistoryRepository: ScanHistoryRepository =
        RoomScanHistoryRepository(database.scanHistoryDao())

    val qrCodeReader = QrCodeReader(context)

    val scanHistoryPdfWriter = ScanHistoryPdfWriter(context)
}

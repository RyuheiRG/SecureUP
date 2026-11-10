package com.secureup.app.data.scanner

import android.net.Uri

/**
 * Lectura de un QR desde una imagen elegida por el usuario.
 *
 * Se define como interfaz para que el ViewModel no dependa de ML Kit en las
 * pruebas unitarias de JVM.
 */
fun interface QrImageReader {
    suspend fun read(uri: Uri): String?
}

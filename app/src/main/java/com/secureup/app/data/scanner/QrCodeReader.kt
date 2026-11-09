package com.secureup.app.data.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Lee un QR desde una imagen de la galería con ML Kit (procesamiento local).
 *
 * La imagen se reduce antes de analizarla para no agotar memoria con fotos de
 * cámara de alta resolución. El bitmap se recicla siempre al terminar.
 */
class QrCodeReader(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    private val scanner: BarcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    suspend fun read(uri: Uri): String? = withContext(ioDispatcher) {
        val bitmap = decodeSampledBitmap(uri) ?: return@withContext null
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val barcodes = Tasks.await(scanner.process(image))
            barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue
        } catch (e: Exception) {
            // Imagen ilegible, sin QR o fallo del detector: la UI muestra un
            // mensaje genérico y no se registra nada.
            null
        } finally {
            bitmap.recycle()
        }
    }

    private fun decodeSampledBitmap(uri: Uri): Bitmap? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            val options = BitmapFactory.Options().apply {
                inSampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight)
            }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
        } catch (e: IOException) {
            null
        } catch (e: SecurityException) {
            null
        }
    }

    private fun calculateSampleSize(width: Int, height: Int): Int {
        var sample = 1
        var largest = maxOf(width, height)
        while (largest / sample > MAX_IMAGE_DIMENSION) {
            sample *= 2
            largest /= 2
        }
        return sample
    }

    fun close() {
        scanner.close()
    }

    private companion object {
        const val MAX_IMAGE_DIMENSION = 1600
    }
}

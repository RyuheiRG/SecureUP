package com.secureup.app.domain.model

/**
 * Veredicto local de una URL leída desde un QR.
 *
 * No es una verificación contra listas de amenazas: se calcula en el dispositivo
 * con reglas heurísticas (ver `UrlAnalyzer`).
 */
enum class ScanVerdict {
    SAFE,
    SUSPICIOUS,
    DANGEROUS,
}

/**
 * Registro del historial de escaneos. Se guarda en Room solo con la URL y el
 * veredicto: nunca se guardan imágenes ni el contenido bruto del QR.
 */
data class ScanRecord(
    val id: Long = 0,
    val url: String,
    val verdict: ScanVerdict,
    val scannedAt: Long,
)

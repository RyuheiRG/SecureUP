package com.secureup.app.data.export

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.secureup.app.domain.model.ScanRecord
import com.secureup.app.domain.model.ScanVerdict
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Genera un PDF legible del historial de escaneos y lo escribe en el `Uri` que
 * devuelve el selector de documentos (SAF). No requiere permisos de almacenamiento.
 */
class ScanHistoryPdfWriter(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    suspend fun write(uri: Uri, records: List<ScanRecord>): Boolean = withContext(ioDispatcher) {
        val document = PdfDocument()
        try {
            if (records.isEmpty()) {
                drawPage(document, pageNumber = 1, records = emptyList(), startIndex = 0, hasMore = false)
            } else {
                var start = 0
                var pageNumber = 1
                while (start < records.size) {
                    val end = minOf(start + ROWS_PER_PAGE, records.size)
                    drawPage(
                        document = document,
                        pageNumber = pageNumber,
                        records = records.subList(start, end),
                        startIndex = start,
                        hasMore = end < records.size,
                    )
                    start = end
                    pageNumber++
                }
            }

            context.contentResolver.openOutputStream(uri)?.use { output ->
                document.writeTo(output)
            } ?: return@withContext false
            true
        } catch (e: IOException) {
            false
        } finally {
            document.close()
        }
    }

    private fun drawPage(
        document: PdfDocument,
        pageNumber: Int,
        records: List<ScanRecord>,
        startIndex: Int,
        hasMore: Boolean,
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        canvas.drawColor(Color.WHITE)

        canvas.drawText("SecureUP", MARGIN, 54f, titlePaint)
        canvas.drawText("Historial de escaneos QR", MARGIN, 80f, subtitlePaint)
        canvas.drawText(
            "Generado el ${generatedAtFormatter.format(Date())}",
            MARGIN,
            100f,
            metaPaint,
        )
        canvas.drawLine(MARGIN, 112f, PAGE_WIDTH - MARGIN, 112f, linePaint)

        var y = 140f
        if (records.isEmpty()) {
            canvas.drawText("Todavía no hay escaneos registrados.", MARGIN, y, bodyPaint)
        } else {
            records.forEachIndexed { index, record ->
                val number = startIndex + index + 1
                canvas.drawText("$number.", MARGIN, y, bodyPaint)
                canvas.drawText(verdictLabel(record.verdict), MARGIN + 24f, y, verdictPaint(record.verdict))
                canvas.drawText(
                    dateFormatter.format(Date(record.scannedAt)),
                    MARGIN + 100f,
                    y,
                    bodyPaint,
                )
                y += 16f
                val urlPaint = bodyPaint
                canvas.drawText(ellipsize(record.url, urlPaint, CONTENT_WIDTH), MARGIN + 24f, y, urlPaint)
                y += 24f
                if (y > PAGE_HEIGHT - MARGIN - 24f) return@forEachIndexed
            }
        }

        val footer = "Página $pageNumber"
        canvas.drawText(footer, PAGE_WIDTH - MARGIN - 60f, PAGE_HEIGHT - 24f, metaPaint)
        if (hasMore) {
            canvas.drawText("Continúa en la página siguiente", MARGIN, PAGE_HEIGHT - 24f, metaPaint)
        }
        document.finishPage(page)
    }

    private fun verdictLabel(verdict: ScanVerdict): String = when (verdict) {
        ScanVerdict.SAFE -> "SEGURO"
        ScanVerdict.SUSPICIOUS -> "SOSPECHOSO"
        ScanVerdict.DANGEROUS -> "PELIGROSO"
    }

    private fun verdictPaint(verdict: ScanVerdict): Paint = Paint().apply {
        isAntiAlias = true
        textSize = 9f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        color = when (verdict) {
            ScanVerdict.SAFE -> Color.rgb(21, 128, 84)
            ScanVerdict.SUSPICIOUS -> Color.rgb(161, 98, 7)
            ScanVerdict.DANGEROUS -> Color.rgb(185, 28, 28)
        }
    }

    private fun ellipsize(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var end = text.length
        while (end > 4 && paint.measureText(text.substring(0, end) + "…") > maxWidth) {
            end--
        }
        return text.substring(0, end) + "…"
    }

    private val titlePaint = Paint().apply {
        isAntiAlias = true
        textSize = 24f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        color = Color.rgb(15, 17, 21)
    }

    private val subtitlePaint = Paint().apply {
        isAntiAlias = true
        textSize = 14f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        color = Color.rgb(40, 46, 58)
    }

    private val bodyPaint = Paint().apply {
        isAntiAlias = true
        textSize = 10f
        color = Color.rgb(30, 34, 42)
    }

    private val metaPaint = Paint().apply {
        isAntiAlias = true
        textSize = 9f
        color = Color.rgb(110, 118, 132)
    }

    private val linePaint = Paint().apply {
        color = Color.rgb(220, 224, 230)
        strokeWidth = 1f
    }

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private val generatedAtFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    private companion object {
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842
        const val MARGIN = 40f
        const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN * 2
        const val ROWS_PER_PAGE = 15
    }
}

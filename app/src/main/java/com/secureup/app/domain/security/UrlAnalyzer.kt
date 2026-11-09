package com.secureup.app.domain.security

import com.secureup.app.domain.model.ScanVerdict
import java.net.URI

/**
 * Motivos por los que una URL puede ser riesgosa. La UI los traduce a texto;
 * el dominio no depende de recursos de Android.
 */
enum class UrlRiskReason {
    INVALID_URL,
    UNSUPPORTED_SCHEME,
    NO_TLS,
    IP_HOST,
    PUNYCODE,
    USER_INFO,
    SHORTENER,
    LONG_URL,
}

data class UrlAnalysis(
    val url: String,
    val verdict: ScanVerdict,
    val reasons: List<UrlRiskReason>,
)

/**
 * Análisis local de una URL leída de un QR. NO consulta ningún servicio de
 * reputación: aplica reglas heurísticas explicables y conservadoras.
 *
 * Reglas:
 * - Esquema distinto de http/https -> peligroso (incluye `javascript:`, `file:`…).
 * - Usuario/contraseña embebidos (`https://banco.com@malicioso.com`) -> peligroso.
 * - Sin TLS, host con IP literal, dominio punycode, acortador o URL muy larga
 *   -> sospechoso.
 * - Sin señales anteriores -> seguro (sin amenazas conocidas, no "verificado").
 */
object UrlAnalyzer {

    private const val MAX_REASONABLE_LENGTH = 150

    private val knownShorteners = setOf(
        "bit.ly", "tinyurl.com", "t.co", "goo.gl", "ow.ly", "is.gd",
        "buff.ly", "cutt.ly", "shorturl.at", "rb.gy", "rebrand.ly",
    )

    private val ipv4Regex = Regex("""^\d{1,3}(\.\d{1,3}){3}$""")

    fun analyze(raw: String): UrlAnalysis {
        val trimmed = raw.trim()
        val uri = runCatching { URI(trimmed) }.getOrNull()
            ?: return dangerous(trimmed, UrlRiskReason.INVALID_URL)

        val scheme = uri.scheme?.lowercase()
        if (scheme != "http" && scheme != "https") {
            return dangerous(trimmed, UrlRiskReason.UNSUPPORTED_SCHEME)
        }

        val host = uri.host?.lowercase()
        if (host.isNullOrBlank()) {
            return dangerous(trimmed, UrlRiskReason.INVALID_URL)
        }

        val reasons = mutableListOf<UrlRiskReason>()
        if (scheme == "http") reasons += UrlRiskReason.NO_TLS
        if (isIpLiteral(host)) reasons += UrlRiskReason.IP_HOST
        if (host.contains("xn--")) reasons += UrlRiskReason.PUNYCODE
        if (!uri.userInfo.isNullOrBlank()) reasons += UrlRiskReason.USER_INFO
        if (knownShorteners.any { host == it || host.endsWith(".$it") }) {
            reasons += UrlRiskReason.SHORTENER
        }
        if (trimmed.length > MAX_REASONABLE_LENGTH) reasons += UrlRiskReason.LONG_URL

        val verdict = when {
            UrlRiskReason.USER_INFO in reasons -> ScanVerdict.DANGEROUS
            reasons.isEmpty() -> ScanVerdict.SAFE
            else -> ScanVerdict.SUSPICIOUS
        }
        return UrlAnalysis(trimmed, verdict, reasons)
    }

    private fun isIpLiteral(host: String): Boolean =
        ipv4Regex.matches(host) || host.startsWith("[") || host.contains(':')

    private fun dangerous(url: String, reason: UrlRiskReason) = UrlAnalysis(
        url = url,
        verdict = ScanVerdict.DANGEROUS,
        reasons = listOf(reason),
    )
}

package com.secureup.app

import com.secureup.app.domain.model.ScanVerdict
import com.secureup.app.domain.security.UrlAnalyzer
import com.secureup.app.domain.security.UrlRiskReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Reglas del análisis local de URLs. No hay red: son funciones puras.
 */
class UrlAnalyzerTest {

    @Test
    fun httpsWithoutSignals_isSafe() {
        val result = UrlAnalyzer.analyze("https://www.secureup.app/login")
        assertEquals(ScanVerdict.SAFE, result.verdict)
        assertTrue(result.reasons.isEmpty())
    }

    @Test
    fun httpWithoutTls_isSuspicious() {
        val result = UrlAnalyzer.analyze("http://example.com")
        assertEquals(ScanVerdict.SUSPICIOUS, result.verdict)
        assertTrue(result.reasons.contains(UrlRiskReason.NO_TLS))
    }

    @Test
    fun ipHost_isSuspicious() {
        val result = UrlAnalyzer.analyze("https://192.168.1.10/login")
        assertEquals(ScanVerdict.SUSPICIOUS, result.verdict)
        assertTrue(result.reasons.contains(UrlRiskReason.IP_HOST))
    }

    @Test
    fun punycodeHost_isSuspicious() {
        val result = UrlAnalyzer.analyze("https://xn--bcher-kva.example")
        assertEquals(ScanVerdict.SUSPICIOUS, result.verdict)
        assertTrue(result.reasons.contains(UrlRiskReason.PUNYCODE))
    }

    @Test
    fun shortenerHost_isSuspicious() {
        val result = UrlAnalyzer.analyze("https://bit.ly/abc123")
        assertEquals(ScanVerdict.SUSPICIOUS, result.verdict)
        assertTrue(result.reasons.contains(UrlRiskReason.SHORTENER))
    }

    @Test
    fun userInfoBeforeHost_isDangerous() {
        val result = UrlAnalyzer.analyze("https://banco.com@malicioso.example/login")
        assertEquals(ScanVerdict.DANGEROUS, result.verdict)
        assertTrue(result.reasons.contains(UrlRiskReason.USER_INFO))
    }

    @Test
    fun javascriptScheme_isDangerous() {
        val result = UrlAnalyzer.analyze("javascript:alert(1)")
        assertEquals(ScanVerdict.DANGEROUS, result.verdict)
        assertTrue(result.reasons.contains(UrlRiskReason.UNSUPPORTED_SCHEME))
    }

    @Test
    fun fileScheme_isDangerous() {
        val result = UrlAnalyzer.analyze("file:///sdcard/secret.txt")
        assertEquals(ScanVerdict.DANGEROUS, result.verdict)
        assertTrue(result.reasons.contains(UrlRiskReason.UNSUPPORTED_SCHEME))
    }

    @Test
    fun plainText_isDangerous() {
        val result = UrlAnalyzer.analyze("esto no es una url")
        assertEquals(ScanVerdict.DANGEROUS, result.verdict)
        assertTrue(result.reasons.contains(UrlRiskReason.INVALID_URL))
    }

    @Test
    fun excessivelyLongUrl_isSuspicious() {
        val result = UrlAnalyzer.analyze("https://example.com/" + "a".repeat(200))
        assertEquals(ScanVerdict.SUSPICIOUS, result.verdict)
        assertTrue(result.reasons.contains(UrlRiskReason.LONG_URL))
    }

    @Test
    fun surroundingWhitespace_isTrimmed() {
        val result = UrlAnalyzer.analyze("  https://example.com  ")
        assertEquals("https://example.com", result.url)
        assertEquals(ScanVerdict.SAFE, result.verdict)
    }
}

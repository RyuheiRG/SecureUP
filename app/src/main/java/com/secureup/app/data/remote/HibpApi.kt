package com.secureup.app.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

/**
 * Cliente del endpoint Pwned Passwords de Have I Been Pwned (v3).
 *
 * Solo se envía el prefijo de 5 caracteres del SHA-1; el resto se compara en el
 * dispositivo (k-anonimato). `Add-Padding` hace que la respuesta incluya filas
 * de relleno con contador 0, para que el tamaño no delate el prefijo.
 */
interface HibpApi {

    @GET("range/{prefix}")
    @Headers(
        "Add-Padding: true",
        "User-Agent: SecureUP-Android",
    )
    suspend fun getRange(@Path("prefix") prefix: String): Response<String>
}

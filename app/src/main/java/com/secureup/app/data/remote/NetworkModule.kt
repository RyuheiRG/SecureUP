package com.secureup.app.data.remote

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

/**
 * Construye los clientes HTTP de la app.
 *
 * El backend propio todavía no existe; por ahora la única llamada remota es la
 * consulta de brechas a HIBP (sin clave de API y sin enviar la contraseña).
 */
object NetworkModule {

    private const val HIBP_BASE_URL = "https://api.pwnedpasswords.com/"

    fun createHibpApi(): HibpApi = Retrofit.Builder()
        .baseUrl(HIBP_BASE_URL)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
        .create(HibpApi::class.java)
}

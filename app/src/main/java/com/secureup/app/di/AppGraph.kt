package com.secureup.app.di

import android.content.Context
import com.secureup.app.BuildConfig

/**
 * Punto de acceso global al contenedor de dependencias.
 *
 * `init` se llama desde `SecureUpApplication.onCreate`, antes de que se cree
 * cualquier Activity y, por tanto, antes de instanciar cualquier ViewModel.
 */
object AppGraph {

    @Volatile
    private var appContainer: AppContainer? = null

    val container: AppContainer
        get() = appContainer
            ?: error("AppGraph no está inicializado: SecureUpApplication debe arrancar primero")

    val appVersionName: String
        get() = BuildConfig.VERSION_NAME

    fun init(context: Context) {
        if (appContainer == null) {
            synchronized(this) {
                if (appContainer == null) {
                    appContainer = AppContainer(context.applicationContext)
                }
            }
        }
    }
}

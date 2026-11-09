package com.secureup.app

import android.app.Application
import com.secureup.app.di.AppGraph

/**
 * Application de SecureUP: inicializa el contenedor de dependencias una sola vez
 * por proceso. El manifest la declara con `android:name=".SecureUpApplication"`.
 */
class SecureUpApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppGraph.init(this)
    }
}

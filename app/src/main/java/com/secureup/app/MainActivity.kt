package com.secureup.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.secureup.app.ui.screens.LoginScreen
import com.secureup.app.ui.theme.SecureUPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SecureUPTheme {
                LoginScreen()
            }
        }
    }
}
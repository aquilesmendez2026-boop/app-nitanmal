package com.nitanmal.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import com.nitanmal.app.auth.AndroidPlatformAuth
import com.nitanmal.app.core.config.AppContextHolder
import com.nitanmal.app.domain.auth.LocalPlatformAuth
import com.nitanmal.app.domain.util.LocalActivity

class MainActivity : ComponentActivity() {

    private lateinit var platformAuth: AndroidPlatformAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AppContextHolder.context = applicationContext
        platformAuth = AndroidPlatformAuth(this)

        platformAuth.signInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            platformAuth.handleSignInResult(result.data)
        }

        setContent {
            CompositionLocalProvider(
                LocalPlatformAuth provides platformAuth,
                LocalActivity provides this
            ) {
                App()
            }
        }
    }
}

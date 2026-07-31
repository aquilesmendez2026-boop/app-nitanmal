package com.nitanmal.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.content.ContextCompat
import com.nitanmal.app.auth.AndroidPlatformAuth
import com.nitanmal.app.core.config.AppContextHolder
import com.nitanmal.app.core.config.PermissionBridge
import com.nitanmal.app.domain.auth.LocalPlatformAuth
import com.nitanmal.app.domain.util.LocalActivity

class MainActivity : ComponentActivity() {

    private lateinit var platformAuth: AndroidPlatformAuth
    private var audioPermissionCallback: ((Boolean) -> Unit)? = null

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

        val audioPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            audioPermissionCallback?.invoke(granted)
            audioPermissionCallback = null
        }
        PermissionBridge.requestAudioPermission = { callback ->
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                callback(true)
            } else {
                audioPermissionCallback = callback
                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
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

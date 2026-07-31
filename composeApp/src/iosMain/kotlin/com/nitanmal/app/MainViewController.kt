package com.nitanmal.app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import com.nitanmal.app.auth.IosPlatformAuth
import com.nitanmal.app.domain.auth.LocalPlatformAuth

fun MainViewController() = ComposeUIViewController {
    CompositionLocalProvider(LocalPlatformAuth provides IosPlatformAuth()) {
        App()
    }
}

package com.nitanmal.app.core.config

import com.nitanmal.app.BuildConfig

actual object SecureConfig {
    actual val adminApiKey: String = BuildConfig.ADMIN_API_KEY
    actual val webClientId: String = BuildConfig.WEB_CLIENT_ID
}

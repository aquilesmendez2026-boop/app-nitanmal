package com.nitanmal.app.core.config

actual object SecureConfig {
    actual val adminApiKey: String
        get() = platform.Foundation.NSBundle.mainBundle.objectForInfoDictionaryKey("ADMIN_API_KEY") as? String ?: ""

    // TODO(nitanmal): reemplazar con el CLIENT_ID (iOS) del proyecto Firebase de nitanmal
    // (viene en GoogleService-Info.plist → CLIENT_ID)
    actual val webClientId: String
        get() = "REEMPLAZAR_IOS_CLIENT_ID.apps.googleusercontent.com"
}

package com.nitanmal.app.core.config

expect object SecureConfig {
    val adminApiKey: String
    val webClientId: String
}

package com.nitanmal.app.data.remote

import com.nitanmal.app.core.config.SecureConfig

object AuthConfig {
    const val AUTH_API_URL = "https://safe-api-auth-customers.umine.com/prod"

    // TODO(nitanmal): confirmar el core_key y client_key asignados a nitanmal
    // en umine-core-auth-customers. Valores actuales son provisionales.
    const val AUTH_CORE_ID = "smart-customers"
    const val CLIENT_KEY = "NITANMAL"
    const val ENVIRONMENT = "prod"

    val ADMIN_API_KEY: String get() = SecureConfig.adminApiKey
    val WEB_CLIENT_ID: String get() = SecureConfig.webClientId
}

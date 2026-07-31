package com.nitanmal.app.data.remote

import com.nitanmal.app.core.config.SecureConfig

object AuthConfig {
    // Backend propio de nitanmal (stack `nitalmal-backend`, cuenta AWS 970335222766).
    // El API Gateway valida el Firebase ID token (proyecto nitanmal-a75de) con un
    // JWT authorizer; GET /me crea/actualiza el usuario y devuelve su perfil.
    const val API_URL = "https://uhryf0x2jb.execute-api.us-east-2.amazonaws.com"

    val WEB_CLIENT_ID: String get() = SecureConfig.webClientId
}

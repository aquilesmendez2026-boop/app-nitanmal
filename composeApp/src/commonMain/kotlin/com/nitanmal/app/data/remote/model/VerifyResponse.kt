package com.nitanmal.app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class VerifyResponse(
    val ok: Boolean = false,
    val user_id: String? = null,
    val email: String? = null,
    val env: String? = null,
    val core_key: String? = null,
    val client_key: String? = null,
    val role: String? = null,
    val roles: List<RoleEntry>? = null,
    val message: String? = null
)

@Serializable
data class RoleEntry(
    val client_key: String,
    val role: String,
    val core_key: String
)

package com.nitanmal.app.data.remote.model

import kotlinx.serialization.Serializable

/** Respuesta de GET /me del backend de nitanmal: { "user": {...} } */
@Serializable
data class MeResponse(
    val user: ProfileDto? = null,
    val error: String? = null
)

@Serializable
data class ProfileDto(
    val userId: String? = null,
    val email: String? = null,
    val name: String? = null,
    val role: String? = null,
    val apodo: String? = null,
    val pais: String? = null,
    val region: String? = null,
    val telefono: String? = null,
    val avatarKey: String? = null,
    /** Foto propia (URL firmada de S3), si el usuario subió una. */
    val photoURL: String? = null,
    /** Plan de membresía: "free" | "premium". */
    val plan: String? = null,
    val premiumSince: String? = null
)

package com.nitanmal.app.domain.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val isAuthenticated: Boolean = false,
    /** Rol: "miembro" | "participante" | "admin" | "superadmin" */
    val role: String? = null,
    /** Plan de membresía: "free" | "premium" */
    val plan: String? = null,
    val apodo: String? = null,
    val pais: String? = null,
    val region: String? = null,
    val telefono: String? = null,
    val roles: List<UserRole> = emptyList(),
    val selectedClientKey: String? = null
) {
    /** Equipo del podcast (participante o superior). */
    val esEquipo: Boolean
        get() = role == "participante" || role == "admin" || role == "superadmin"

    val esPremium: Boolean
        get() = plan == "premium" || role == "admin" || role == "superadmin"
}

data class UserRole(
    val clientKey: String,
    val role: String,
    val coreKey: String
)

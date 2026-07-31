package com.nitanmal.app.domain.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val isAuthenticated: Boolean = false,
    val role: String? = null,
    val roles: List<UserRole> = emptyList(),
    val selectedClientKey: String? = null
)

data class UserRole(
    val clientKey: String,
    val role: String,
    val coreKey: String
)

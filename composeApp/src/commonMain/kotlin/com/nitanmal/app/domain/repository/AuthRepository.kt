package com.nitanmal.app.domain.repository

import com.nitanmal.app.domain.model.User

interface AuthRepository {
    suspend fun signInWithGoogle(): Result<User>

    /**
     * Restaura la sesión sin UI: si Firebase tiene un usuario cacheado,
     * pide GET /me y devuelve el User. null = no había sesión.
     */
    suspend fun restoreSession(): User?

    /** PUT /me: actualiza apodo/país/región/teléfono y devuelve el User. */
    suspend fun updateProfile(
        apodo: String?,
        pais: String?,
        region: String?,
        telefono: String?
    ): Result<User>

    suspend fun selectClient(clientKey: String): Result<User>
    suspend fun signOut(): Result<Unit>
    fun getCurrentUser(): User?
}

package com.nitanmal.app.domain.repository

import com.nitanmal.app.domain.model.User

interface AuthRepository {
    suspend fun signInWithGoogle(): Result<User>
    suspend fun selectClient(clientKey: String): Result<User>
    suspend fun signOut(): Result<Unit>
    fun getCurrentUser(): User?
}

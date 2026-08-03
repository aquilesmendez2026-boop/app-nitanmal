package com.nitanmal.app.domain.auth

import androidx.compose.runtime.staticCompositionLocalOf

sealed class GoogleSignInResult {
    data class Success(
        val firebaseIdToken: String,
        val userId: String,
        val email: String,
        val displayName: String?,
        val photoUrl: String?
    ) : GoogleSignInResult()

    object Canceled : GoogleSignInResult()

    data class Error(val message: String) : GoogleSignInResult()
}

/** Datos del usuario Firebase cacheado (para restaurar sesión sin UI). */
data class CachedUserInfo(
    val userId: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?
)

interface PlatformAuth {
    suspend fun signInWithGoogle(): Result<GoogleSignInResult>
    suspend fun getFirebaseIdToken(): String?
    suspend fun signOut()

    /** Usuario Firebase persistido entre sesiones (null si no hay). */
    fun getCachedUser(): CachedUserInfo? = null
}

val LocalPlatformAuth = staticCompositionLocalOf<PlatformAuth> {
    error("PlatformAuth not provided")
}

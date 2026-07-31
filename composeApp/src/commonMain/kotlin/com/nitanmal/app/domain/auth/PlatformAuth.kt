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

interface PlatformAuth {
    suspend fun signInWithGoogle(): Result<GoogleSignInResult>
    suspend fun getFirebaseIdToken(): String?
    suspend fun signOut()
}

val LocalPlatformAuth = staticCompositionLocalOf<PlatformAuth> {
    error("PlatformAuth not provided")
}

package com.nitanmal.app.auth

import com.nitanmal.app.domain.auth.GoogleSignInResult
import com.nitanmal.app.domain.auth.PlatformAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class IosPlatformAuth : PlatformAuth {

    override suspend fun signInWithGoogle(): Result<GoogleSignInResult> {
        val delegate = IosAuthBridge.delegate
            ?: return Result.failure(IllegalStateException("IosAuthDelegate not set. Ensure Firebase is configured in iOSApp.swift"))

        return suspendCancellableCoroutine { continuation ->
            delegate.signInWithGoogle { token, userId, email, displayName, photoUrl, error ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error)))
                } else if (token != null && userId != null && email != null) {
                    continuation.resume(
                        Result.success(
                            GoogleSignInResult.Success(
                                firebaseIdToken = token,
                                userId = userId,
                                email = email,
                                displayName = displayName,
                                photoUrl = photoUrl
                            )
                        )
                    )
                } else {
                    continuation.resume(Result.failure(Exception("Google Sign-In returned incomplete data")))
                }
            }
        }
    }

    override suspend fun getFirebaseIdToken(): String? {
        val delegate = IosAuthBridge.delegate ?: return null

        return suspendCancellableCoroutine { continuation ->
            delegate.getFirebaseIdToken { token ->
                continuation.resume(token)
            }
        }
    }

    override suspend fun signOut() {
        IosAuthBridge.delegate?.signOut()
    }
}

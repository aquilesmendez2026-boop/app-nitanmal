package com.nitanmal.app.auth

/**
 * Protocol/interface that Swift will implement to handle Firebase Auth operations.
 * This bridge allows Kotlin to call Firebase iOS SDK through Swift.
 */
interface IosAuthDelegate {
    fun signInWithGoogle(completion: (token: String?, userId: String?, email: String?, displayName: String?, photoUrl: String?, error: String?) -> Unit)
    fun getFirebaseIdToken(completion: (token: String?) -> Unit)
    fun signOut()
}

/**
 * Singleton bridge that holds the Swift-side auth delegate.
 * Swift sets the delegate at app startup, Kotlin calls it from IosPlatformAuth.
 */
object IosAuthBridge {
    var delegate: IosAuthDelegate? = null
}

package com.nitanmal.app.auth

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.nitanmal.app.data.remote.AuthConfig
import com.nitanmal.app.domain.auth.GoogleSignInResult
import com.nitanmal.app.domain.auth.PlatformAuth
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AndroidPlatformAuth(
    private val activity: ComponentActivity
) : PlatformAuth {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private var signInDeferred: CompletableDeferred<Result<GoogleSignInResult>>? = null

    lateinit var signInLauncher: ActivityResultLauncher<Intent>

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(AuthConfig.WEB_CLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()
        GoogleSignIn.getClient(activity, gso)
    }

    override suspend fun signInWithGoogle(): Result<GoogleSignInResult> {
        android.util.Log.i("NitanmalAuth", "signInWithGoogle() called")
        val deferred = CompletableDeferred<Result<GoogleSignInResult>>()
        signInDeferred = deferred
        try {
            // Force fresh Google sign-in: signOut() clears any cached credentials
            // that would otherwise produce a stale ID token → FirebaseNetworkException
            // after a previous signOut in the same process.
            runCatching { googleSignInClient.signOut().await() }
            val intent = googleSignInClient.signInIntent
            signInLauncher.launch(intent)
        } catch (e: Throwable) {
            android.util.Log.e("NitanmalAuth", "Exception BEFORE launching picker: ${e.javaClass.simpleName}: ${e.message}", e)
            deferred.complete(Result.failure(e))
        }
        return deferred.await()
    }

    fun handleSignInResult(data: Intent?) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                val googleIdToken = account.idToken
                    ?: throw Exception("No se obtuvo ID token de Google")

                val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                val firebaseUser = authResult.user
                    ?: throw Exception("No se obtuvo usuario de Firebase")
                // Use cached token — signInWithCredential just issued a fresh one.
                // Forcing refresh (true) adds an unnecessary round-trip to
                // securetoken.googleapis.com that fails intermittently on emulators
                // with a misleading "FirebaseNetworkException".
                val tokenResult = firebaseUser.getIdToken(false).await()

                val result = GoogleSignInResult.Success(
                    firebaseIdToken = tokenResult.token ?: "",
                    userId = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName,
                    photoUrl = firebaseUser.photoUrl?.toString()
                )
                signInDeferred?.complete(Result.success(result))
            } catch (e: ApiException) {
                // Handle Google Sign-In specific errors
                when (e.statusCode) {
                    12501 -> {
                        // User canceled - not an error, just return Canceled state
                        signInDeferred?.complete(Result.success(GoogleSignInResult.Canceled))
                    }
                    else -> {
                        // Real Google Sign-In error
                        signInDeferred?.complete(Result.success(GoogleSignInResult.Error("Google Sign-In failed: ${e.message}")))
                    }
                }
            } catch (e: Exception) {
                // Other exceptions (Firebase, network, etc.)
                signInDeferred?.complete(Result.success(GoogleSignInResult.Error("Authentication error: ${e.message}")))
            }
        }
    }

    override suspend fun getFirebaseIdToken(): String? {
        return try {
            firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun signOut() {
        try {
            firebaseAuth.signOut()
            googleSignInClient.signOut().await()
        } catch (_: Exception) { }
    }
}

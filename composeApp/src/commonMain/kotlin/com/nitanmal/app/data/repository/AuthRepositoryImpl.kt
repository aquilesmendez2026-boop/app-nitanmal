package com.nitanmal.app.data.repository

import com.nitanmal.app.data.remote.AuthApiService
import com.nitanmal.app.data.remote.IAuthApiService
import com.nitanmal.app.domain.auth.GoogleSignInResult
import com.nitanmal.app.domain.auth.PlatformAuth
import com.nitanmal.app.domain.model.User
import com.nitanmal.app.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val platformAuth: PlatformAuth,
    private val apiService: IAuthApiService = AuthApiService()
) : AuthRepository {
    private var currentUser: User? = null

    override suspend fun signInWithGoogle(): Result<User> {
        return try {
            // 1. Google Sign-In + Firebase Auth
            val signInResult = platformAuth.signInWithGoogle().getOrThrow()

            // Handle user cancellation
            when (signInResult) {
                is GoogleSignInResult.Canceled -> {
                    // User canceled - return failure but don't show as error
                    return Result.failure(Exception("USER_CANCELED"))
                }
                is GoogleSignInResult.Error -> {
                    // Real error - show message
                    return Result.failure(Exception(signInResult.message))
                }
                is GoogleSignInResult.Success -> {
                    // Continue with successful sign-in
                }
            }

            // 2. GET /me del backend de nitanmal: el JWT authorizer valida el token
            //    y el handler registra el login y devuelve el perfil (con rol).
            val meResponse = apiService.getMe(signInResult.firebaseIdToken)

            val profile = meResponse.user
                ?: return Result.failure(
                    Exception(meResponse.error ?: "El backend no devolvió el perfil")
                )

            val user = User(
                id = profile.userId ?: signInResult.userId,
                name = profile.name?.takeIf { it.isNotBlank() }
                    ?: signInResult.displayName ?: "",
                email = profile.email ?: signInResult.email,
                // Preferimos la foto subida al backend; si no hay, la de Google.
                photoUrl = profile.photoURL ?: signInResult.photoUrl,
                isAuthenticated = true,
                role = profile.role,
                plan = profile.plan
            )
            currentUser = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun selectClient(clientKey: String): Result<User> {
        // nitanmal es mono-portal: no hay selección de cliente.
        val user = currentUser ?: return Result.failure(Exception("No hay usuario autenticado"))
        return Result.success(user)
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            platformAuth.signOut()
            currentUser = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): User? = currentUser
}

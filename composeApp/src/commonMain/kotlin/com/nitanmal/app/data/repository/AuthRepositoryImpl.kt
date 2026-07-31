package com.nitanmal.app.data.repository

import com.nitanmal.app.data.remote.AuthApiService
import com.nitanmal.app.data.remote.AuthConfig
import com.nitanmal.app.data.remote.IAuthApiService
import com.nitanmal.app.domain.auth.GoogleSignInResult
import com.nitanmal.app.domain.auth.PlatformAuth
import com.nitanmal.app.domain.model.User
import com.nitanmal.app.domain.model.UserRole
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

            // 2. Call /verify API with Firebase ID token
            val verifyResponse = apiService.verify(
                firebaseIdToken = signInResult.firebaseIdToken,
                coreKey = AuthConfig.AUTH_CORE_ID,
                clientKey = AuthConfig.CLIENT_KEY,
                env = AuthConfig.ENVIRONMENT
            )

            if (!verifyResponse.ok) {
                return Result.failure(
                    Exception(verifyResponse.message ?: "Verificación fallida")
                )
            }

            val user = User(
                id = verifyResponse.user_id ?: signInResult.userId,
                name = signInResult.displayName ?: "",
                email = verifyResponse.email ?: signInResult.email,
                photoUrl = signInResult.photoUrl,
                isAuthenticated = true,
                role = verifyResponse.role,
                roles = verifyResponse.roles?.map {
                    UserRole(
                        clientKey = it.client_key,
                        role = it.role,
                        coreKey = it.core_key
                    )
                } ?: emptyList()
            )
            currentUser = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun selectClient(clientKey: String): Result<User> {
        return try {
            val user = currentUser ?: return Result.failure(Exception("No hay usuario autenticado"))
            val role = user.roles.find { it.clientKey == clientKey }
            val updatedUser = user.copy(
                selectedClientKey = clientKey,
                role = role?.role ?: user.role
            )
            currentUser = updatedUser
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
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

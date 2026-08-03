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

    private fun profileToUser(
        profile: com.nitanmal.app.data.remote.model.ProfileDto,
        fallbackId: String = "",
        fallbackName: String = "",
        fallbackEmail: String = "",
        fallbackPhoto: String? = null
    ): User = User(
        id = profile.userId ?: fallbackId,
        name = profile.name?.takeIf { it.isNotBlank() } ?: fallbackName,
        email = profile.email ?: fallbackEmail,
        photoUrl = profile.photoURL ?: fallbackPhoto,
        isAuthenticated = true,
        role = profile.role,
        plan = profile.plan,
        apodo = profile.apodo,
        pais = profile.pais,
        region = profile.region,
        telefono = profile.telefono
    )

    override suspend fun restoreSession(): User? {
        val cached = platformAuth.getCachedUser() ?: return null
        val token = platformAuth.getFirebaseIdToken() ?: return null
        return try {
            val profile = apiService.getMe(token).user ?: return null
            profileToUser(
                profile,
                fallbackId = cached.userId,
                fallbackName = cached.displayName ?: "",
                fallbackEmail = cached.email,
                fallbackPhoto = cached.photoUrl
            ).also { currentUser = it }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateProfile(
        apodo: String?,
        pais: String?,
        region: String?,
        telefono: String?
    ): Result<User> = try {
        val token = platformAuth.getFirebaseIdToken()
            ?: throw IllegalStateException("No hay sesión activa")
        val profile = apiService.updateMe(
            token,
            com.nitanmal.app.data.remote.ProfileUpdateInput(
                apodo = apodo, pais = pais, region = region, telefono = telefono
            )
        ).user ?: throw IllegalStateException("El backend no devolvió el perfil")
        val previo = currentUser
        val user = profileToUser(
            profile,
            fallbackId = previo?.id ?: "",
            fallbackName = previo?.name ?: "",
            fallbackEmail = previo?.email ?: "",
            fallbackPhoto = previo?.photoUrl
        )
        currentUser = user
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

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

            val user = profileToUser(
                profile,
                fallbackId = signInResult.userId,
                fallbackName = signInResult.displayName ?: "",
                fallbackEmail = signInResult.email,
                // Preferimos la foto subida al backend; si no hay, la de Google.
                fallbackPhoto = signInResult.photoUrl
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

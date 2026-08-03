package com.nitanmal.app.data.remote

import com.nitanmal.app.data.remote.model.MeResponse

interface IAuthApiService {
    /** GET /me con el Firebase ID token: registra el login y devuelve el perfil. */
    suspend fun getMe(firebaseIdToken: String): MeResponse

    /** PUT /me: actualiza apodo/país/región/teléfono. */
    suspend fun updateMe(firebaseIdToken: String, input: ProfileUpdateInput): MeResponse
}

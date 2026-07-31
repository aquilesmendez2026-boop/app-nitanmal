package com.nitanmal.app.data.remote

import com.nitanmal.app.data.remote.model.VerifyResponse

interface IAuthApiService {
    suspend fun verify(
        firebaseIdToken: String,
        coreKey: String,
        clientKey: String? = null,
        env: String? = null
    ): VerifyResponse
}

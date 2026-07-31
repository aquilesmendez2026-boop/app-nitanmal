package com.nitanmal.app.data.remote

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.nitanmal.app.core.logger.Logger
import com.nitanmal.app.data.remote.model.VerifyRequest
import com.nitanmal.app.data.remote.model.VerifyResponse

class AuthApiService : IAuthApiService {
    private val client = ApiClient.httpClient

    override suspend fun verify(
        firebaseIdToken: String,
        coreKey: String,
        clientKey: String?,
        env: String?
    ): VerifyResponse {
        val url = "${AuthConfig.AUTH_API_URL}/verify"
        Logger.d("AuthApi", "POST $url  coreKey=$coreKey clientKey=$clientKey env=$env  tokenLen=${firebaseIdToken.length}")
        return try {
            val response = client.post(url) {
                header(HttpHeaders.Authorization, "Bearer $firebaseIdToken")
                setBody(
                    VerifyRequest(
                        core_key = coreKey,
                        client_key = clientKey,
                        env = env
                    )
                )
            }
            val bodyText = response.bodyAsText()
            Logger.d("AuthApi", "RESPONSE ${response.status.value} ${response.status.description}: $bodyText")
            if (!response.status.isSuccess()) {
                throw RuntimeException("HTTP ${response.status.value}: $bodyText")
            }
            response.body()
        } catch (e: Exception) {
            Logger.d("AuthApi", "EXCEPTION: ${e::class.simpleName}: ${e.message}")
            throw e.toNetworkException()
        }
    }
}

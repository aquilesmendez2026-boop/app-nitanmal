package com.nitanmal.app.data.remote

import com.nitanmal.app.core.logger.Logger
import com.nitanmal.app.data.remote.model.MeResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class AuthApiService : IAuthApiService {
    private val client = ApiClient.httpClient

    override suspend fun getMe(firebaseIdToken: String): MeResponse {
        val url = "${AuthConfig.API_URL}/me"
        Logger.d("AuthApi", "GET $url  tokenLen=${firebaseIdToken.length}")
        return try {
            val response = client.get(url) {
                header(HttpHeaders.Authorization, "Bearer $firebaseIdToken")
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

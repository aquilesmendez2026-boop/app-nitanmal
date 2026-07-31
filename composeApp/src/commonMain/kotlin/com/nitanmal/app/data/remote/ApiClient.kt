package com.nitanmal.app.data.remote

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object ApiClient {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
        encodeDefaults = false  // Don't encode null/default values
        explicitNulls = false   // Omit null fields from JSON
    }

    val httpClient: HttpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(json)
            }

            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
                filter { request ->
                    request.url.host.contains("execute-api") ||
                    request.url.host.contains("umine")
                }
            }

            install(DefaultRequest) {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 15_000
            }

            defaultRequest {
                headers {
                    append(HttpHeaders.UserAgent, "Mozilla/5.0 (Linux; Android) AppleWebKit/537.36 KHTML, like Gecko Chrome/120.0 Mobile Safari/537.36")
                }
            }
        }
    }

    fun close() {
        httpClient.close()
    }
}

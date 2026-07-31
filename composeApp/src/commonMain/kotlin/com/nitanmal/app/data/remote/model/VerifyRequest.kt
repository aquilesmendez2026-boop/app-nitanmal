package com.nitanmal.app.data.remote.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class VerifyRequest(
    val core_key: String,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val client_key: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val env: String? = null
)

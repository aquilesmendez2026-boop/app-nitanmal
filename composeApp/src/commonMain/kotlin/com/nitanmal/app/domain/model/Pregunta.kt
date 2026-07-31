package com.nitanmal.app.domain.model

import kotlinx.serialization.Serializable

/** Pregunta del público (buzón). Contrato: backend rutas /preguntas. */
@Serializable
data class Pregunta(
    val id: String,
    val contenido: String = "",
    val fromUserId: String? = null,
    val fromName: String? = null,
    val fromEmail: String? = null,
    val answered: Boolean = false,
    val createdAt: String? = null
)

package com.nitanmal.app.domain.model

import kotlinx.serialization.Serializable

/** Notificación del usuario. Contrato: backend GET /notificaciones. */
@Serializable
data class Notificacion(
    val id: String,
    val texto: String = "",
    val episodioId: String? = null,
    val episodioTitulo: String? = null,
    val stage: String? = null,
    val leida: Boolean = false,
    val createdAt: String? = null
)

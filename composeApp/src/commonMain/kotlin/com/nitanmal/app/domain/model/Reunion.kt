package com.nitanmal.app.domain.model

import kotlinx.serialization.Serializable

/** Reunión del equipo. Contrato: backend rutas /reuniones. */
@Serializable
data class Reunion(
    val id: String,
    val date: String = "",
    val time: String = "",
    val title: String = "",
    val description: String? = null,
    val lugar: String? = null,
    val createdByUserId: String? = null,
    val createdByName: String? = null,
    val createdAt: String? = null
)

package com.nitanmal.app.domain.model

import kotlinx.serialization.Serializable

/** Canal/red social del proyecto. Contrato: GET /socials. */
@Serializable
data class Canal(
    val plataforma: String = "",
    val handle: String = "",
    val url: String = "",
    val visible: Boolean = true,
    val seguidores: String = "",
    /** true si `seguidores` vino de la API de la plataforma. */
    val auto: Boolean = false,
    /** true si el canal está transmitiendo en vivo ahora. */
    val enVivo: Boolean = false
)

/** Métricas de seguidores. Contrato: GET /metricas. */
@Serializable
data class PuntoMetrica(
    val fecha: String = "",
    val seguidores: Long = 0
)

@Serializable
data class MetricaActual(
    val plataforma: String = "",
    val seguidores: Long = 0,
    val fecha: String = "",
    val delta: Long = 0
)

@Serializable
data class Metricas(
    val series: Map<String, List<PuntoMetrica>> = emptyMap(),
    val actuales: List<MetricaActual> = emptyList()
)

package com.nitanmal.app.domain.model

import kotlinx.serialization.Serializable

// ── Modelos del lado fan (rutas públicas + Mi Zona) ──

/** Estado del en vivo. Contrato: GET /live (público). */
@Serializable
data class LiveState(
    val isLive: Boolean = false,
    val videoId: String = "",
    val title: String = "",
    val platform: String? = null
)

/** Teaser público de sorteos activos (viene junto con GET /live). */
@Serializable
data class SorteoPublico(
    val titulo: String = "",
    val premio: String = "",
    val fecha: String = ""
)

/** Show/evento del calendario. Contrato: GET /eventos (público). */
@Serializable
data class Evento(
    val id: String,
    val date: String = "",
    val time: String = "",
    val title: String = "",
    val type: String = "",
    val description: String? = null,
    val premium: Boolean = false,
    val createdBy: String? = null,
    val createdAt: String? = null
)

@Serializable
data class EpisodioLinks(
    val spotify: String? = null,
    val youtube: String? = null,
    val apple: String? = null
)

/** Episodio del podcast. Contrato: GET /episodios (público). */
@Serializable
data class EpisodioFan(
    val id: String,
    val number: Int = 0,
    val title: String = "",
    val description: String = "",
    val showNotes: String? = null,
    val duration: String = "",
    val date: String? = null,
    val premium: Boolean = false,
    val links: EpisodioLinks? = null
)

/** Descarga de Mi Zona. Contrato: GET /descargas (miembro; url firmada). */
@Serializable
data class Descarga(
    val id: String,
    val title: String = "",
    val type: String = "otro",
    val fileKey: String? = null,
    val filename: String? = null,
    val size: String? = null,
    val premium: Boolean = false,
    val url: String? = null,
    val createdAt: String? = null
)

// ── Zona de registrados (POST /zona, acción en el body) ──

@Serializable
data class Sugerencia(
    val id: String,
    val tipo: String = "tema",
    val texto: String = "",
    val createdByName: String? = null,
    val createdByUserId: String? = null,
    val votos: Int = 0,
    val miVoto: Boolean = false,
    val createdAt: String? = null
)

@Serializable
data class Sorteo(
    val id: String,
    val activo: Boolean = false,
    val titulo: String = "",
    val premio: String = "",
    val comoParticipar: String = "",
    val fecha: String = "",
    val enlace: String = "",
    val createdAt: String? = null,
    val participantes: Int = 0,
    val participa: Boolean = false,
    val misChances: Int? = null
)

@Serializable
data class OpcionEncuesta(
    val id: String,
    val texto: String = "",
    val votos: Int = 0
)

@Serializable
data class Encuesta(
    val id: String,
    val pregunta: String = "",
    val tipo: String = "si_no",
    val activa: Boolean = false,
    val total: Int = 0,
    val miVoto: String? = null,
    val opciones: List<OpcionEncuesta> = emptyList(),
    val createdAt: String? = null
)

@Serializable
data class ZonaData(
    val sugerencias: List<Sugerencia> = emptyList(),
    val sorteos: List<Sorteo> = emptyList(),
    val encuestas: List<Encuesta> = emptyList(),
    val referidos: Int = 0,
    val error: String? = null
)

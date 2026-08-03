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

// ── Metadatos por plataforma (espejo de src/molecules/plataformas.tsx del web) ──

/**
 * Color de marca y sustantivo por red. `color == null` → neutral
 * (tiktok/x/threads son blanco en el web oscuro; en la app usamos onSurface).
 */
data class PlataformaMeta(
    val label: String,
    val color: Long?,
    val noun: String
)

val PLATAFORMA_META: Map<String, PlataformaMeta> = mapOf(
    "twitch" to PlataformaMeta("Twitch", 0xFF9146ff, "seguidores"),
    "youtube" to PlataformaMeta("YouTube", 0xFFff0033, "suscriptores"),
    "kick" to PlataformaMeta("Kick", 0xFF3fc510, "seguidores"),
    "tiktok" to PlataformaMeta("TikTok", null, "seguidores"),
    "instagram" to PlataformaMeta("Instagram", 0xFFe1306c, "seguidores"),
    "x" to PlataformaMeta("X", null, "seguidores"),
    "facebook" to PlataformaMeta("Facebook", 0xFF1877f2, "seguidores"),
    "spotify" to PlataformaMeta("Spotify", 0xFF1db954, "oyentes"),
    "threads" to PlataformaMeta("Threads", null, "seguidores")
)

/** "12500" → "12K" · "1500" → "1.5K" · texto no numérico se muestra tal cual. */
fun fmtSeguidores(s: String): String {
    val n = s.trim().toLongOrNull() ?: return s
    if (n < 1000) return n.toString()
    val miles = n / 1000.0
    return if (n >= 10_000) {
        "${miles.toLong()}K"
    } else {
        val txt = ((miles * 10).toLong() / 10.0).toString().removeSuffix(".0")
        "${txt}K"
    }
}

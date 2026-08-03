package com.nitanmal.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Post del planificador de contenido (borradores por plataforma,
 * generados por el agente de IA o creados a mano).
 * Contrato: backend ruta /planificador (acciones por POST).
 */
@Serializable
data class Post(
    val id: String,
    val loteId: String? = null,
    val plataforma: String = "",
    val estado: String = "borrador",
    val fecha: String? = null,
    val titulo: String? = null,
    val copy: String = "",
    val hashtags: List<String>? = null,
    val tipoPost: String? = null,
    val formatoSugerido: String? = null,
    val duracionSugerida: String? = null,
    val assetKey: String? = null,
    val assetTipo: String? = null,
    val assetUrl: String? = null,
    val enlace: String? = null,
    val responsableId: String? = null,
    val responsable: String? = null,
    val generadoPorIA: Boolean = false,
    val createdByUserId: String? = null,
    val createdByName: String? = null,
    val createdAt: String? = null,
    val publicadoAt: String? = null
)

/** Estados del planificador con etiqueta y color (mismo set del web). */
val PLAN_ESTADOS: List<Triple<String, String, Long>> = listOf(
    Triple("sugerido", "Sugerido por IA", 0xFFa78bfa),
    Triple("borrador", "Borrador", 0xFF94a3b8),
    Triple("programado", "Programado", 0xFFf59e0b),
    Triple("publicado", "Publicado", 0xFF22c55e),
    Triple("descartado", "Descartado", 0xFFef4444)
)

val PLATAFORMAS = listOf(
    "twitch", "youtube", "kick", "tiktok", "instagram",
    "x", "facebook", "spotify", "threads"
)

fun plataformaLabel(p: String): String = when (p) {
    "x" -> "X"
    "youtube" -> "YouTube"
    "tiktok" -> "TikTok"
    else -> p.replaceFirstChar { it.uppercase() }
}

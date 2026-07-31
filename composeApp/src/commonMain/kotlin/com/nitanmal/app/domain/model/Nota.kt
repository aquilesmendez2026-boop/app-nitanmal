package com.nitanmal.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Idea del equipo (el backend las llama "notas").
 * Mismo contrato que el web: backend/src/index.mjs → rutas /notas.
 */
@Serializable
data class Nota(
    val id: String,
    val titulo: String? = null,
    val contenido: String = "",
    val audios: List<NotaMedia>? = null,
    val imagenes: List<NotaMedia>? = null,
    val enlaces: List<String>? = null,
    val etiquetas: List<String>? = null,
    val estado: String? = null,
    val pinned: Boolean = false,
    /** userId → emoji */
    val reacciones: Map<String, String>? = null,
    val comentarios: List<NotaComentario>? = null,
    val responsableId: String? = null,
    val responsable: String? = null,
    val fechaObjetivo: String? = null,
    val episodioId: String? = null,
    val createdByName: String? = null,
    val createdByUserId: String? = null,
    val createdAt: String? = null
)

@Serializable
data class NotaMedia(
    val key: String,
    val nombre: String? = null,
    val url: String? = null
)

@Serializable
data class NotaComentario(
    val id: String,
    val userId: String? = null,
    val nombre: String? = null,
    val texto: String = "",
    val createdAt: String? = null
)

/** Estados válidos de una idea, con etiqueta para UI (mismo set del web). */
enum class NotaEstado(val key: String, val label: String) {
    NUEVA("nueva", "Nueva"),
    REVISION("revision", "En revisión"),
    APROBADA("aprobada", "Aprobada"),
    DESCARTADA("descartada", "Descartada"),
    CONVERTIDA("convertida", "Convertida");

    companion object {
        fun fromKey(key: String?): NotaEstado = entries.firstOrNull { it.key == key } ?: NUEVA
    }
}

/** Set fijo de etiquetas con color (v1, mismo del web). */
val NOTA_ETIQUETAS: List<Pair<String, Long>> = listOf(
    "Invitado" to 0xFF22d3ee,
    "Sketch" to 0xFFd946ef,
    "Canción del día" to 0xFF22c55e,
    "Banner" to 0xFFf59e0b,
    "Segmento" to 0xFF8b5cf6,
    "Debate" to 0xFFec4899
)

val NOTA_EMOJIS = listOf("👍", "🔥", "❤️", "😂", "💡")

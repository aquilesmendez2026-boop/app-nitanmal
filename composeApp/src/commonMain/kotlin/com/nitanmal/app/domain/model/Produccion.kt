package com.nitanmal.app.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Episodio de producción con etapas tipadas.
 * Contrato: backend rutas /produccion (mismas plantillas que el web).
 */
@Serializable
data class Episodio(
    val id: String,
    val titulo: String = "",
    val stages: Map<String, Etapa>? = null,
    val createdByUserId: String? = null,
    val createdByName: String? = null,
    val createdAt: String? = null
) {
    fun etapa(key: String): Etapa = stages?.get(key) ?: Etapa()

    val aprobadas: Int
        get() = Plantillas.STAGES.count { etapa(it).estado == "aprobada" }
}

@Serializable
data class Etapa(
    val responsable: String? = null,
    val responsableId: String? = null,
    val fecha: String? = null,
    val estado: String? = null,
    val subtareas: List<Subtarea>? = null,
    val done: Boolean = false,
    /**
     * Valores tipados según la plantilla de la etapa. Heterogéneo:
     * texto/fecha/url/select → string, numero → number, checkbox → bool,
     * file → {archivoKey, archivoNombre, archivoUrl}.
     */
    val values: JsonObject? = null,
    val templateVersion: Int? = null,
    val historial: List<Transicion>? = null
)

@Serializable
data class Subtarea(
    val id: String,
    val texto: String = "",
    val desc: String? = null,
    val hecha: Boolean = false
)

@Serializable
data class Transicion(
    val de: String? = null,
    val a: String? = null,
    val porUserId: String? = null,
    val porNombre: String? = null,
    val cuando: String? = null
)

/** Miembro del equipo (para asignar responsables). Contrato: GET /equipo. */
@Serializable
data class MiembroEquipo(
    val userId: String,
    val nombre: String = ""
)

// ── Helpers para leer values heterogéneos ──

fun JsonObject?.stringValue(key: String): String =
    (this?.get(key) as? JsonPrimitive)?.content ?: ""

fun JsonObject?.boolValue(key: String): Boolean =
    (this?.get(key) as? JsonPrimitive)?.content == "true"

/** Para campos file: (nombre, urlFirmada) o null si no hay archivo. */
fun JsonObject?.fileValue(key: String): Pair<String, String?>? {
    val obj = this?.get(key) as? JsonObject ?: return null
    val nombre = (obj["archivoNombre"] as? JsonPrimitive)?.content ?: "archivo"
    val url = (obj["archivoUrl"] as? JsonPrimitive)?.content
    return if (obj["archivoKey"] != null) nombre to url else null
}

// ── Plantillas de etapas (espejo de STAGE_TEMPLATES del backend) ──

enum class CampoTipo { TEXTO, TEXTO_LARGO, FECHA, NUMERO, SELECT, CHECKBOX, URL, FILE }

data class CampoPlantilla(
    val key: String,
    val label: String,
    val tipo: CampoTipo,
    val required: Boolean,
    val options: List<String> = emptyList()
)

object Plantillas {
    val STAGES = listOf("idea", "guion", "grabacion", "edicion", "programado", "publicado")

    val LABELS = mapOf(
        "idea" to "Idea",
        "guion" to "Guion",
        "grabacion" to "Grabación",
        "edicion" to "Edición",
        "programado" to "Programado",
        "publicado" to "Publicado"
    )

    val ESTADOS = listOf(
        "pendiente" to "Pendiente",
        "en_progreso" to "En progreso",
        "en_revision" to "En revisión",
        "aprobada" to "Aprobada"
    )

    val ENTREGAS = mapOf(
        "idea" to "Para el guionista",
        "guion" to "Para quien graba",
        "grabacion" to "Para el editor",
        "edicion" to "Para quien programa",
        "programado" to "Para publicar",
        "publicado" to "Cierre"
    )

    val CAMPOS: Map<String, List<CampoPlantilla>> = mapOf(
        "idea" to listOf(
            CampoPlantilla("tema", "Tema central", CampoTipo.TEXTO, required = true),
            CampoPlantilla("tipo", "Tipo de episodio", CampoTipo.SELECT, required = true, options = listOf("Entrevista", "Debate", "Solo", "Especial")),
            CampoPlantilla("angulo", "Ángulo / enfoque", CampoTipo.TEXTO_LARGO, required = true),
            CampoPlantilla("invitado", "Invitado propuesto", CampoTipo.TEXTO, required = false),
            CampoPlantilla("referencias", "Referencias / links", CampoTipo.TEXTO_LARGO, required = false)
        ),
        "guion" to listOf(
            CampoPlantilla("gancho", "Gancho de apertura", CampoTipo.TEXTO_LARGO, required = true),
            CampoPlantilla("bloques", "Estructura por bloques", CampoTipo.TEXTO_LARGO, required = true),
            CampoPlantilla("duracion", "Duración estimada (min)", CampoTipo.NUMERO, required = false),
            CampoPlantilla("documento", "Guion (documento)", CampoTipo.FILE, required = true)
        ),
        "grabacion" to listOf(
            CampoPlantilla("fecha_grab", "Fecha de grabación", CampoTipo.FECHA, required = true),
            CampoPlantilla("lugar", "Lugar / estudio", CampoTipo.TEXTO, required = false),
            CampoPlantilla("crudo", "Audio/video crudo", CampoTipo.FILE, required = true),
            CampoPlantilla("notas_edicion", "Notas para edición", CampoTipo.TEXTO_LARGO, required = false)
        ),
        "edicion" to listOf(
            CampoPlantilla("master", "Master final (audio/video)", CampoTipo.FILE, required = true),
            CampoPlantilla("duracion_final", "Duración final (min)", CampoTipo.NUMERO, required = false),
            CampoPlantilla("notas", "Notas de la edición", CampoTipo.TEXTO_LARGO, required = false)
        ),
        "programado" to listOf(
            CampoPlantilla("titulo_pub", "Título de publicación", CampoTipo.TEXTO, required = true),
            CampoPlantilla("descripcion", "Descripción / show notes", CampoTipo.TEXTO_LARGO, required = true),
            CampoPlantilla("fecha_pub", "Fecha de publicación", CampoTipo.FECHA, required = true),
            CampoPlantilla("portada", "Portada / miniatura", CampoTipo.FILE, required = false),
            CampoPlantilla("plataformas", "Plataformas (Spotify, YouTube…)", CampoTipo.TEXTO, required = false)
        ),
        "publicado" to listOf(
            CampoPlantilla("url_youtube", "Enlace YouTube", CampoTipo.URL, required = false),
            CampoPlantilla("url_spotify", "Enlace Spotify", CampoTipo.URL, required = false),
            CampoPlantilla("publicado_ok", "Confirmar publicado", CampoTipo.CHECKBOX, required = true)
        )
    )
}

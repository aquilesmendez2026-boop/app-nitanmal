package com.nitanmal.app.data.remote.model

import com.nitanmal.app.domain.model.Episodio
import com.nitanmal.app.domain.model.MiembroEquipo
import com.nitanmal.app.domain.model.Nota
import com.nitanmal.app.domain.model.Notificacion
import com.nitanmal.app.domain.model.Pregunta
import com.nitanmal.app.domain.model.Reunion
import com.nitanmal.app.domain.model.Subtarea
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class NotasResponse(val notas: List<Nota> = emptyList(), val error: String? = null)

@Serializable
data class NotaResponse(val nota: Nota? = null, val error: String? = null)

@Serializable
data class ConvertirResponse(
    val nota: Nota? = null,
    val episodioId: String? = null,
    val error: String? = null
)

@Serializable
data class NotificacionesResponse(
    val notificaciones: List<Notificacion> = emptyList(),
    val error: String? = null
)

@Serializable
data class PreguntasResponse(val preguntas: List<Pregunta> = emptyList(), val error: String? = null)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class NotaInput(
    @EncodeDefault(EncodeDefault.Mode.NEVER) val titulo: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val contenido: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val etiquetas: List<String>? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val audios: List<MediaRef>? = null
)

/** Referencia a un archivo ya subido a S3 (key devuelta por /notas-upload). */
@Serializable
data class MediaRef(val key: String, val nombre: String)

@Serializable
data class UploadRequest(val filename: String, val contentType: String)

@Serializable
data class UploadUrlResponse(
    val uploadUrl: String? = null,
    val key: String? = null,
    val error: String? = null
)

@Serializable
data class ReaccionInput(val emoji: String)

@Serializable
data class ComentarioInput(val texto: String)

@Serializable
data class EstadoInput(val estado: String)

@Serializable
data class AnsweredInput(val answered: Boolean)

// ── Producción ──

@Serializable
data class ProduccionResponse(val produccion: List<Episodio> = emptyList(), val error: String? = null)

@Serializable
data class EpisodioResponse(val item: Episodio? = null, val error: String? = null)

@Serializable
data class EquipoResponse(val equipo: List<MiembroEquipo> = emptyList(), val error: String? = null)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ProduccionCreateInput(
    val titulo: String,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val idea: String? = null
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class StageDataInput(
    @EncodeDefault(EncodeDefault.Mode.NEVER) val estado: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val responsable: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val responsableId: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val fecha: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val subtareas: List<Subtarea>? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val values: JsonObject? = null
)

@Serializable
data class ProduccionUpdateInput(
    val stage: String,
    val stageData: StageDataInput
)

// ── Reuniones ──

@Serializable
data class ReunionesResponse(val reuniones: List<Reunion> = emptyList(), val error: String? = null)

@Serializable
data class ReunionResponse(val reunion: Reunion? = null, val error: String? = null)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ReunionInput(
    val date: String,
    val time: String,
    val title: String,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val description: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val lugar: String? = null
)

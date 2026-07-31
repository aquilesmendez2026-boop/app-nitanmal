package com.nitanmal.app.data.remote.model

import com.nitanmal.app.domain.model.Nota
import com.nitanmal.app.domain.model.Notificacion
import com.nitanmal.app.domain.model.Pregunta
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

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
    @EncodeDefault(EncodeDefault.Mode.NEVER) val etiquetas: List<String>? = null
)

@Serializable
data class ReaccionInput(val emoji: String)

@Serializable
data class ComentarioInput(val texto: String)

@Serializable
data class EstadoInput(val estado: String)

@Serializable
data class AnsweredInput(val answered: Boolean)

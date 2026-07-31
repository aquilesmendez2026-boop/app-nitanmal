package com.nitanmal.app.domain.repository

import com.nitanmal.app.data.remote.model.StageDataInput
import com.nitanmal.app.domain.model.Episodio
import com.nitanmal.app.domain.model.MiembroEquipo
import com.nitanmal.app.domain.model.Nota
import com.nitanmal.app.domain.model.Notificacion
import com.nitanmal.app.domain.model.Pregunta
import com.nitanmal.app.domain.model.Reunion

/** Audio grabado pendiente de subir junto con una idea. */
data class AudioAdjunto(
    val bytes: ByteArray,
    val filename: String,
    val contentType: String
)

interface TeamRepository {
    // Ideas
    suspend fun listNotas(): Result<List<Nota>>
    suspend fun createNota(
        titulo: String?,
        contenido: String?,
        etiquetas: List<String>,
        audio: AudioAdjunto? = null
    ): Result<Nota>
    suspend fun deleteNota(id: String): Result<Unit>
    suspend fun reaccionarNota(id: String, emoji: String): Result<Nota>
    suspend fun comentarNota(id: String, texto: String): Result<Nota>
    suspend fun borrarComentario(id: String, comentarioId: String): Result<Nota>
    suspend fun setNotaEstado(id: String, estado: String): Result<Nota>
    suspend fun togglePinNota(id: String): Result<Nota>

    /** Convierte la idea en episodio de producción. Devuelve la nota actualizada. */
    suspend fun convertirNota(id: String): Result<Nota>

    // Buzón
    suspend fun listPreguntas(): Result<List<Pregunta>>
    suspend fun setPreguntaAnswered(id: String, answered: Boolean): Result<Unit>
    suspend fun deletePregunta(id: String): Result<Unit>

    // Notificaciones
    suspend fun listNotificaciones(): Result<List<Notificacion>>
    suspend fun marcarNotificacionesLeidas(): Result<Unit>

    // Producción
    suspend fun listProduccion(): Result<List<Episodio>>
    suspend fun createEpisodio(titulo: String, idea: String?): Result<Episodio>
    suspend fun updateEpisodioStage(id: String, stage: String, data: StageDataInput): Result<Episodio>
    suspend fun deleteEpisodio(id: String): Result<Unit>
    suspend fun listEquipo(): Result<List<MiembroEquipo>>

    // Reuniones
    suspend fun listReuniones(): Result<List<Reunion>>
    suspend fun createReunion(date: String, time: String, title: String, description: String?, lugar: String?): Result<Reunion>
    suspend fun deleteReunion(id: String): Result<Unit>
}

package com.nitanmal.app.domain.repository

import com.nitanmal.app.data.remote.model.PlanificadorInput
import com.nitanmal.app.data.remote.model.StageDataInput
import com.nitanmal.app.domain.model.Canal
import com.nitanmal.app.domain.model.Episodio
import com.nitanmal.app.domain.model.Metricas
import com.nitanmal.app.domain.model.MiembroEquipo
import com.nitanmal.app.domain.model.Nota
import com.nitanmal.app.domain.model.Notificacion
import com.nitanmal.app.domain.model.Post
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
        audios: List<AudioAdjunto> = emptyList()
    ): Result<Nota>
    suspend fun deleteNota(id: String): Result<Unit>
    suspend fun reaccionarNota(id: String, emoji: String): Result<Nota>
    suspend fun comentarNota(id: String, texto: String): Result<Nota>
    suspend fun borrarComentario(id: String, comentarioId: String): Result<Nota>
    suspend fun setNotaEstado(id: String, estado: String): Result<Nota>
    suspend fun togglePinNota(id: String): Result<Nota>

    /** Convierte la idea en episodio de producción. Devuelve la nota actualizada. */
    suspend fun convertirNota(id: String): Result<Nota>

    /** Edita título/contenido/etiquetas/enlaces (solo el creador). */
    suspend fun editNota(
        id: String,
        titulo: String?,
        contenido: String?,
        etiquetas: List<String>?,
        enlaces: List<String>?
    ): Result<Nota>

    /** Lanza (o consulta) la transcripción de un audio de la idea. */
    suspend fun transcribirNota(id: String, audioKey: String): Result<Nota>

    // Planificador
    suspend fun listPlanificador(): Result<List<Post>>
    suspend fun generarPosts(input: PlanificadorInput): Result<List<Post>>
    suspend fun createPost(input: PlanificadorInput): Result<Post>
    suspend fun updatePost(input: PlanificadorInput): Result<Post>
    suspend fun deletePost(id: String): Result<Unit>

    // Canales y métricas
    suspend fun listSocials(): Result<List<Canal>>
    suspend fun getMetricas(): Result<Metricas>

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
    suspend fun listUsuarios(): Result<List<com.nitanmal.app.domain.model.UsuarioAdmin>>
    suspend fun guardarSocials(canales: List<Canal>): Result<List<Canal>>
    suspend fun cambiarRol(userId: String, role: String): Result<Unit>

    // Reuniones
    suspend fun listReuniones(): Result<List<Reunion>>
    suspend fun createReunion(date: String, time: String, title: String, description: String?, lugar: String?): Result<Reunion>
    suspend fun deleteReunion(id: String): Result<Unit>
}

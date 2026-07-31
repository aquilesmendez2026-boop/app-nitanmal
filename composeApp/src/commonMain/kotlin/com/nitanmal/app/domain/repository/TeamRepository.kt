package com.nitanmal.app.domain.repository

import com.nitanmal.app.domain.model.Nota
import com.nitanmal.app.domain.model.Notificacion
import com.nitanmal.app.domain.model.Pregunta

interface TeamRepository {
    // Ideas
    suspend fun listNotas(): Result<List<Nota>>
    suspend fun createNota(titulo: String?, contenido: String?, etiquetas: List<String>): Result<Nota>
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
}

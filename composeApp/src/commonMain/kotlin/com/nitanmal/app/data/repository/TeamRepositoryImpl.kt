package com.nitanmal.app.data.repository

import com.nitanmal.app.data.remote.ITeamApiService
import com.nitanmal.app.data.remote.TeamApiService
import com.nitanmal.app.data.remote.model.NotaInput
import com.nitanmal.app.domain.auth.PlatformAuth
import com.nitanmal.app.domain.model.Nota
import com.nitanmal.app.domain.model.Notificacion
import com.nitanmal.app.domain.model.Pregunta
import com.nitanmal.app.domain.repository.TeamRepository

class TeamRepositoryImpl(
    private val platformAuth: PlatformAuth,
    private val apiService: ITeamApiService = TeamApiService()
) : TeamRepository {

    private suspend fun token(): String =
        platformAuth.getFirebaseIdToken()
            ?: throw IllegalStateException("No hay sesión activa")

    private suspend fun <T> call(block: suspend (String) -> T): Result<T> =
        try {
            Result.success(block(token()))
        } catch (e: Exception) {
            Result.failure(e)
        }

    private fun requireNota(nota: Nota?): Nota =
        nota ?: throw IllegalStateException("El backend no devolvió la idea")

    // ── Ideas ──
    override suspend fun listNotas(): Result<List<Nota>> =
        call { apiService.listNotas(it).notas }

    override suspend fun createNota(
        titulo: String?,
        contenido: String?,
        etiquetas: List<String>
    ): Result<Nota> = call {
        requireNota(
            apiService.createNota(
                it,
                NotaInput(
                    titulo = titulo?.takeIf { t -> t.isNotBlank() },
                    contenido = contenido?.takeIf { c -> c.isNotBlank() },
                    etiquetas = etiquetas.takeIf { e -> e.isNotEmpty() }
                )
            ).nota
        )
    }

    override suspend fun deleteNota(id: String): Result<Unit> =
        call { apiService.deleteNota(it, id) }

    override suspend fun reaccionarNota(id: String, emoji: String): Result<Nota> =
        call { requireNota(apiService.reaccionarNota(it, id, emoji).nota) }

    override suspend fun comentarNota(id: String, texto: String): Result<Nota> =
        call { requireNota(apiService.comentarNota(it, id, texto).nota) }

    override suspend fun borrarComentario(id: String, comentarioId: String): Result<Nota> =
        call { requireNota(apiService.borrarComentario(it, id, comentarioId).nota) }

    override suspend fun setNotaEstado(id: String, estado: String): Result<Nota> =
        call { requireNota(apiService.setNotaEstado(it, id, estado).nota) }

    override suspend fun togglePinNota(id: String): Result<Nota> =
        call { requireNota(apiService.togglePinNota(it, id).nota) }

    override suspend fun convertirNota(id: String): Result<Nota> =
        call { requireNota(apiService.convertirNota(it, id).nota) }

    // ── Buzón ──
    override suspend fun listPreguntas(): Result<List<Pregunta>> =
        call { apiService.listPreguntas(it).preguntas }

    override suspend fun setPreguntaAnswered(id: String, answered: Boolean): Result<Unit> =
        call { apiService.setPreguntaAnswered(it, id, answered) }

    override suspend fun deletePregunta(id: String): Result<Unit> =
        call { apiService.deletePregunta(it, id) }

    // ── Notificaciones ──
    override suspend fun listNotificaciones(): Result<List<Notificacion>> =
        call { apiService.listNotificaciones(it).notificaciones }

    override suspend fun marcarNotificacionesLeidas(): Result<Unit> =
        call { apiService.marcarNotificacionesLeidas(it) }
}

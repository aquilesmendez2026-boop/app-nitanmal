package com.nitanmal.app.data.repository

import com.nitanmal.app.data.remote.ITeamApiService
import com.nitanmal.app.data.remote.TeamApiService
import com.nitanmal.app.data.remote.model.MediaRef
import com.nitanmal.app.data.remote.model.NotaEditInput
import com.nitanmal.app.data.remote.model.NotaInput
import com.nitanmal.app.data.remote.model.PlanificadorInput
import com.nitanmal.app.data.remote.model.ProduccionCreateInput
import com.nitanmal.app.data.remote.model.ProduccionUpdateInput
import com.nitanmal.app.data.remote.model.ReunionInput
import com.nitanmal.app.data.remote.model.StageDataInput
import com.nitanmal.app.domain.auth.PlatformAuth
import com.nitanmal.app.domain.model.Canal
import com.nitanmal.app.domain.model.Episodio
import com.nitanmal.app.domain.model.Metricas
import com.nitanmal.app.domain.model.MiembroEquipo
import com.nitanmal.app.domain.model.Nota
import com.nitanmal.app.domain.model.Notificacion
import com.nitanmal.app.domain.model.Post
import com.nitanmal.app.domain.model.Pregunta
import com.nitanmal.app.domain.model.Reunion
import com.nitanmal.app.domain.repository.AudioAdjunto
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
        etiquetas: List<String>,
        audios: List<AudioAdjunto>
    ): Result<Nota> = call { token ->
        // Los audios grabados se suben primero a S3 vía /notas-upload.
        val refs = audios.map { audio ->
            val key = apiService.uploadNotaMedia(token, audio.filename, audio.contentType, audio.bytes)
            MediaRef(key = key, nombre = audio.filename)
        }
        requireNota(
            apiService.createNota(
                token,
                NotaInput(
                    titulo = titulo?.takeIf { t -> t.isNotBlank() },
                    contenido = contenido?.takeIf { c -> c.isNotBlank() },
                    etiquetas = etiquetas.takeIf { e -> e.isNotEmpty() },
                    audios = refs.takeIf { it.isNotEmpty() }
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

    override suspend fun editNota(
        id: String,
        titulo: String?,
        contenido: String?,
        etiquetas: List<String>?,
        enlaces: List<String>?
    ): Result<Nota> = call {
        requireNota(
            apiService.editNota(
                it, id,
                NotaEditInput(
                    titulo = titulo,
                    contenido = contenido,
                    etiquetas = etiquetas,
                    enlaces = enlaces
                )
            ).nota
        )
    }

    override suspend fun transcribirNota(id: String, audioKey: String): Result<Nota> =
        call { requireNota(apiService.transcribirNota(it, id, audioKey).nota) }

    // ── Planificador ──
    private fun requirePost(post: Post?): Post =
        post ?: throw IllegalStateException("El backend no devolvió el post")

    override suspend fun listPlanificador(): Result<List<Post>> =
        call { apiService.listPlanificador(it).posts }

    override suspend fun generarPosts(input: PlanificadorInput): Result<List<Post>> =
        call { apiService.generarPosts(it, input).posts }

    override suspend fun createPost(input: PlanificadorInput): Result<Post> =
        call { requirePost(apiService.createPost(it, input).post) }

    override suspend fun updatePost(input: PlanificadorInput): Result<Post> =
        call { requirePost(apiService.updatePost(it, input).post) }

    override suspend fun deletePost(id: String): Result<Unit> =
        call { apiService.deletePost(it, id) }

    // ── Canales y métricas ──
    override suspend fun listSocials(): Result<List<Canal>> =
        try {
            // /socials es ruta pública: si no hay sesión se pide sin token.
            val token = platformAuth.getFirebaseIdToken() ?: ""
            Result.success(apiService.listSocials(token).canales)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getMetricas(): Result<Metricas> =
        call { apiService.getMetricas(it) }

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

    // ── Producción ──
    private fun requireEpisodio(item: Episodio?): Episodio =
        item ?: throw IllegalStateException("El backend no devolvió el episodio")

    override suspend fun listProduccion(): Result<List<Episodio>> =
        call { apiService.listProduccion(it).produccion }

    override suspend fun createEpisodio(titulo: String, idea: String?): Result<Episodio> =
        call {
            requireEpisodio(
                apiService.createEpisodio(
                    it,
                    ProduccionCreateInput(titulo = titulo, idea = idea?.takeIf { i -> i.isNotBlank() })
                ).item
            )
        }

    override suspend fun updateEpisodioStage(id: String, stage: String, data: StageDataInput): Result<Episodio> =
        call {
            requireEpisodio(
                apiService.updateEpisodio(it, id, ProduccionUpdateInput(stage = stage, stageData = data)).item
            )
        }

    override suspend fun deleteEpisodio(id: String): Result<Unit> =
        call { apiService.deleteEpisodio(it, id) }

    override suspend fun listUsuarios(): Result<List<com.nitanmal.app.domain.model.UsuarioAdmin>> =
        call { apiService.listUsuarios(it).usuarios }

    override suspend fun guardarSocials(canales: List<Canal>): Result<List<Canal>> =
        call { apiService.guardarSocials(it, canales).canales }

    override suspend fun cambiarRol(userId: String, role: String): Result<Unit> =
        call { apiService.cambiarRol(it, userId, role) }

    override suspend fun listEquipo(): Result<List<MiembroEquipo>> =
        call { apiService.listEquipo(it).equipo }

    // ── Reuniones ──
    override suspend fun listReuniones(): Result<List<Reunion>> =
        call { apiService.listReuniones(it).reuniones }

    override suspend fun createReunion(
        date: String,
        time: String,
        title: String,
        description: String?,
        lugar: String?
    ): Result<Reunion> = call {
        apiService.createReunion(
            it,
            ReunionInput(
                date = date,
                time = time,
                title = title,
                description = description?.takeIf { d -> d.isNotBlank() },
                lugar = lugar?.takeIf { l -> l.isNotBlank() }
            )
        ).reunion ?: throw IllegalStateException("El backend no devolvió la reunión")
    }

    override suspend fun deleteReunion(id: String): Result<Unit> =
        call { apiService.deleteReunion(it, id) }
}

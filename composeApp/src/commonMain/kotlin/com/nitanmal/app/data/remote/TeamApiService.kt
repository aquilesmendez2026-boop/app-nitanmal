package com.nitanmal.app.data.remote

import com.nitanmal.app.core.logger.Logger
import com.nitanmal.app.data.remote.model.AnsweredInput
import com.nitanmal.app.data.remote.model.ComentarioInput
import com.nitanmal.app.data.remote.model.ConvertirResponse
import com.nitanmal.app.data.remote.model.EpisodioResponse
import com.nitanmal.app.data.remote.model.EquipoResponse
import com.nitanmal.app.data.remote.model.EstadoInput
import com.nitanmal.app.data.remote.model.NotaInput
import com.nitanmal.app.data.remote.model.NotaResponse
import com.nitanmal.app.data.remote.model.NotasResponse
import com.nitanmal.app.data.remote.model.NotificacionesResponse
import com.nitanmal.app.data.remote.model.PreguntasResponse
import com.nitanmal.app.data.remote.model.ProduccionCreateInput
import com.nitanmal.app.data.remote.model.ProduccionResponse
import com.nitanmal.app.data.remote.model.ProduccionUpdateInput
import com.nitanmal.app.data.remote.model.ReaccionInput
import com.nitanmal.app.data.remote.model.ReunionInput
import com.nitanmal.app.data.remote.model.ReunionResponse
import com.nitanmal.app.data.remote.model.ReunionesResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

interface ITeamApiService {
    // Ideas ("notas")
    suspend fun listNotas(token: String): NotasResponse
    suspend fun createNota(token: String, input: NotaInput): NotaResponse
    suspend fun deleteNota(token: String, id: String)
    suspend fun reaccionarNota(token: String, id: String, emoji: String): NotaResponse
    suspend fun comentarNota(token: String, id: String, texto: String): NotaResponse
    suspend fun borrarComentario(token: String, id: String, comentarioId: String): NotaResponse
    suspend fun setNotaEstado(token: String, id: String, estado: String): NotaResponse
    suspend fun togglePinNota(token: String, id: String): NotaResponse
    suspend fun convertirNota(token: String, id: String): ConvertirResponse

    // Buzón ("preguntas")
    suspend fun listPreguntas(token: String): PreguntasResponse
    suspend fun setPreguntaAnswered(token: String, id: String, answered: Boolean)
    suspend fun deletePregunta(token: String, id: String)

    // Notificaciones
    suspend fun listNotificaciones(token: String): NotificacionesResponse
    suspend fun marcarNotificacionesLeidas(token: String)

    // Producción
    suspend fun listProduccion(token: String): ProduccionResponse
    suspend fun createEpisodio(token: String, input: ProduccionCreateInput): EpisodioResponse
    suspend fun updateEpisodio(token: String, id: String, input: ProduccionUpdateInput): EpisodioResponse
    suspend fun deleteEpisodio(token: String, id: String)
    suspend fun listEquipo(token: String): EquipoResponse

    // Reuniones
    suspend fun listReuniones(token: String): ReunionesResponse
    suspend fun createReunion(token: String, input: ReunionInput): ReunionResponse
    suspend fun deleteReunion(token: String, id: String)
}

class TeamApiService : ITeamApiService {
    private val client = ApiClient.httpClient

    private suspend inline fun <reified T> request(
        method: HttpMethod,
        path: String,
        token: String,
        body: Any? = null
    ): T {
        val url = "${AuthConfig.API_URL}$path"
        Logger.d("TeamApi", "${method.value} $url")
        return try {
            val response = client.request(url) {
                this.method = method
                header(HttpHeaders.Authorization, "Bearer $token")
                if (body != null) setBody(body)
            }
            val bodyText = response.bodyAsText()
            Logger.d("TeamApi", "RESPONSE ${response.status.value}: ${bodyText.take(300)}")
            if (!response.status.isSuccess()) {
                throw RuntimeException(parseError(response.status.value, bodyText))
            }
            response.body()
        } catch (e: Exception) {
            Logger.d("TeamApi", "EXCEPTION: ${e::class.simpleName}: ${e.message}")
            throw e.toNetworkException()
        }
    }

    private fun parseError(status: Int, bodyText: String): String {
        // El backend responde { "error": "..." } — lo mostramos directo si viene.
        val error = Regex("\"error\"\\s*:\\s*\"([^\"]+)\"").find(bodyText)
            ?.groupValues?.get(1) ?: return "HTTP $status"
        // El gate de Definición de Hecho agrega { "faltantes": ["Campo", ...] }.
        val faltantes = Regex("\"faltantes\"\\s*:\\s*\\[([^\\]]*)]").find(bodyText)
            ?.groupValues?.get(1)
            ?.split(",")
            ?.map { it.trim().removeSurrounding("\"") }
            ?.filter { it.isNotBlank() }
        return if (faltantes.isNullOrEmpty()) error else "$error Faltan: ${faltantes.joinToString(", ")}"
    }

    // ── Ideas ──
    override suspend fun listNotas(token: String): NotasResponse =
        request(HttpMethod.Get, "/notas", token)

    override suspend fun createNota(token: String, input: NotaInput): NotaResponse =
        request(HttpMethod.Post, "/notas", token, input)

    override suspend fun deleteNota(token: String, id: String) =
        request<Unit>(HttpMethod.Delete, "/notas/$id", token)

    override suspend fun reaccionarNota(token: String, id: String, emoji: String): NotaResponse =
        request(HttpMethod.Post, "/notas/$id/reaccion", token, ReaccionInput(emoji))

    override suspend fun comentarNota(token: String, id: String, texto: String): NotaResponse =
        request(HttpMethod.Post, "/notas/$id/comentario", token, ComentarioInput(texto))

    override suspend fun borrarComentario(token: String, id: String, comentarioId: String): NotaResponse =
        request(HttpMethod.Delete, "/notas/$id/comentario/$comentarioId", token)

    override suspend fun setNotaEstado(token: String, id: String, estado: String): NotaResponse =
        request(HttpMethod.Put, "/notas/$id/estado", token, EstadoInput(estado))

    override suspend fun togglePinNota(token: String, id: String): NotaResponse =
        request(HttpMethod.Put, "/notas/$id/pin", token)

    override suspend fun convertirNota(token: String, id: String): ConvertirResponse =
        request(HttpMethod.Post, "/notas/$id/convertir", token)

    // ── Buzón ──
    override suspend fun listPreguntas(token: String): PreguntasResponse =
        request(HttpMethod.Get, "/preguntas", token)

    override suspend fun setPreguntaAnswered(token: String, id: String, answered: Boolean) =
        request<Unit>(HttpMethod.Put, "/preguntas/$id", token, AnsweredInput(answered))

    override suspend fun deletePregunta(token: String, id: String) =
        request<Unit>(HttpMethod.Delete, "/preguntas/$id", token)

    // ── Notificaciones ──
    override suspend fun listNotificaciones(token: String): NotificacionesResponse =
        request(HttpMethod.Get, "/notificaciones", token)

    override suspend fun marcarNotificacionesLeidas(token: String) =
        request<Unit>(HttpMethod.Post, "/notificaciones/leer", token)

    // ── Producción ──
    override suspend fun listProduccion(token: String): ProduccionResponse =
        request(HttpMethod.Get, "/produccion", token)

    override suspend fun createEpisodio(token: String, input: ProduccionCreateInput): EpisodioResponse =
        request(HttpMethod.Post, "/produccion", token, input)

    override suspend fun updateEpisodio(token: String, id: String, input: ProduccionUpdateInput): EpisodioResponse =
        request(HttpMethod.Put, "/produccion/$id", token, input)

    override suspend fun deleteEpisodio(token: String, id: String) =
        request<Unit>(HttpMethod.Delete, "/produccion/$id", token)

    override suspend fun listEquipo(token: String): EquipoResponse =
        request(HttpMethod.Get, "/equipo", token)

    // ── Reuniones ──
    override suspend fun listReuniones(token: String): ReunionesResponse =
        request(HttpMethod.Get, "/reuniones", token)

    override suspend fun createReunion(token: String, input: ReunionInput): ReunionResponse =
        request(HttpMethod.Post, "/reuniones", token, input)

    override suspend fun deleteReunion(token: String, id: String) =
        request<Unit>(HttpMethod.Delete, "/reuniones/$id", token)
}

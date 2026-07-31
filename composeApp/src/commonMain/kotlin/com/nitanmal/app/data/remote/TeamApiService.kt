package com.nitanmal.app.data.remote

import com.nitanmal.app.core.logger.Logger
import com.nitanmal.app.data.remote.model.AnsweredInput
import com.nitanmal.app.data.remote.model.ComentarioInput
import com.nitanmal.app.data.remote.model.EstadoInput
import com.nitanmal.app.data.remote.model.NotaInput
import com.nitanmal.app.data.remote.model.NotaResponse
import com.nitanmal.app.data.remote.model.NotasResponse
import com.nitanmal.app.data.remote.model.PreguntasResponse
import com.nitanmal.app.data.remote.model.ReaccionInput
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

    // Buzón ("preguntas")
    suspend fun listPreguntas(token: String): PreguntasResponse
    suspend fun setPreguntaAnswered(token: String, id: String, answered: Boolean)
    suspend fun deletePregunta(token: String, id: String)
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
        val match = Regex("\"error\"\\s*:\\s*\"([^\"]+)\"").find(bodyText)
        return match?.groupValues?.get(1) ?: "HTTP $status"
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

    // ── Buzón ──
    override suspend fun listPreguntas(token: String): PreguntasResponse =
        request(HttpMethod.Get, "/preguntas", token)

    override suspend fun setPreguntaAnswered(token: String, id: String, answered: Boolean) =
        request<Unit>(HttpMethod.Put, "/preguntas/$id", token, AnsweredInput(answered))

    override suspend fun deletePregunta(token: String, id: String) =
        request<Unit>(HttpMethod.Delete, "/preguntas/$id", token)
}

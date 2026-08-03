package com.nitanmal.app.data.remote

import com.nitanmal.app.core.logger.Logger
import com.nitanmal.app.domain.model.Descarga
import com.nitanmal.app.domain.model.Encuesta
import com.nitanmal.app.domain.model.EpisodioFan
import com.nitanmal.app.domain.model.Evento
import com.nitanmal.app.domain.model.LiveState
import com.nitanmal.app.domain.model.Sorteo
import com.nitanmal.app.domain.model.SorteoPublico
import com.nitanmal.app.domain.model.Sugerencia
import com.nitanmal.app.domain.model.ZonaData
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

@Serializable
data class LiveResponse(
    val live: LiveState? = null,
    val sorteos: List<SorteoPublico>? = null,
    val error: String? = null
)

@Serializable
data class EventosResponse(val eventos: List<Evento> = emptyList(), val error: String? = null)

@Serializable
data class EpisodiosFanResponse(val episodios: List<EpisodioFan> = emptyList(), val error: String? = null)

@Serializable
data class DescargasResponse(val descargas: List<Descarga> = emptyList(), val error: String? = null)

@Serializable
data class VotoSugerenciaResponse(val votos: Int = 0, val miVoto: Boolean = false)

@Serializable
data class ParticiparResponse(
    val participa: Boolean = false,
    val participantes: Int = 0,
    val chances: Int = 0
)

interface IFanApiService {
    // Rutas públicas (sin token)
    suspend fun getLive(): LiveResponse
    suspend fun listEventos(): List<Evento>
    suspend fun listEpisodios(): List<EpisodioFan>

    // Con sesión
    suspend fun listDescargas(token: String): List<Descarga>
    suspend fun getZona(token: String): ZonaData
    suspend fun sugerir(token: String, tipo: String, texto: String)
    suspend fun votarSugerencia(token: String, id: String): VotoSugerenciaResponse
    suspend fun participarSorteo(token: String, id: String): ParticiparResponse
    suspend fun votarEncuesta(token: String, id: String, opcionId: String)

    /** Buzón del público: cualquier usuario con sesión puede enviar. */
    suspend fun crearPregunta(token: String, contenido: String)

    // Admin
    suspend fun setLive(token: String, live: LiveState): LiveState
    suspend fun cerrarSorteo(token: String, id: String)
    suspend fun elegirGanador(token: String, id: String): GanadorResponse
}

@Serializable
data class Ganador(val nombre: String = "", val email: String = "")

@Serializable
data class GanadorResponse(val ganador: Ganador? = null, val error: String? = null)

@Serializable
data class PreguntaFanInput(val contenido: String)

class FanApiService : IFanApiService {
    private val client = ApiClient.httpClient

    private suspend inline fun <reified T> request(
        method: HttpMethod,
        path: String,
        token: String? = null,
        body: Any? = null
    ): T {
        val url = "${AuthConfig.API_URL}$path"
        Logger.d("FanApi", "${method.value} $url ${if (token == null) "(público)" else ""}")
        return try {
            val response = client.request(url) {
                this.method = method
                if (token != null) header(HttpHeaders.Authorization, "Bearer $token")
                when (body) {
                    null -> Unit
                    // JsonObject no resuelve serializer vía Any: lo mandamos como texto JSON.
                    is JsonObject -> {
                        contentType(ContentType.Application.Json)
                        setBody(body.toString())
                    }
                    else -> setBody(body)
                }
            }
            val bodyText = response.bodyAsText()
            Logger.d("FanApi", "RESPONSE ${response.status.value}: ${bodyText.take(200)}")
            if (!response.status.isSuccess()) {
                val error = Regex("\"error\"\\s*:\\s*\"([^\"]+)\"").find(bodyText)
                    ?.groupValues?.get(1) ?: "HTTP ${response.status.value}"
                throw RuntimeException(error)
            }
            response.body()
        } catch (e: Exception) {
            Logger.d("FanApi", "EXCEPTION: ${e::class.simpleName}: ${e.message}")
            throw e.toNetworkException()
        }
    }

    private fun zonaBody(accion: String, vararg extras: Pair<String, String>): JsonObject =
        buildJsonObject {
            put("accion", JsonPrimitive(accion))
            extras.forEach { (k, v) -> put(k, JsonPrimitive(v)) }
        }

    override suspend fun getLive(): LiveResponse =
        request(HttpMethod.Get, "/live")

    override suspend fun listEventos(): List<Evento> =
        request<EventosResponse>(HttpMethod.Get, "/eventos").eventos

    override suspend fun listEpisodios(): List<EpisodioFan> =
        request<EpisodiosFanResponse>(HttpMethod.Get, "/episodios").episodios

    override suspend fun listDescargas(token: String): List<Descarga> =
        request<DescargasResponse>(HttpMethod.Get, "/descargas", token).descargas

    override suspend fun getZona(token: String): ZonaData =
        request(HttpMethod.Post, "/zona", token, zonaBody("list"))

    override suspend fun sugerir(token: String, tipo: String, texto: String) =
        request<Unit>(HttpMethod.Post, "/zona", token, zonaBody("sugerir", "tipo" to tipo, "texto" to texto))

    override suspend fun votarSugerencia(token: String, id: String): VotoSugerenciaResponse =
        request(HttpMethod.Post, "/zona", token, zonaBody("votar", "id" to id))

    override suspend fun participarSorteo(token: String, id: String): ParticiparResponse =
        request(HttpMethod.Post, "/zona", token, zonaBody("participar", "id" to id))

    override suspend fun votarEncuesta(token: String, id: String, opcionId: String) =
        request<Unit>(HttpMethod.Post, "/zona", token, zonaBody("encuesta_votar", "id" to id, "opcionId" to opcionId))

    override suspend fun crearPregunta(token: String, contenido: String) =
        request<Unit>(HttpMethod.Post, "/preguntas", token, PreguntaFanInput(contenido))

    // ── Admin ──
    override suspend fun setLive(token: String, live: LiveState): LiveState =
        request<LiveResponse>(HttpMethod.Put, "/live", token, live).live ?: live

    override suspend fun cerrarSorteo(token: String, id: String) =
        request<Unit>(HttpMethod.Post, "/zona", token, zonaBody("sorteo_cerrar", "id" to id))

    override suspend fun elegirGanador(token: String, id: String): GanadorResponse =
        request(HttpMethod.Post, "/zona", token, zonaBody("sorteo_ganador", "id" to id))
}

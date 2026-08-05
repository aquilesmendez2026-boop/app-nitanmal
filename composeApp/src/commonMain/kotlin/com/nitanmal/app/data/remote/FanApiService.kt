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
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
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
    suspend fun getVersion(): VersionResponse
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
    suspend fun crearEvento(token: String, input: EventoInput): Evento
    suspend fun borrarEvento(token: String, id: String)
    suspend fun crearEpisodio(token: String, input: EpisodioInput): EpisodioFan
    suspend fun editarEpisodio(token: String, id: String, input: EpisodioInput): EpisodioFan
    suspend fun borrarEpisodio(token: String, id: String)
    suspend fun crearSorteo(token: String, input: SorteoInput)
    suspend fun crearEncuesta(token: String, input: EncuestaInput)
    suspend fun cerrarEncuesta(token: String, id: String)
    suspend fun editarSorteo(token: String, input: SorteoEditInput)
    suspend fun borrarSorteo(token: String, id: String)
    suspend fun editarEncuesta(token: String, id: String, pregunta: String)
    suspend fun borrarEncuesta(token: String, id: String)
}

/** Cuerpo de `POST /zona` con accion=sorteo_editar. */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SorteoEditInput(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) val accion: String = "sorteo_editar",
    val id: String,
    val titulo: String,
    val premio: String = "",
    val comoParticipar: String = "",
    val fecha: String = ""
)

/** Cuerpo de `POST /zona` con accion=encuesta_crear. */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class EncuestaInput(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) val accion: String = "encuesta_crear",
    val pregunta: String,
    val tipo: String = "si_no",
    val opciones: List<String> = emptyList()
)

@Serializable
data class EventoInput(
    val date: String,
    val time: String,
    val title: String,
    val type: String,
    val description: String = "",
    val premium: Boolean = false
)

@Serializable
data class EventoResponse(val evento: Evento? = null, val error: String? = null)

@Serializable
data class LinksInput(val youtube: String = "", val spotify: String = "", val apple: String = "")

@Serializable
data class EpisodioInput(
    val number: Int,
    val title: String,
    val description: String = "",
    val showNotes: String = "",
    val duration: String = "",
    val date: String = "",
    val premium: Boolean = false,
    val links: LinksInput = LinksInput()
)

@Serializable
data class EpisodioAdminResponse(val episodio: EpisodioFan? = null, val error: String? = null)

/** Cuerpo de `POST /zona` con accion=sorteo_crear. */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SorteoInput(
    // Sin EncodeDefault el campo se omite del JSON y el backend responde "Acción inválida".
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) val accion: String = "sorteo_crear",
    val titulo: String,
    val premio: String = "",
    val comoParticipar: String = "",
    val fecha: String = "",
    val enlace: String = ""
)

@Serializable
data class VersionPlataforma(
    val versionCode: Int = 0,
    val versionName: String = "",
    val minVersionCode: Int = 0,
    val url: String = "",
    val notas: String = ""
)

@Serializable
data class VersionResponse(
    val android: VersionPlataforma = VersionPlataforma(),
    val ios: VersionPlataforma = VersionPlataforma()
)

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

    override suspend fun getVersion(): VersionResponse =
        request(HttpMethod.Get, "/version")

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

    override suspend fun crearEvento(token: String, input: EventoInput): Evento =
        request<EventoResponse>(HttpMethod.Post, "/eventos", token, input).evento
            ?: throw RuntimeException("El backend no devolvió el evento")

    override suspend fun borrarEvento(token: String, id: String) =
        request<Unit>(HttpMethod.Delete, "/eventos/$id", token)

    override suspend fun crearEpisodio(token: String, input: EpisodioInput): EpisodioFan =
        request<EpisodioAdminResponse>(HttpMethod.Post, "/episodios", token, input).episodio
            ?: throw RuntimeException("El backend no devolvió el episodio")

    override suspend fun editarEpisodio(token: String, id: String, input: EpisodioInput): EpisodioFan =
        request<EpisodioAdminResponse>(HttpMethod.Put, "/episodios/$id", token, input).episodio
            ?: throw RuntimeException("El backend no devolvió el episodio")

    override suspend fun borrarEpisodio(token: String, id: String) =
        request<Unit>(HttpMethod.Delete, "/episodios/$id", token)

    override suspend fun crearSorteo(token: String, input: SorteoInput) =
        request<Unit>(HttpMethod.Post, "/zona", token, input)

    override suspend fun crearEncuesta(token: String, input: EncuestaInput) =
        request<Unit>(HttpMethod.Post, "/zona", token, input)

    override suspend fun cerrarEncuesta(token: String, id: String) =
        request<Unit>(HttpMethod.Post, "/zona", token, zonaBody("encuesta_cerrar", "id" to id))

    override suspend fun editarSorteo(token: String, input: SorteoEditInput) =
        request<Unit>(HttpMethod.Post, "/zona", token, input)

    override suspend fun borrarSorteo(token: String, id: String) =
        request<Unit>(HttpMethod.Post, "/zona", token, zonaBody("sorteo_borrar", "id" to id))

    override suspend fun editarEncuesta(token: String, id: String, pregunta: String) =
        request<Unit>(
            HttpMethod.Post, "/zona", token,
            zonaBody("encuesta_editar", "id" to id, "pregunta" to pregunta)
        )

    override suspend fun borrarEncuesta(token: String, id: String) =
        request<Unit>(HttpMethod.Post, "/zona", token, zonaBody("encuesta_borrar", "id" to id))

    override suspend fun elegirGanador(token: String, id: String): GanadorResponse =
        request(HttpMethod.Post, "/zona", token, zonaBody("sorteo_ganador", "id" to id))
}

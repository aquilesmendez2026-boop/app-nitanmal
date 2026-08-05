package com.nitanmal.app.domain.repository

import com.nitanmal.app.data.remote.LiveResponse
import com.nitanmal.app.data.remote.ParticiparResponse
import com.nitanmal.app.data.remote.VotoSugerenciaResponse
import com.nitanmal.app.domain.model.Descarga
import com.nitanmal.app.domain.model.EpisodioFan
import com.nitanmal.app.domain.model.Evento
import com.nitanmal.app.domain.model.ZonaData

interface FanRepository {
    // Público
    suspend fun getVersion(): Result<com.nitanmal.app.data.remote.VersionResponse>
    suspend fun getLive(): Result<LiveResponse>
    suspend fun listEventos(): Result<List<Evento>>
    suspend fun listEpisodios(): Result<List<EpisodioFan>>

    // Con sesión
    suspend fun listDescargas(): Result<List<Descarga>>
    suspend fun getZona(): Result<ZonaData>
    suspend fun sugerir(tipo: String, texto: String): Result<Unit>
    suspend fun votarSugerencia(id: String): Result<VotoSugerenciaResponse>
    suspend fun participarSorteo(id: String): Result<ParticiparResponse>
    suspend fun votarEncuesta(id: String, opcionId: String): Result<Unit>
    suspend fun enviarPregunta(contenido: String): Result<Unit>

    // Admin
    suspend fun setLive(live: com.nitanmal.app.domain.model.LiveState): Result<com.nitanmal.app.domain.model.LiveState>
    suspend fun cerrarSorteo(id: String): Result<Unit>
    suspend fun elegirGanador(id: String): Result<com.nitanmal.app.data.remote.Ganador?>

    // Admin: shows, episodios y sorteos
    suspend fun crearEvento(input: com.nitanmal.app.data.remote.EventoInput): Result<Evento>
    suspend fun borrarEvento(id: String): Result<Unit>
    suspend fun crearEpisodio(input: com.nitanmal.app.data.remote.EpisodioInput): Result<EpisodioFan>
    suspend fun editarEpisodio(id: String, input: com.nitanmal.app.data.remote.EpisodioInput): Result<EpisodioFan>
    suspend fun borrarEpisodio(id: String): Result<Unit>
    suspend fun crearSorteo(input: com.nitanmal.app.data.remote.SorteoInput): Result<Unit>
    suspend fun crearEncuesta(input: com.nitanmal.app.data.remote.EncuestaInput): Result<Unit>
    suspend fun cerrarEncuesta(id: String): Result<Unit>
    suspend fun editarSorteo(input: com.nitanmal.app.data.remote.SorteoEditInput): Result<Unit>
    suspend fun borrarSorteo(id: String): Result<Unit>
    suspend fun editarEncuesta(id: String, pregunta: String): Result<Unit>
    suspend fun borrarEncuesta(id: String): Result<Unit>
}

package com.nitanmal.app.data.repository

import com.nitanmal.app.data.remote.FanApiService
import com.nitanmal.app.data.remote.IFanApiService
import com.nitanmal.app.data.remote.LiveResponse
import com.nitanmal.app.data.remote.ParticiparResponse
import com.nitanmal.app.data.remote.VotoSugerenciaResponse
import com.nitanmal.app.domain.auth.PlatformAuth
import com.nitanmal.app.domain.model.Descarga
import com.nitanmal.app.domain.model.EpisodioFan
import com.nitanmal.app.domain.model.Evento
import com.nitanmal.app.domain.model.ZonaData
import com.nitanmal.app.domain.repository.FanRepository

class FanRepositoryImpl(
    private val platformAuth: PlatformAuth,
    private val apiService: IFanApiService = FanApiService()
) : FanRepository {

    private suspend fun token(): String =
        platformAuth.getFirebaseIdToken()
            ?: throw IllegalStateException("No hay sesión activa")

    private suspend fun <T> call(block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getVersion(): Result<com.nitanmal.app.data.remote.VersionResponse> =
        try {
            Result.success(apiService.getVersion())
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getLive(): Result<LiveResponse> =
        call { apiService.getLive() }

    override suspend fun listEventos(): Result<List<Evento>> =
        call { apiService.listEventos() }

    override suspend fun listEpisodios(): Result<List<EpisodioFan>> =
        call { apiService.listEpisodios() }

    override suspend fun listDescargas(): Result<List<Descarga>> =
        call { apiService.listDescargas(token()) }

    override suspend fun getZona(): Result<ZonaData> =
        call { apiService.getZona(token()) }

    override suspend fun sugerir(tipo: String, texto: String): Result<Unit> =
        call { apiService.sugerir(token(), tipo, texto) }

    override suspend fun votarSugerencia(id: String): Result<VotoSugerenciaResponse> =
        call { apiService.votarSugerencia(token(), id) }

    override suspend fun participarSorteo(id: String): Result<ParticiparResponse> =
        call { apiService.participarSorteo(token(), id) }

    override suspend fun votarEncuesta(id: String, opcionId: String): Result<Unit> =
        call { apiService.votarEncuesta(token(), id, opcionId) }

    override suspend fun enviarPregunta(contenido: String): Result<Unit> =
        call { apiService.crearPregunta(token(), contenido) }

    // ── Admin ──
    override suspend fun setLive(live: com.nitanmal.app.domain.model.LiveState): Result<com.nitanmal.app.domain.model.LiveState> =
        call { apiService.setLive(token(), live) }

    override suspend fun cerrarSorteo(id: String): Result<Unit> =
        call { apiService.cerrarSorteo(token(), id) }

    override suspend fun crearEvento(input: com.nitanmal.app.data.remote.EventoInput) =
        call { apiService.crearEvento(token(), input) }

    override suspend fun borrarEvento(id: String): Result<Unit> =
        call { apiService.borrarEvento(token(), id) }

    override suspend fun crearEpisodio(input: com.nitanmal.app.data.remote.EpisodioInput) =
        call { apiService.crearEpisodio(token(), input) }

    override suspend fun editarEpisodio(id: String, input: com.nitanmal.app.data.remote.EpisodioInput) =
        call { apiService.editarEpisodio(token(), id, input) }

    override suspend fun borrarEpisodio(id: String): Result<Unit> =
        call { apiService.borrarEpisodio(token(), id) }

    override suspend fun crearSorteo(input: com.nitanmal.app.data.remote.SorteoInput): Result<Unit> =
        call { apiService.crearSorteo(token(), input) }

    override suspend fun crearEncuesta(input: com.nitanmal.app.data.remote.EncuestaInput): Result<Unit> =
        call { apiService.crearEncuesta(token(), input) }

    override suspend fun cerrarEncuesta(id: String): Result<Unit> =
        call { apiService.cerrarEncuesta(token(), id) }

    override suspend fun editarSorteo(input: com.nitanmal.app.data.remote.SorteoEditInput): Result<Unit> =
        call { apiService.editarSorteo(token(), input) }

    override suspend fun borrarSorteo(id: String): Result<Unit> =
        call { apiService.borrarSorteo(token(), id) }

    override suspend fun editarEncuesta(id: String, pregunta: String): Result<Unit> =
        call { apiService.editarEncuesta(token(), id, pregunta) }

    override suspend fun borrarEncuesta(id: String): Result<Unit> =
        call { apiService.borrarEncuesta(token(), id) }

    override suspend fun elegirGanador(id: String): Result<com.nitanmal.app.data.remote.Ganador?> =
        call { apiService.elegirGanador(token(), id).ganador }
}

package com.nitanmal.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitanmal.app.data.remote.Ganador
import com.nitanmal.app.domain.model.Canal
import com.nitanmal.app.domain.model.Descarga
import com.nitanmal.app.domain.model.Encuesta
import com.nitanmal.app.domain.model.EpisodioFan
import com.nitanmal.app.domain.model.Evento
import com.nitanmal.app.domain.model.LiveState
import com.nitanmal.app.domain.model.Sorteo
import com.nitanmal.app.domain.model.Sugerencia
import com.nitanmal.app.domain.model.UsuarioAdmin
import com.nitanmal.app.domain.repository.FanRepository
import com.nitanmal.app.domain.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AdminUiState(
    val isLoading: Boolean = false,
    val live: LiveState? = null,
    val sorteos: List<Sorteo> = emptyList(),
    val encuestas: List<Encuesta> = emptyList(),
    val sugerencias: List<Sugerencia> = emptyList(),
    val eventos: List<Evento> = emptyList(),
    val episodios: List<EpisodioFan> = emptyList(),
    val descargas: List<Descarga> = emptyList(),
    val canales: List<Canal> = emptyList(),
    val usuarios: List<UsuarioAdmin> = emptyList(),
    /** Respaldo cuando no se es superadmin: /usuarios da 403 pero /equipo no. */
    val equipo: List<com.nitanmal.app.domain.model.MiembroEquipo> = emptyList(),
    val preguntasPendientes: Int = 0,
    val ganadores: Map<String, Ganador> = emptyMap(),
    val isSavingLive: Boolean = false,
    val isGuardando: Boolean = false,
    val error: String? = null,
    val info: String? = null
) {
    val sorteosActivos: Int get() = sorteos.count { it.activo }
    val participantes: Int get() = sorteos.sumOf { it.participantes }
    val encuestasActivas: Int get() = encuestas.count { it.activa }
    val votos: Int get() = encuestas.sumOf { it.total }
    val enEquipo: Int
        get() = if (usuarios.isNotEmpty())
            usuarios.count { it.role == "participante" || it.role == "admin" || it.role == "superadmin" }
        else equipo.size
}

/**
 * Panel admin: espejo del panel web (resumen, en vivo, shows, episodios,
 * descargas, sorteo, encuestas, canales y usuarios).
 */
class AdminViewModel(
    private val repository: FanRepository,
    private val teamRepository: TeamRepository? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            repository.getLive().onSuccess { r ->
                _uiState.value = _uiState.value.copy(live = r.live)
            }
            repository.getZona()
                .onSuccess { zona ->
                    _uiState.value = _uiState.value.copy(
                        sorteos = zona.sorteos,
                        encuestas = zona.encuestas,
                        sugerencias = zona.sugerencias
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message ?: "Error al cargar")
                }
            repository.listEventos().onSuccess { eventos ->
                _uiState.value = _uiState.value.copy(eventos = eventos)
            }
            repository.listEpisodios().onSuccess { episodios ->
                _uiState.value = _uiState.value.copy(episodios = episodios)
            }
            repository.listDescargas().onSuccess { descargas ->
                _uiState.value = _uiState.value.copy(descargas = descargas)
            }
            teamRepository?.let { team ->
                team.listSocials().onSuccess { canales ->
                    _uiState.value = _uiState.value.copy(canales = canales)
                }
                team.listEquipo().onSuccess { equipo ->
                    _uiState.value = _uiState.value.copy(equipo = equipo)
                }
                team.listPreguntas().onSuccess { preguntas ->
                    _uiState.value = _uiState.value.copy(
                        preguntasPendientes = preguntas.count { !it.answered }
                    )
                }
                // Solo el superadmin puede listar usuarios; si da 403 se ignora.
                team.listUsuarios().onSuccess { usuarios ->
                    _uiState.value = _uiState.value.copy(usuarios = usuarios)
                }
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun guardarLive(live: LiveState) {
        _uiState.value = _uiState.value.copy(isSavingLive = true, error = null)
        viewModelScope.launch {
            repository.setLive(live)
                .onSuccess { actualizado ->
                    _uiState.value = _uiState.value.copy(
                        isSavingLive = false,
                        live = actualizado,
                        info = if (actualizado.isLive) "🔴 ¡Estás en vivo!" else "Transmisión apagada"
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isSavingLive = false,
                        error = e.message ?: "No se pudo guardar"
                    )
                }
        }
    }

    fun toggleSorteo(id: String) {
        viewModelScope.launch {
            repository.cerrarSorteo(id)
                .onSuccess { load() }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message ?: "No se pudo")
                }
        }
    }

    fun elegirGanador(id: String) {
        viewModelScope.launch {
            repository.elegirGanador(id)
                .onSuccess { ganador ->
                    if (ganador != null) {
                        _uiState.value = _uiState.value.copy(
                            ganadores = _uiState.value.ganadores + (id to ganador),
                            info = "🏆 Ganador: ${ganador.nombre}"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message ?: "No se pudo elegir")
                }
        }
    }

    // ── Shows ──
    fun crearShow(date: String, time: String, title: String, tipo: String, descripcion: String) {
        _uiState.value = _uiState.value.copy(isGuardando = true, error = null)
        viewModelScope.launch {
            repository.crearEvento(
                com.nitanmal.app.data.remote.EventoInput(
                    date = date, time = time, title = title, type = tipo, description = descripcion
                )
            )
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isGuardando = false, info = "Show creado ✅")
                    load()
                }
                .onFailure { e -> falla(e, "No se pudo crear el show") }
        }
    }

    fun borrarShow(id: String) {
        viewModelScope.launch {
            repository.borrarEvento(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(info = "Show eliminado")
                    load()
                }
                .onFailure { e -> falla(e, "No se pudo eliminar") }
        }
    }

    // ── Episodios ──
    fun guardarEpisodio(
        id: String?,
        number: Int,
        title: String,
        description: String,
        duration: String,
        date: String,
        premium: Boolean,
        youtube: String,
        spotify: String
    ) {
        _uiState.value = _uiState.value.copy(isGuardando = true, error = null)
        val input = com.nitanmal.app.data.remote.EpisodioInput(
            number = number, title = title, description = description,
            duration = duration, date = date, premium = premium,
            links = com.nitanmal.app.data.remote.LinksInput(youtube = youtube, spotify = spotify)
        )
        viewModelScope.launch {
            val res = if (id == null) repository.crearEpisodio(input)
            else repository.editarEpisodio(id, input)
            res
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isGuardando = false,
                        info = if (id == null) "Episodio creado ✅" else "Episodio actualizado ✅"
                    )
                    load()
                }
                .onFailure { e -> falla(e, "No se pudo guardar el episodio") }
        }
    }

    fun borrarEpisodio(id: String) {
        viewModelScope.launch {
            repository.borrarEpisodio(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(info = "Episodio eliminado")
                    load()
                }
                .onFailure { e -> falla(e, "No se pudo eliminar") }
        }
    }

    // ── Sorteos ──
    fun crearSorteo(titulo: String, premio: String, comoParticipar: String, fecha: String) {
        _uiState.value = _uiState.value.copy(isGuardando = true, error = null)
        viewModelScope.launch {
            repository.crearSorteo(
                com.nitanmal.app.data.remote.SorteoInput(
                    titulo = titulo, premio = premio,
                    comoParticipar = comoParticipar, fecha = fecha
                )
            )
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isGuardando = false, info = "Sorteo creado 🎁")
                    load()
                }
                .onFailure { e -> falla(e, "No se pudo crear el sorteo") }
        }
    }

    // ── Encuestas ──
    fun crearEncuesta(pregunta: String, tipo: String, opciones: List<String>) {
        _uiState.value = _uiState.value.copy(isGuardando = true, error = null)
        viewModelScope.launch {
            repository.crearEncuesta(
                com.nitanmal.app.data.remote.EncuestaInput(
                    pregunta = pregunta, tipo = tipo, opciones = opciones
                )
            )
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isGuardando = false, info = "Encuesta creada 🗳️")
                    load()
                }
                .onFailure { e -> falla(e, "No se pudo crear la encuesta") }
        }
    }

    fun toggleEncuesta(id: String) {
        viewModelScope.launch {
            repository.cerrarEncuesta(id)
                .onSuccess { load() }
                .onFailure { e -> falla(e, "No se pudo actualizar la encuesta") }
        }
    }

    fun editarSorteo(id: String, titulo: String, premio: String, comoParticipar: String, fecha: String) {
        _uiState.value = _uiState.value.copy(isGuardando = true, error = null)
        viewModelScope.launch {
            repository.editarSorteo(
                com.nitanmal.app.data.remote.SorteoEditInput(
                    id = id, titulo = titulo, premio = premio,
                    comoParticipar = comoParticipar, fecha = fecha
                )
            )
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isGuardando = false, info = "Sorteo actualizado ✅")
                    load()
                }
                .onFailure { e -> falla(e, "No se pudo editar el sorteo") }
        }
    }

    fun borrarSorteo(id: String) {
        viewModelScope.launch {
            repository.borrarSorteo(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(info = "Sorteo eliminado")
                    load()
                }
                .onFailure { e -> falla(e, "No se pudo eliminar el sorteo") }
        }
    }

    fun editarEncuesta(id: String, pregunta: String) {
        _uiState.value = _uiState.value.copy(isGuardando = true, error = null)
        viewModelScope.launch {
            repository.editarEncuesta(id, pregunta)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isGuardando = false, info = "Encuesta actualizada ✅")
                    load()
                }
                .onFailure { e -> falla(e, "No se pudo editar la encuesta") }
        }
    }

    fun borrarEncuesta(id: String) {
        viewModelScope.launch {
            repository.borrarEncuesta(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(info = "Encuesta eliminada")
                    load()
                }
                .onFailure { e -> falla(e, "No se pudo eliminar la encuesta") }
        }
    }

    // ── Canales (solo superadmin) ──
    fun guardarCanal(canal: Canal) {
        val actualizados = _uiState.value.canales.map { if (it.plataforma == canal.plataforma) canal else it }
        _uiState.value = _uiState.value.copy(isGuardando = true, error = null)
        viewModelScope.launch {
            teamRepository?.guardarSocials(actualizados)
                ?.onSuccess { canales ->
                    _uiState.value = _uiState.value.copy(
                        isGuardando = false, canales = canales, info = "Canal actualizado ✅"
                    )
                }
                ?.onFailure { e -> falla(e, "No se pudo guardar el canal") }
        }
    }

    // ── Usuarios (solo superadmin) ──
    fun cambiarRol(userId: String, role: String) {
        _uiState.value = _uiState.value.copy(isGuardando = true, error = null)
        viewModelScope.launch {
            teamRepository?.cambiarRol(userId, role)
                ?.onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isGuardando = false,
                        usuarios = _uiState.value.usuarios.map {
                            if (it.userId == userId) it.copy(role = role) else it
                        },
                        info = "Rol actualizado ✅"
                    )
                }
                ?.onFailure { e -> falla(e, "No se pudo cambiar el rol") }
        }
    }

    private fun falla(e: Throwable, porDefecto: String) {
        _uiState.value = _uiState.value.copy(
            isGuardando = false,
            isSavingLive = false,
            error = e.message ?: porDefecto
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearInfo() {
        _uiState.value = _uiState.value.copy(info = null)
    }
}

package com.nitanmal.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitanmal.app.core.util.todayIsoDate
import com.nitanmal.app.domain.model.EpisodioFan
import com.nitanmal.app.domain.model.Evento
import com.nitanmal.app.domain.model.LiveState
import com.nitanmal.app.domain.model.SorteoPublico
import com.nitanmal.app.domain.repository.FanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FanUiState(
    val isLoading: Boolean = false,
    val live: LiveState? = null,
    val sorteosPublicos: List<SorteoPublico> = emptyList(),
    val eventos: List<Evento> = emptyList(),
    val episodios: List<EpisodioFan> = emptyList(),
    val error: String? = null,
    val info: String? = null,
    val isEnviandoPregunta: Boolean = false,
    val showPreguntaSheet: Boolean = false
) {
    val proximosEventos: List<Evento>
        get() = eventos.filter { it.date >= todayIsoDate() }.sortedBy { "${it.date}${it.time}" }

    val proximoEvento: Evento? get() = proximosEventos.firstOrNull()

    val recientes: List<EpisodioFan> get() = episodios.take(3)
}

/** Contenido del lado fan: en vivo, horarios, episodios y buzón. */
class FanViewModel(
    private val repository: FanRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FanUiState())
    val uiState: StateFlow<FanUiState> = _uiState

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            repository.getLive().onSuccess { r ->
                _uiState.value = _uiState.value.copy(
                    live = r.live,
                    sorteosPublicos = r.sorteos ?: emptyList()
                )
            }
            repository.listEventos().onSuccess { eventos ->
                _uiState.value = _uiState.value.copy(eventos = eventos)
            }
            repository.listEpisodios()
                .onSuccess { episodios ->
                    _uiState.value = _uiState.value.copy(isLoading = false, episodios = episodios)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar el contenido"
                    )
                }
        }
    }

    fun refreshLive() {
        viewModelScope.launch {
            repository.getLive().onSuccess { r ->
                _uiState.value = _uiState.value.copy(
                    live = r.live,
                    sorteosPublicos = r.sorteos ?: emptyList()
                )
            }
        }
    }

    fun setShowPreguntaSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showPreguntaSheet = show)
    }

    fun enviarPregunta(contenido: String) {
        _uiState.value = _uiState.value.copy(isEnviandoPregunta = true, error = null)
        viewModelScope.launch {
            repository.enviarPregunta(contenido)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isEnviandoPregunta = false,
                        showPreguntaSheet = false,
                        info = "¡Gracias! Recibimos tu mensaje."
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isEnviandoPregunta = false,
                        error = e.message ?: "No se pudo enviar"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearInfo() {
        _uiState.value = _uiState.value.copy(info = null)
    }
}

package com.nitanmal.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitanmal.app.data.remote.Ganador
import com.nitanmal.app.domain.model.LiveState
import com.nitanmal.app.domain.model.Sorteo
import com.nitanmal.app.domain.repository.FanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AdminUiState(
    val isLoading: Boolean = false,
    val live: LiveState? = null,
    val sorteos: List<Sorteo> = emptyList(),
    val ganadores: Map<String, Ganador> = emptyMap(),
    val isSavingLive: Boolean = false,
    val error: String? = null,
    val info: String? = null
)

/** Panel admin móvil: en vivo + sorteos (lo urgente; el CRUD completo queda en la web). */
class AdminViewModel(
    private val repository: FanRepository
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
                    _uiState.value = _uiState.value.copy(isLoading = false, sorteos = zona.sorteos)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar"
                    )
                }
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearInfo() {
        _uiState.value = _uiState.value.copy(info = null)
    }
}

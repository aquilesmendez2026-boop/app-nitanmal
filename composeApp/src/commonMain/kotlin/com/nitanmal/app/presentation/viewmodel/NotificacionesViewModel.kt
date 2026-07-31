package com.nitanmal.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitanmal.app.domain.model.Notificacion
import com.nitanmal.app.domain.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class NotificacionesUiState(
    val isLoading: Boolean = false,
    val notificaciones: List<Notificacion> = emptyList(),
    val error: String? = null
) {
    val noLeidas: Int get() = notificaciones.count { !it.leida }
}

class NotificacionesViewModel(
    private val repository: TeamRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificacionesUiState())
    val uiState: StateFlow<NotificacionesUiState> = _uiState

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            repository.listNotificaciones()
                .onSuccess { lista ->
                    _uiState.value = _uiState.value.copy(isLoading = false, notificaciones = lista)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar notificaciones"
                    )
                }
        }
    }

    /** Marca todas como leídas (optimista: el badge desaparece de inmediato). */
    fun marcarLeidas() {
        if (_uiState.value.noLeidas == 0) return
        _uiState.value = _uiState.value.copy(
            notificaciones = _uiState.value.notificaciones.map { it.copy(leida = true) }
        )
        viewModelScope.launch {
            repository.marcarNotificacionesLeidas()
            // Si falla, el próximo load() restaura el estado real.
        }
    }
}

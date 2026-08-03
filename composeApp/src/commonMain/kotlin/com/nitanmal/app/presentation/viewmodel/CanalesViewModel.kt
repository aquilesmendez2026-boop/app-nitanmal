package com.nitanmal.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitanmal.app.domain.model.Canal
import com.nitanmal.app.domain.model.Metricas
import com.nitanmal.app.domain.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CanalesUiState(
    val isLoading: Boolean = false,
    val canales: List<Canal> = emptyList(),
    val metricas: Metricas? = null,
    val error: String? = null
) {
    val visibles: List<Canal> get() = canales.filter { it.visible }
    val enVivo: List<Canal> get() = visibles.filter { it.enVivo }
}

/** Canales/redes del proyecto + métricas de seguidores. */
class CanalesViewModel(
    private val repository: TeamRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CanalesUiState())
    val uiState: StateFlow<CanalesUiState> = _uiState

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            repository.listSocials()
                .onSuccess { canales ->
                    _uiState.value = _uiState.value.copy(isLoading = false, canales = canales)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar canales"
                    )
                }
        }
    }

    fun loadMetricas() {
        viewModelScope.launch {
            repository.getMetricas()
                .onSuccess { m ->
                    _uiState.value = _uiState.value.copy(metricas = m)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Error al cargar métricas"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

package com.nitanmal.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitanmal.app.core.util.todayIsoDate
import com.nitanmal.app.domain.model.Reunion
import com.nitanmal.app.domain.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ReunionesUiState(
    val isLoading: Boolean = false,
    val reuniones: List<Reunion> = emptyList(),
    val error: String? = null,
    val isCreating: Boolean = false,
    val showCreateSheet: Boolean = false
) {
    private val hoy: String get() = todayIsoDate()

    val proximas: List<Reunion> get() = reuniones.filter { it.date >= hoy }
    val pasadas: List<Reunion> get() = reuniones.filter { it.date < hoy }.reversed()
}

class ReunionesViewModel(
    private val repository: TeamRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReunionesUiState())
    val uiState: StateFlow<ReunionesUiState> = _uiState

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            repository.listReuniones()
                .onSuccess { lista ->
                    _uiState.value = _uiState.value.copy(isLoading = false, reuniones = lista)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar reuniones"
                    )
                }
        }
    }

    fun setShowCreateSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showCreateSheet = show)
    }

    fun create(date: String, time: String, title: String, description: String, lugar: String) {
        _uiState.value = _uiState.value.copy(isCreating = true, error = null)
        viewModelScope.launch {
            repository.createReunion(date, time, title, description, lugar)
                .onSuccess { reunion ->
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        showCreateSheet = false,
                        reuniones = (_uiState.value.reuniones + reunion)
                            .sortedBy { "${it.date}${it.time}" }
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        error = e.message ?: "No se pudo crear la reunión"
                    )
                }
        }
    }

    fun delete(id: String) {
        val previas = _uiState.value.reuniones
        _uiState.value = _uiState.value.copy(reuniones = previas.filterNot { it.id == id })
        viewModelScope.launch {
            repository.deleteReunion(id)
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        reuniones = previas,
                        error = e.message ?: "No se pudo borrar"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

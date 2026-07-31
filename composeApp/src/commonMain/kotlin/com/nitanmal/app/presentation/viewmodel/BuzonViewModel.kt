package com.nitanmal.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitanmal.app.domain.model.Pregunta
import com.nitanmal.app.domain.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class BuzonFilter { TODAS, PENDIENTES, RESPONDIDAS }

data class BuzonUiState(
    val isLoading: Boolean = false,
    val preguntas: List<Pregunta> = emptyList(),
    val filter: BuzonFilter = BuzonFilter.TODAS,
    val error: String? = null
) {
    val filtradas: List<Pregunta>
        get() = when (filter) {
            BuzonFilter.TODAS -> preguntas
            BuzonFilter.PENDIENTES -> preguntas.filterNot { it.answered }
            BuzonFilter.RESPONDIDAS -> preguntas.filter { it.answered }
        }

    val pendientes: Int get() = preguntas.count { !it.answered }
}

class BuzonViewModel(
    private val repository: TeamRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(BuzonUiState())
    val uiState: StateFlow<BuzonUiState> = _uiState

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            repository.listPreguntas()
                .onSuccess { preguntas ->
                    _uiState.value = _uiState.value.copy(isLoading = false, preguntas = preguntas)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar el buzón"
                    )
                }
        }
    }

    fun setFilter(filter: BuzonFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
    }

    fun setAnswered(id: String, answered: Boolean) {
        // Optimista: actualizamos local y revertimos si falla.
        val previas = _uiState.value.preguntas
        _uiState.value = _uiState.value.copy(
            preguntas = previas.map { if (it.id == id) it.copy(answered = answered) else it }
        )
        viewModelScope.launch {
            repository.setPreguntaAnswered(id, answered)
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        preguntas = previas,
                        error = e.message ?: "No se pudo actualizar"
                    )
                }
        }
    }

    fun delete(id: String) {
        val previas = _uiState.value.preguntas
        _uiState.value = _uiState.value.copy(
            preguntas = previas.filterNot { it.id == id }
        )
        viewModelScope.launch {
            repository.deletePregunta(id)
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        preguntas = previas,
                        error = e.message ?: "No se pudo borrar"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

package com.nitanmal.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitanmal.app.data.remote.model.StageDataInput
import com.nitanmal.app.domain.model.Episodio
import com.nitanmal.app.domain.model.MiembroEquipo
import com.nitanmal.app.domain.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProduccionUiState(
    val isLoading: Boolean = false,
    val episodios: List<Episodio> = emptyList(),
    val equipo: List<MiembroEquipo> = emptyList(),
    val error: String? = null,
    val info: String? = null,
    val isCreating: Boolean = false,
    val isSaving: Boolean = false,
    val showCreateSheet: Boolean = false
) {
    /** Más recientes arriba (el backend los ordena ascendente). */
    val ordenados: List<Episodio> get() = episodios.reversed()
}

class ProduccionViewModel(
    private val repository: TeamRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProduccionUiState())
    val uiState: StateFlow<ProduccionUiState> = _uiState

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            repository.listProduccion()
                .onSuccess { lista ->
                    _uiState.value = _uiState.value.copy(isLoading = false, episodios = lista)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar producción"
                    )
                }
            if (_uiState.value.equipo.isEmpty()) {
                repository.listEquipo().onSuccess { equipo ->
                    _uiState.value = _uiState.value.copy(equipo = equipo)
                }
            }
        }
    }

    fun setShowCreateSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showCreateSheet = show)
    }

    fun create(titulo: String, idea: String) {
        _uiState.value = _uiState.value.copy(isCreating = true, error = null)
        viewModelScope.launch {
            repository.createEpisodio(titulo, idea)
                .onSuccess { episodio ->
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        showCreateSheet = false,
                        episodios = _uiState.value.episodios + episodio
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        error = e.message ?: "No se pudo crear el episodio"
                    )
                }
        }
    }

    fun updateStage(id: String, stage: String, data: StageDataInput, onSaved: () -> Unit = {}) {
        _uiState.value = _uiState.value.copy(isSaving = true, error = null)
        viewModelScope.launch {
            repository.updateEpisodioStage(id, stage, data)
                .onSuccess { actualizado ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        episodios = _uiState.value.episodios.map {
                            if (it.id == actualizado.id) actualizado else it
                        },
                        info = "Etapa guardada ✓"
                    )
                    onSaved()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = e.message ?: "No se pudo guardar la etapa"
                    )
                }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            repository.deleteEpisodio(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        episodios = _uiState.value.episodios.filterNot { it.id == id }
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "No se pudo borrar el episodio"
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

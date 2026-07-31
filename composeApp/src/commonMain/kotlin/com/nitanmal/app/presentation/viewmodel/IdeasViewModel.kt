package com.nitanmal.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitanmal.app.domain.model.Nota
import com.nitanmal.app.domain.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class IdeasUiState(
    val isLoading: Boolean = false,
    val notas: List<Nota> = emptyList(),
    val error: String? = null,
    val info: String? = null,
    val isCreating: Boolean = false,
    val showCreateSheet: Boolean = false
) {
    /** Fijadas primero; dentro de cada grupo, más recientes arriba (orden del backend). */
    val ordenadas: List<Nota>
        get() = notas.sortedByDescending { it.pinned }
}

class IdeasViewModel(
    private val repository: TeamRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(IdeasUiState())
    val uiState: StateFlow<IdeasUiState> = _uiState

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            repository.listNotas()
                .onSuccess { notas ->
                    _uiState.value = _uiState.value.copy(isLoading = false, notas = notas)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar las ideas"
                    )
                }
        }
    }

    fun setShowCreateSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showCreateSheet = show)
    }

    fun create(titulo: String, contenido: String, etiquetas: List<String>) {
        _uiState.value = _uiState.value.copy(isCreating = true, error = null)
        viewModelScope.launch {
            repository.createNota(titulo, contenido, etiquetas)
                .onSuccess { nota ->
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        showCreateSheet = false,
                        notas = listOf(nota) + _uiState.value.notas
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        error = e.message ?: "No se pudo crear la idea"
                    )
                }
        }
    }

    fun reaccionar(id: String, emoji: String) = mutate { repository.reaccionarNota(id, emoji) }

    fun comentar(id: String, texto: String) = mutate { repository.comentarNota(id, texto) }

    fun borrarComentario(id: String, comentarioId: String) =
        mutate { repository.borrarComentario(id, comentarioId) }

    fun setEstado(id: String, estado: String) = mutate { repository.setNotaEstado(id, estado) }

    fun togglePin(id: String) = mutate { repository.togglePinNota(id) }

    fun convertir(id: String) {
        viewModelScope.launch {
            repository.convertirNota(id)
                .onSuccess { actualizada ->
                    _uiState.value = _uiState.value.copy(
                        notas = _uiState.value.notas.map {
                            if (it.id == actualizada.id) actualizada else it
                        },
                        info = "Idea convertida en episodio 🎬"
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "No se pudo convertir la idea"
                    )
                }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            repository.deleteNota(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        notas = _uiState.value.notas.filterNot { it.id == id }
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "No se pudo borrar la idea"
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

    /** Ejecuta una mutación que devuelve la nota actualizada y la reemplaza en la lista. */
    private fun mutate(block: suspend () -> Result<Nota>) {
        viewModelScope.launch {
            block()
                .onSuccess { actualizada ->
                    _uiState.value = _uiState.value.copy(
                        notas = _uiState.value.notas.map {
                            if (it.id == actualizada.id) actualizada else it
                        }
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "No se pudo actualizar la idea"
                    )
                }
        }
    }
}

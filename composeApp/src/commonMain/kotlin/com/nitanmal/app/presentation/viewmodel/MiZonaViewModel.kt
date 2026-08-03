package com.nitanmal.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitanmal.app.domain.model.Descarga
import com.nitanmal.app.domain.model.ZonaData
import com.nitanmal.app.domain.repository.FanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MiZonaUiState(
    val isLoading: Boolean = false,
    val zona: ZonaData? = null,
    val descargas: List<Descarga> = emptyList(),
    val error: String? = null,
    val info: String? = null,
    val isSugiriendo: Boolean = false,
    val showSugerirSheet: Boolean = false
)

/** Mi Zona: sorteos, encuestas, sugerencias, referidos y descargas. */
class MiZonaViewModel(
    private val repository: FanRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MiZonaUiState())
    val uiState: StateFlow<MiZonaUiState> = _uiState

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            repository.getZona()
                .onSuccess { zona ->
                    _uiState.value = _uiState.value.copy(isLoading = false, zona = zona)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar Mi Zona"
                    )
                }
            repository.listDescargas().onSuccess { descargas ->
                _uiState.value = _uiState.value.copy(descargas = descargas)
            }
        }
    }

    fun participarSorteo(id: String) {
        viewModelScope.launch {
            repository.participarSorteo(id)
                .onSuccess { r ->
                    val zona = _uiState.value.zona ?: return@onSuccess
                    _uiState.value = _uiState.value.copy(
                        zona = zona.copy(
                            sorteos = zona.sorteos.map {
                                if (it.id == id) it.copy(
                                    participa = r.participa,
                                    participantes = r.participantes,
                                    misChances = r.chances
                                ) else it
                            }
                        ),
                        info = if (r.participa) "¡Estás participando! 🎉" else null
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message ?: "No se pudo participar")
                }
        }
    }

    fun votarEncuesta(id: String, opcionId: String) {
        viewModelScope.launch {
            repository.votarEncuesta(id, opcionId)
                .onSuccess { load() }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message ?: "No se pudo votar")
                }
        }
    }

    fun votarSugerencia(id: String) {
        viewModelScope.launch {
            repository.votarSugerencia(id)
                .onSuccess { r ->
                    val zona = _uiState.value.zona ?: return@onSuccess
                    _uiState.value = _uiState.value.copy(
                        zona = zona.copy(
                            sugerencias = zona.sugerencias.map {
                                if (it.id == id) it.copy(votos = r.votos, miVoto = r.miVoto) else it
                            }
                        )
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message ?: "No se pudo votar")
                }
        }
    }

    fun setShowSugerirSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSugerirSheet = show)
    }

    fun sugerir(tipo: String, texto: String) {
        _uiState.value = _uiState.value.copy(isSugiriendo = true, error = null)
        viewModelScope.launch {
            repository.sugerir(tipo, texto)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSugiriendo = false,
                        showSugerirSheet = false
                    )
                    load()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isSugiriendo = false,
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

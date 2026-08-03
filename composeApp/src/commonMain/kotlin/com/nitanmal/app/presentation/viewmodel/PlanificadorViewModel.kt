package com.nitanmal.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitanmal.app.data.remote.model.PlanificadorInput
import com.nitanmal.app.domain.model.Post
import com.nitanmal.app.domain.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PlanificadorUiState(
    val isLoading: Boolean = false,
    val posts: List<Post> = emptyList(),
    /** null = todos; si no, filtra por estado. */
    val filtro: String? = null,
    val error: String? = null,
    val info: String? = null,
    /** true mientras el agente de IA genera borradores (puede tardar). */
    val isGenerating: Boolean = false,
    val isSaving: Boolean = false,
    val showGenerarSheet: Boolean = false,
    val showCrearSheet: Boolean = false
) {
    val filtrados: List<Post>
        get() = (if (filtro == null) posts else posts.filter { it.estado == filtro })
            .sortedByDescending { it.createdAt ?: "" }
}

class PlanificadorViewModel(
    private val repository: TeamRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlanificadorUiState())
    val uiState: StateFlow<PlanificadorUiState> = _uiState

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            repository.listPlanificador()
                .onSuccess { posts ->
                    _uiState.value = _uiState.value.copy(isLoading = false, posts = posts)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar el planificador"
                    )
                }
        }
    }

    fun setFiltro(estado: String?) {
        _uiState.value = _uiState.value.copy(filtro = estado)
    }

    fun setShowGenerarSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showGenerarSheet = show)
    }

    fun setShowCrearSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showCrearSheet = show)
    }

    fun generar(tema: String, tono: String, cta: String, plataformas: List<String>) {
        _uiState.value = _uiState.value.copy(isGenerating = true, error = null)
        viewModelScope.launch {
            repository.generarPosts(
                PlanificadorInput(
                    accion = "generar",
                    tema = tema.takeIf { it.isNotBlank() },
                    tono = tono.takeIf { it.isNotBlank() },
                    cta = cta.takeIf { it.isNotBlank() },
                    plataformas = plataformas.takeIf { it.isNotEmpty() }
                )
            )
                .onSuccess { nuevos ->
                    _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        showGenerarSheet = false,
                        posts = nuevos + _uiState.value.posts,
                        info = "El agente generó ${nuevos.size} borradores ✨"
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        error = e.message ?: "La generación falló"
                    )
                }
        }
    }

    fun crear(plataforma: String, titulo: String, copy: String, fecha: String) {
        _uiState.value = _uiState.value.copy(isSaving = true, error = null)
        viewModelScope.launch {
            repository.createPost(
                PlanificadorInput(
                    accion = "create",
                    plataforma = plataforma,
                    titulo = titulo.takeIf { it.isNotBlank() },
                    copy = copy,
                    fecha = fecha.takeIf { it.isNotBlank() }
                )
            )
                .onSuccess { post ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        showCrearSheet = false,
                        posts = listOf(post) + _uiState.value.posts
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = e.message ?: "No se pudo crear el post"
                    )
                }
        }
    }

    fun setEstado(id: String, estado: String) {
        viewModelScope.launch {
            repository.updatePost(PlanificadorInput(accion = "update", id = id, estado = estado))
                .onSuccess { actualizado ->
                    _uiState.value = _uiState.value.copy(
                        posts = _uiState.value.posts.map {
                            if (it.id == actualizado.id) actualizado else it
                        }
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "No se pudo actualizar"
                    )
                }
        }
    }

    fun delete(id: String) {
        val previos = _uiState.value.posts
        _uiState.value = _uiState.value.copy(posts = previos.filterNot { it.id == id })
        viewModelScope.launch {
            repository.deletePost(id)
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        posts = previos,
                        error = e.message ?: "No se pudo borrar"
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

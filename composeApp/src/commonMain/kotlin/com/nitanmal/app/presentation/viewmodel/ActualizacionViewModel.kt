package com.nitanmal.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitanmal.app.core.util.versionCodeApp
import com.nitanmal.app.core.util.versionNameApp
import com.nitanmal.app.domain.repository.FanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ActualizacionUiState(
    /** Hay una versión más nueva publicada. */
    val hayNueva: Boolean = false,
    /** La versión instalada quedó por debajo del mínimo: hay que actualizar sí o sí. */
    val obligatoria: Boolean = false,
    val versionNueva: String = "",
    val notas: String = "",
    val url: String = "",
    val descartada: Boolean = false
) {
    val mostrar: Boolean get() = hayNueva && (obligatoria || !descartada)
}

/** Avisa al arrancar si hay una versión más nueva publicada (GET /version). */
class ActualizacionViewModel(
    private val repository: FanRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ActualizacionUiState())
    val uiState: StateFlow<ActualizacionUiState> = _uiState

    fun comprobar(esAndroid: Boolean = true) {
        viewModelScope.launch {
            repository.getVersion().onSuccess { r ->
                val info = if (esAndroid) r.android else r.ios
                val instalada = versionCodeApp()
                // Sin datos publicados (0) no molestamos al usuario.
                if (info.versionCode <= 0 || instalada <= 0) return@onSuccess
                if (info.versionCode > instalada) {
                    _uiState.value = ActualizacionUiState(
                        hayNueva = true,
                        obligatoria = instalada < info.minVersionCode,
                        versionNueva = info.versionName.ifBlank { "${info.versionCode}" },
                        notas = info.notas,
                        url = info.url
                    )
                }
            }
            // Si falla la consulta no se avisa nada: nunca bloquea el arranque.
        }
    }

    fun descartar() {
        _uiState.value = _uiState.value.copy(descartada = true)
    }

    fun versionInstalada(): String = versionNameApp()
}

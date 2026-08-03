package com.nitanmal.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitanmal.app.domain.model.User
import com.nitanmal.app.domain.usecase.SelectClientUseCase
import com.nitanmal.app.domain.usecase.SignInWithGoogleUseCase
import com.nitanmal.app.domain.usecase.SignOutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class AppScreen {
    LOGIN,
    CLIENT_SELECTION,
    DASHBOARD
}

/** Modo de la app para el staff (los miembros solo conocen FAN). */
enum class AppModo { FAN, EQUIPO }

data class AuthUiState(
    val isLoading: Boolean = false,
    /** true mientras se intenta restaurar la sesión persistida (splash). */
    val isRestoring: Boolean = true,
    val currentUser: User? = null,
    val error: String? = null,
    val screen: AppScreen = AppScreen.LOGIN,
    val modo: AppModo = AppModo.FAN
)

class AuthViewModel(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val selectClientUseCase: SelectClientUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val repository: com.nitanmal.app.domain.repository.AuthRepository? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    /** Restaura la sesión persistida por Firebase sin mostrar el picker. */
    fun tryRestoreSession() {
        viewModelScope.launch {
            val user = repository?.restoreSession()
            _uiState.value = if (user != null) {
                _uiState.value.copy(
                    isRestoring = false,
                    currentUser = user,
                    screen = AppScreen.DASHBOARD,
                    // El staff entra directo a su modo de trabajo.
                    modo = if (user.esEquipo) AppModo.EQUIPO else AppModo.FAN
                )
            } else {
                _uiState.value.copy(isRestoring = false, screen = AppScreen.LOGIN)
            }
        }
    }

    fun setModo(modo: AppModo) {
        _uiState.value = _uiState.value.copy(modo = modo)
    }

    fun updateProfile(apodo: String, pais: String, region: String, telefono: String) {
        viewModelScope.launch {
            repository?.updateProfile(apodo, pais, region, telefono)
                ?.onSuccess { user ->
                    _uiState.value = _uiState.value.copy(currentUser = user)
                }
                ?.onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "No se pudo guardar el perfil"
                    )
                }
        }
    }

    fun signInWithGoogle() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {

            signInWithGoogleUseCase()
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUser = user,
                        screen = AppScreen.DASHBOARD,
                        modo = if (user.esEquipo) AppModo.EQUIPO else AppModo.FAN
                    )
                }
                .onFailure { exception ->
                    // Don't show error if user canceled
                    val errorMessage = if (exception.message == "USER_CANCELED") {
                        null
                    } else {
                        exception.message ?: "Error con Google Sign-In"
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
        }
    }

    fun selectClient(clientKey: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            selectClientUseCase(clientKey)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUser = user,
                        screen = AppScreen.DASHBOARD
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al seleccionar empresa"
                    )
                }
        }
    }

    fun switchClient() {
        val user = _uiState.value.currentUser ?: return
        if (user.roles.size <= 1) return
        _uiState.value = _uiState.value.copy(
            screen = AppScreen.CLIENT_SELECTION,
            error = null
        )
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
                .onSuccess {
                    _uiState.value = AuthUiState(isRestoring = false)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Error al cerrar sesión"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

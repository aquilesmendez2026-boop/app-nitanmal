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

data class AuthUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val error: String? = null,
    val screen: AppScreen = AppScreen.LOGIN
)

class AuthViewModel(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val selectClientUseCase: SelectClientUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun signInWithGoogle() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {

            signInWithGoogleUseCase()
                .onSuccess { user ->
                    val nextScreen = when {
                        user.roles.size > 1 -> AppScreen.CLIENT_SELECTION
                        user.roles.size == 1 -> {
                            selectClient(user.roles.first().clientKey)
                            return@launch
                        }
                        else -> AppScreen.DASHBOARD
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUser = user,
                        screen = nextScreen
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
                    _uiState.value = AuthUiState()
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

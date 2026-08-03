package com.nitanmal.app

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nitanmal.app.core.localization.ProvideLocaleManager
import com.nitanmal.app.data.repository.AuthRepositoryImpl
import com.nitanmal.app.domain.auth.LocalPlatformAuth
import com.nitanmal.app.domain.usecase.SelectClientUseCase
import com.nitanmal.app.domain.usecase.SignInWithGoogleUseCase
import com.nitanmal.app.domain.usecase.SignOutUseCase
import com.nitanmal.app.presentation.ui.screens.FanDashboardScreen
import com.nitanmal.app.presentation.ui.screens.LoginScreen
import com.nitanmal.app.presentation.ui.screens.MainDashboardScreen
import com.nitanmal.app.presentation.ui.screens.SplashScreen
import com.nitanmal.app.presentation.viewmodel.AppModo
import com.nitanmal.app.presentation.viewmodel.AuthViewModel
import com.nitanmal.app.theme.NitanmalTheme

@Composable
fun App() {
    var isDarkTheme by remember { mutableStateOf(false) }

    NitanmalTheme(darkTheme = isDarkTheme) {
        ProvideLocaleManager {
            val platformAuth = LocalPlatformAuth.current
            val authRepository = remember { AuthRepositoryImpl(platformAuth) }
            val authViewModel = viewModel {
                AuthViewModel(
                    signInWithGoogleUseCase = SignInWithGoogleUseCase(authRepository),
                    selectClientUseCase = SelectClientUseCase(authRepository),
                    signOutUseCase = SignOutUseCase(authRepository),
                    repository = authRepository
                )
            }
            val uiState by authViewModel.uiState.collectAsState()

            // Restaura la sesión persistida por Firebase sin mostrar el picker.
            LaunchedEffect(Unit) { authViewModel.tryRestoreSession() }

            val user = uiState.currentUser
            when {
                uiState.isRestoring -> SplashScreen()

                user == null -> LoginScreen(
                    uiState = uiState,
                    onGoogleSignInClick = { authViewModel.signInWithGoogle() },
                    onClearError = { authViewModel.clearError() }
                )

                // Modo Equipo: la herramienta de trabajo del staff.
                uiState.modo == AppModo.EQUIPO && user.esEquipo -> MainDashboardScreen(
                    user = user,
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = { isDarkTheme = !isDarkTheme },
                    onSignOutClick = { authViewModel.signOut() },
                    onSwitchSessionClick = { authViewModel.switchClient() },
                    onSwitchToFan = { authViewModel.setModo(AppModo.FAN) }
                )

                // Modo Fan: contenido del podcast para todos.
                else -> FanDashboardScreen(
                    user = user,
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = { isDarkTheme = !isDarkTheme },
                    onGuardarPerfil = { apodo, pais, region, telefono ->
                        authViewModel.updateProfile(apodo, pais, region, telefono)
                    },
                    onSignOutClick = { authViewModel.signOut() },
                    onSwitchToEquipo = if (user.esEquipo) {
                        { authViewModel.setModo(AppModo.EQUIPO) }
                    } else null
                )
            }
        }
    }
}

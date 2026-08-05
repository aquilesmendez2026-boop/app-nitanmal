package com.nitanmal.app

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nitanmal.app.core.localization.ProvideLocaleManager
import com.nitanmal.app.data.repository.AuthRepositoryImpl
import com.nitanmal.app.domain.auth.LocalPlatformAuth
import com.nitanmal.app.domain.usecase.SelectClientUseCase
import com.nitanmal.app.domain.usecase.SignInWithGoogleUseCase
import com.nitanmal.app.domain.usecase.SignOutUseCase
import com.nitanmal.app.presentation.ui.screens.LoginScreen
import com.nitanmal.app.presentation.ui.screens.PortadaPublicaScreen
import com.nitanmal.app.presentation.ui.screens.RootDashboardScreen
import com.nitanmal.app.presentation.ui.screens.SplashScreen
import com.nitanmal.app.presentation.viewmodel.ActualizacionViewModel
import com.nitanmal.app.presentation.viewmodel.AuthViewModel
import com.nitanmal.app.theme.NitanmalTheme
import com.nitanmal.app.theme.TemaApp

@Composable
fun App() {
    var tema by remember { mutableStateOf(TemaApp.WEB) }
    // El login es una pantalla a la que se ENTRA desde la portada pública,
    // no una barrera: los visitantes navegan el contenido sin sesión.
    var showLogin by remember { mutableStateOf(false) }

    NitanmalTheme(tema = tema) {
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

            // Aviso de nueva versión (consulta pública, no bloquea el arranque).
            val fanRepository = remember { com.nitanmal.app.data.repository.FanRepositoryImpl(platformAuth) }
            val actualizacionViewModel = viewModel { ActualizacionViewModel(fanRepository) }
            val actualizacion by actualizacionViewModel.uiState.collectAsState()

            // Restaura la sesión persistida por Firebase sin mostrar el picker.
            LaunchedEffect(Unit) {
                authViewModel.tryRestoreSession()
                actualizacionViewModel.comprobar()
            }

            val user = uiState.currentUser
            // Al completar el login, volvemos al shell.
            LaunchedEffect(user) { if (user != null) showLogin = false }

            when {
                uiState.isRestoring -> SplashScreen()

                user == null && showLogin -> LoginScreen(
                    uiState = uiState,
                    onGoogleSignInClick = { authViewModel.signInWithGoogle() },
                    onClearError = { authViewModel.clearError() },
                    onBack = { showLogin = false }
                )

                // Visitantes: portada pública sin navbar; la barra aparece con sesión.
                user == null -> PortadaPublicaScreen(
                    onLoginClick = { showLogin = true }
                )

                else -> RootDashboardScreen(
                    user = user,
                    tema = tema,
                    onTemaChange = { tema = it },
                    onGuardarPerfil = { apodo, pais, region, telefono ->
                        authViewModel.updateProfile(apodo, pais, region, telefono)
                    },
                    onSignOutClick = { authViewModel.signOut() }
                )
            }

            if (actualizacion.mostrar) {
                com.nitanmal.app.presentation.ui.screens.ActualizacionDialog(
                    estado = actualizacion,
                    versionInstalada = actualizacionViewModel.versionInstalada(),
                    onDescartar = { actualizacionViewModel.descartar() }
                )
            }
        }
    }
}

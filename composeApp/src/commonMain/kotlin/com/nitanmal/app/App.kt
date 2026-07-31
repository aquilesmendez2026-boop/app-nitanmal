package com.nitanmal.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nitanmal.app.core.localization.ProvideLocaleManager
import com.nitanmal.app.data.repository.AuthRepositoryImpl
import com.nitanmal.app.domain.auth.LocalPlatformAuth
import com.nitanmal.app.domain.usecase.SelectClientUseCase
import com.nitanmal.app.domain.usecase.SignInWithGoogleUseCase
import com.nitanmal.app.domain.usecase.SignOutUseCase
import com.nitanmal.app.presentation.navigation.ClientSelectionRoute
import com.nitanmal.app.presentation.navigation.DashboardRoute
import com.nitanmal.app.presentation.navigation.LoginRoute
import com.nitanmal.app.presentation.ui.screens.ClientSelectionScreen
import com.nitanmal.app.presentation.ui.screens.LoginScreen
import com.nitanmal.app.presentation.ui.screens.MainDashboardScreen
import com.nitanmal.app.presentation.ui.screens.SplashScreen
import com.nitanmal.app.presentation.viewmodel.AppScreen
import com.nitanmal.app.presentation.viewmodel.AuthViewModel
import com.nitanmal.app.theme.NitanmalTheme
import kotlinx.coroutines.delay

@Composable
fun App() {
    var isDarkTheme by remember { mutableStateOf(false) }
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000L)
        showSplash = false
    }

    if (showSplash) {
        SplashScreen()
    } else NitanmalTheme(darkTheme = isDarkTheme) {
        ProvideLocaleManager {
            val platformAuth = LocalPlatformAuth.current
            val authRepository = remember { AuthRepositoryImpl(platformAuth) }
            val authViewModel = viewModel {
                AuthViewModel(
                    signInWithGoogleUseCase = SignInWithGoogleUseCase(authRepository),
                    selectClientUseCase = SelectClientUseCase(authRepository),
                    signOutUseCase = SignOutUseCase(authRepository)
                )
            }

            val uiState by authViewModel.uiState.collectAsState()
            val navController = rememberNavController()

            // Navigate based on auth state
            LaunchedEffect(uiState.screen) {
                when (uiState.screen) {
                    AppScreen.LOGIN -> {
                        navController.navigate(LoginRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                    AppScreen.CLIENT_SELECTION -> {
                        navController.navigate(ClientSelectionRoute) {
                            popUpTo(LoginRoute) { inclusive = true }
                        }
                    }
                    AppScreen.DASHBOARD -> {
                        navController.navigate(DashboardRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                NavHost(
                    navController = navController,
                    startDestination = LoginRoute
                ) {
                    composable<LoginRoute> {
                        LoginScreen(
                            uiState = uiState,
                            onGoogleSignInClick = { authViewModel.signInWithGoogle() },
                            onClearError = { authViewModel.clearError() }
                        )
                    }

                    composable<ClientSelectionRoute> {
                        uiState.currentUser?.let { user ->
                            ClientSelectionScreen(
                                user = user,
                                onClientSelected = { authViewModel.selectClient(it) },
                                onSignOutClick = { authViewModel.signOut() }
                            )
                        }
                    }

                    composable<DashboardRoute> {
                        uiState.currentUser?.let { user ->
                            MainDashboardScreen(
                                user = user,
                                isDarkTheme = isDarkTheme,
                                onThemeToggle = { isDarkTheme = !isDarkTheme },
                                onSignOutClick = { authViewModel.signOut() },
                                onSwitchSessionClick = { authViewModel.switchClient() }
                            )
                        }
                    }
                }
            }
        }
    }
}

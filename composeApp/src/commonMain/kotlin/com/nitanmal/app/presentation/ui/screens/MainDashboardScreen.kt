package com.nitanmal.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.nitanmal.app.data.repository.TeamRepositoryImpl
import com.nitanmal.app.domain.auth.LocalPlatformAuth
import com.nitanmal.app.domain.model.User
import com.nitanmal.app.presentation.navigation.BuzonRoute
import com.nitanmal.app.presentation.navigation.EpisodioDetailRoute
import com.nitanmal.app.presentation.navigation.HomeRoute
import com.nitanmal.app.presentation.navigation.IdeaDetailRoute
import com.nitanmal.app.presentation.navigation.IdeasRoute
import com.nitanmal.app.presentation.navigation.ProduccionRoute
import com.nitanmal.app.presentation.navigation.ReunionesRoute
import com.nitanmal.app.presentation.navigation.SettingsRoute
import com.nitanmal.app.presentation.ui.components.NitanmalNavigationBar
import com.nitanmal.app.presentation.viewmodel.BuzonViewModel
import com.nitanmal.app.presentation.viewmodel.IdeasViewModel
import com.nitanmal.app.presentation.viewmodel.NotificacionesViewModel
import com.nitanmal.app.presentation.viewmodel.ProduccionViewModel
import com.nitanmal.app.presentation.viewmodel.ReunionesViewModel

/**
 * Dashboard principal con navbar inferior:
 * Inicio / Ideas / Producción / Reuniones / Buzón.
 * Ajustes se abre desde el engranaje del Inicio.
 */
@Composable
fun MainDashboardScreen(
    user: User,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onSignOutClick: () -> Unit,
    onSwitchSessionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val platformAuth = LocalPlatformAuth.current
    val teamRepository = remember { TeamRepositoryImpl(platformAuth) }
    val ideasViewModel = remember { IdeasViewModel(teamRepository) }
    val buzonViewModel = remember { BuzonViewModel(teamRepository) }
    val notificacionesViewModel = remember { NotificacionesViewModel(teamRepository) }
    val produccionViewModel = remember { ProduccionViewModel(teamRepository) }
    val reunionesViewModel = remember { ReunionesViewModel(teamRepository) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isAdmin = user.role == "admin" || user.role == "superadmin"
    val scaffoldBg = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)

    Scaffold(
        containerColor = scaffoldBg,
        bottomBar = {
            NitanmalNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = HomeRoute
            ) {
                composable<HomeRoute> {
                    HomeScreen(
                        user = user,
                        ideasViewModel = ideasViewModel,
                        buzonViewModel = buzonViewModel,
                        notificacionesViewModel = notificacionesViewModel,
                        produccionViewModel = produccionViewModel,
                        reunionesViewModel = reunionesViewModel,
                        onGoToIdeas = {
                            navController.navigate(IdeasRoute) { launchSingleTop = true }
                        },
                        onGoToBuzon = {
                            navController.navigate(BuzonRoute) { launchSingleTop = true }
                        },
                        onGoToProduccion = {
                            navController.navigate(ProduccionRoute) { launchSingleTop = true }
                        },
                        onGoToReuniones = {
                            navController.navigate(ReunionesRoute) { launchSingleTop = true }
                        },
                        onGoToSettings = {
                            navController.navigate(SettingsRoute) { launchSingleTop = true }
                        },
                        // Desde Inicio: primero a la pestaña y luego al detalle,
                        // para que atrás caiga en la lista y la navbar marque la pestaña.
                        onOpenIdea = { notaId ->
                            navController.navigate(IdeasRoute) { launchSingleTop = true }
                            navController.navigate(IdeaDetailRoute(notaId))
                        },
                        onOpenEpisodio = { episodioId ->
                            navController.navigate(ProduccionRoute) { launchSingleTop = true }
                            navController.navigate(EpisodioDetailRoute(episodioId))
                        }
                    )
                }

                composable<IdeasRoute> {
                    IdeasScreen(
                        viewModel = ideasViewModel,
                        currentUserId = user.id,
                        isAdmin = isAdmin,
                        onOpenIdea = { notaId ->
                            navController.navigate(IdeaDetailRoute(notaId))
                        }
                    )
                }

                composable<IdeaDetailRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<IdeaDetailRoute>()
                    IdeaDetailScreen(
                        notaId = route.notaId,
                        viewModel = ideasViewModel,
                        currentUserId = user.id,
                        isAdmin = isAdmin,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<ProduccionRoute> {
                    ProduccionScreen(
                        viewModel = produccionViewModel,
                        currentUserId = user.id,
                        isAdmin = isAdmin,
                        onOpenEpisodio = { episodioId ->
                            navController.navigate(EpisodioDetailRoute(episodioId))
                        }
                    )
                }

                composable<EpisodioDetailRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<EpisodioDetailRoute>()
                    EpisodioDetailScreen(
                        episodioId = route.episodioId,
                        viewModel = produccionViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<ReunionesRoute> {
                    ReunionesScreen(
                        viewModel = reunionesViewModel,
                        currentUserId = user.id,
                        isAdmin = isAdmin
                    )
                }

                composable<BuzonRoute> {
                    BuzonScreen(viewModel = buzonViewModel)
                }

                composable<SettingsRoute> {
                    SettingsScreen(
                        user = user,
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = onThemeToggle,
                        onSignOutClick = onSignOutClick,
                        onSwitchSessionClick = onSwitchSessionClick
                    )
                }
            }
        }
    }
}

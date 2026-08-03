package com.nitanmal.app.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.nitanmal.app.core.localization.rememberStrings
import com.nitanmal.app.data.repository.FanRepositoryImpl
import com.nitanmal.app.data.repository.TeamRepositoryImpl
import com.nitanmal.app.domain.auth.LocalPlatformAuth
import com.nitanmal.app.domain.model.User
import com.nitanmal.app.presentation.navigation.CuentaRoute
import com.nitanmal.app.presentation.navigation.EnVivoRoute
import com.nitanmal.app.presentation.navigation.EpisodioFanDetailRoute
import com.nitanmal.app.presentation.navigation.EpisodiosFanRoute
import com.nitanmal.app.presentation.navigation.FanInicioRoute
import com.nitanmal.app.presentation.navigation.MiZonaRoute
import com.nitanmal.app.presentation.ui.icons.AppIcons
import com.nitanmal.app.presentation.ui.icons.AppIcons2
import com.nitanmal.app.presentation.viewmodel.CanalesViewModel
import com.nitanmal.app.presentation.viewmodel.FanViewModel
import com.nitanmal.app.presentation.viewmodel.MiZonaViewModel

/**
 * Shell del modo Fan: Inicio · En Vivo (central, rojo si hay transmisión)
 * · Episodios · Mi Zona · Cuenta.
 */
@Composable
fun FanDashboardScreen(
    user: User,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onGuardarPerfil: (String, String, String, String) -> Unit,
    onSignOutClick: () -> Unit,
    onSwitchToEquipo: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val navController = rememberNavController()
    val platformAuth = LocalPlatformAuth.current
    val fanRepository = remember { FanRepositoryImpl(platformAuth) }
    val teamRepository = remember { TeamRepositoryImpl(platformAuth) }
    val fanViewModel = remember { FanViewModel(fanRepository) }
    val miZonaViewModel = remember { MiZonaViewModel(fanRepository) }
    val canalesViewModel = remember { CanalesViewModel(teamRepository) }
    val fanState by fanViewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val scaffoldBg = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val isLive = fanState.live?.isLive == true

    val inicioName = FanInicioRoute::class.qualifiedName
    val enVivoName = EnVivoRoute::class.qualifiedName
    val episodiosName = EpisodiosFanRoute::class.qualifiedName
    val epDetailName = EpisodioFanDetailRoute::class.qualifiedName
    val miZonaName = MiZonaRoute::class.qualifiedName
    val cuentaName = CuentaRoute::class.qualifiedName

    val selectedIndex = when {
        currentRoute == inicioName -> 0
        currentRoute == enVivoName -> 1
        currentRoute == episodiosName -> 2
        epDetailName != null && currentRoute?.startsWith(epDetailName) == true -> 2
        currentRoute == miZonaName -> 3
        currentRoute == cuentaName -> 4
        else -> 0
    }

    Scaffold(
        containerColor = scaffoldBg,
        bottomBar = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    windowInsets = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
                ) {
                    val items = listOf(
                        Triple(strings.fanNavInicio, AppIcons.Home, FanInicioRoute as Any),
                        Triple(strings.fanNavEnVivo, AppIcons2.Live, EnVivoRoute as Any),
                        Triple(strings.fanNavEpisodios, AppIcons2.Movie, EpisodiosFanRoute as Any),
                        Triple(strings.fanNavMiZona, AppIcons.Star, MiZonaRoute as Any),
                        Triple(strings.fanNavCuenta, AppIcons2.Person, CuentaRoute as Any)
                    )
                    items.forEachIndexed { index, (label, icon, route) ->
                        val selected = selectedIndex == index
                        // El botón central En Vivo pulsa rojo cuando hay transmisión.
                        val esEnVivo = index == 1
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(route) { launchSingleTop = true }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (esEnVivo && isLive) Color(0xFFdc2626)
                                else MaterialTheme.colorScheme.primary,
                                selectedTextColor = if (esEnVivo && isLive) Color(0xFFdc2626)
                                else MaterialTheme.colorScheme.primary,
                                indicatorColor = if (esEnVivo && isLive) Color(0xFFdc2626).copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                unselectedIconColor = if (esEnVivo && isLive) Color(0xFFdc2626)
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                unselectedTextColor = if (esEnVivo && isLive) Color(0xFFdc2626)
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                            )
                        )
                    }
                }
            }
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
                startDestination = FanInicioRoute
            ) {
                composable<FanInicioRoute> {
                    InicioFanScreen(
                        user = user,
                        fanViewModel = fanViewModel,
                        canalesViewModel = canalesViewModel,
                        onGoToEnVivo = {
                            navController.navigate(EnVivoRoute) { launchSingleTop = true }
                        },
                        onGoToEpisodios = {
                            navController.navigate(EpisodiosFanRoute) { launchSingleTop = true }
                        },
                        onOpenEpisodio = { id ->
                            navController.navigate(EpisodiosFanRoute) { launchSingleTop = true }
                            navController.navigate(EpisodioFanDetailRoute(id))
                        },
                        onSwitchToEquipo = onSwitchToEquipo
                    )
                }

                composable<EnVivoRoute> {
                    EnVivoScreen(fanViewModel = fanViewModel)
                }

                composable<EpisodiosFanRoute> {
                    EpisodiosFanScreen(
                        fanViewModel = fanViewModel,
                        esPremiumUsuario = user.esPremium,
                        onOpenEpisodio = { id ->
                            navController.navigate(EpisodioFanDetailRoute(id))
                        }
                    )
                }

                composable<EpisodioFanDetailRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<EpisodioFanDetailRoute>()
                    EpisodioFanDetailScreen(
                        episodioId = route.episodioId,
                        fanViewModel = fanViewModel,
                        esPremiumUsuario = user.esPremium,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<MiZonaRoute> {
                    MiZonaScreen(
                        viewModel = miZonaViewModel,
                        esPremiumUsuario = user.esPremium
                    )
                }

                composable<CuentaRoute> {
                    CuentaScreen(
                        user = user,
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = onThemeToggle,
                        onGuardarPerfil = onGuardarPerfil,
                        onSwitchToEquipo = onSwitchToEquipo,
                        onSignOutClick = onSignOutClick
                    )
                }
            }
        }
    }
}

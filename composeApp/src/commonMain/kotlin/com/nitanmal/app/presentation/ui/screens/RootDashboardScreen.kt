package com.nitanmal.app.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.nitanmal.app.presentation.navigation.*
import com.nitanmal.app.presentation.ui.icons.AppIcons
import com.nitanmal.app.presentation.ui.icons.AppIcons2
import com.nitanmal.app.presentation.viewmodel.*
import com.nitanmal.app.theme.TemaApp

private data class TabDef(
    val label: String,
    val icon: ImageVector,
    val route: Any,
    val matches: (String?) -> Boolean
)

/**
 * Shell único con navbar por rol (el rol agrega pestañas, nunca reordena).
 * Solo para usuarios con sesión; los visitantes ven PortadaPublicaScreen.
 * - miembro:       Inicio · Mi Zona · Ajustes
 * - participante:  Inicio · Trabajo · Agenda · Ajustes
 * - admin+:        Inicio · Trabajo · Agenda · Admin · Ajustes
 */
@Composable
fun RootDashboardScreen(
    user: User,
    tema: TemaApp,
    onTemaChange: (TemaApp) -> Unit,
    onGuardarPerfil: (String, String, String, String) -> Unit,
    onSignOutClick: () -> Unit,
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
    val ideasViewModel = remember { IdeasViewModel(teamRepository) }
    val produccionViewModel = remember { ProduccionViewModel(teamRepository) }
    val planificadorViewModel = remember { PlanificadorViewModel(teamRepository) }
    val reunionesViewModel = remember { ReunionesViewModel(teamRepository) }
    val buzonViewModel = remember { BuzonViewModel(teamRepository) }
    val adminViewModel = remember { AdminViewModel(fanRepository) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isAdmin = user.role == "admin" || user.role == "superadmin"

    fun ruta(r: kotlin.reflect.KClass<*>): String? = r.qualifiedName

    val tabs = remember(user.role) {
        buildList {
            add(TabDef(strings.fanNavInicio, AppIcons.Home, FanInicioRoute) { r ->
                r == ruta(FanInicioRoute::class) ||
                    r == ruta(EnVivoRoute::class) ||
                    r == ruta(EpisodiosFanRoute::class) ||
                    (ruta(EpisodioFanDetailRoute::class)?.let { r?.startsWith(it) } == true)
            })
            if (!user.esEquipo) {
                add(TabDef(strings.fanNavMiZona, AppIcons.Star, MiZonaRoute) { r ->
                    r == ruta(MiZonaRoute::class)
                })
            }
            if (user.esEquipo) {
                add(TabDef(strings.navTrabajo, AppIcons2.Briefcase, TrabajoRoute) { r ->
                    r == ruta(TrabajoRoute::class)
                })
                add(TabDef(strings.navAgenda, AppIcons2.Event, AgendaEquipoRoute) { r ->
                    r == ruta(AgendaEquipoRoute::class) ||
                        (ruta(IdeaDetailRoute::class)?.let { r?.startsWith(it) } == true) ||
                        (ruta(EpisodioDetailRoute::class)?.let { r?.startsWith(it) } == true)
                })
            }
            if (isAdmin) {
                add(TabDef(strings.navAdmin, AppIcons2.Shield, AdminRoute) { r ->
                    r == ruta(AdminRoute::class)
                })
            }
            add(TabDef(strings.navSettings, AppIcons2.Person, CuentaRoute) { r ->
                r == ruta(CuentaRoute::class)
            })
        }
    }

    val selectedIndex = tabs.indexOfFirst { it.matches(currentRoute) }.coerceAtLeast(0)
    val fanState by fanViewModel.uiState.collectAsState()
    val isLive = fanState.live?.isLive == true

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                    tabs.forEachIndexed { index, tab ->
                        val selected = selectedIndex == index
                        // El Inicio pulsa rojo cuando hay transmisión en vivo.
                        val rojo = index == 0 && isLive
                        NavigationBarItem(
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) { launchSingleTop = true }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (rojo) Color(0xFFdc2626) else MaterialTheme.colorScheme.primary,
                                selectedTextColor = if (rojo) Color(0xFFdc2626) else MaterialTheme.colorScheme.primary,
                                indicatorColor = (if (rojo) Color(0xFFdc2626) else MaterialTheme.colorScheme.primary)
                                    .copy(alpha = 0.15f),
                                unselectedIconColor = if (rojo) Color(0xFFdc2626)
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                unselectedTextColor = if (rojo) Color(0xFFdc2626)
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
                // ── Fan ──
                composable<FanInicioRoute> {
                    InicioFanScreen(
                        user = user,
                        fanViewModel = fanViewModel,
                        canalesViewModel = canalesViewModel,
                        onGoToEnVivo = { navController.navigate(EnVivoRoute) { launchSingleTop = true } },
                        onGoToEpisodios = { navController.navigate(EpisodiosFanRoute) { launchSingleTop = true } },
                        onOpenEpisodio = { id ->
                            navController.navigate(EpisodiosFanRoute) { launchSingleTop = true }
                            navController.navigate(EpisodioFanDetailRoute(id))
                        }
                    )
                }
                composable<EnVivoRoute> { EnVivoScreen(fanViewModel = fanViewModel) }
                composable<EpisodiosFanRoute> {
                    EpisodiosFanScreen(
                        fanViewModel = fanViewModel,
                        esPremiumUsuario = user.esPremium,
                        onOpenEpisodio = { id -> navController.navigate(EpisodioFanDetailRoute(id)) }
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
                    MiZonaScreen(viewModel = miZonaViewModel, esPremiumUsuario = user.esPremium)
                }

                    // ── Equipo ──
                    composable<TrabajoRoute> {
                        MiTrabajoScreen(
                            user = user,
                            produccionViewModel = produccionViewModel,
                            ideasViewModel = ideasViewModel,
                            planificadorViewModel = planificadorViewModel,
                            onOpenEpisodio = { id -> navController.navigate(EpisodioDetailRoute(id)) },
                            onOpenIdea = { id -> navController.navigate(IdeaDetailRoute(id)) },
                            onGoToPlanner = {
                                navController.navigate(AgendaEquipoRoute) { launchSingleTop = true }
                            }
                        )
                    }
                    composable<AgendaEquipoRoute> {
                        AgendaEquipoScreen(
                            user = user,
                            isAdmin = isAdmin,
                            produccionViewModel = produccionViewModel,
                            reunionesViewModel = reunionesViewModel,
                            ideasViewModel = ideasViewModel,
                            planificadorViewModel = planificadorViewModel,
                            canalesViewModel = canalesViewModel,
                            buzonViewModel = buzonViewModel,
                            onOpenEpisodio = { id -> navController.navigate(EpisodioDetailRoute(id)) },
                            onOpenIdea = { id -> navController.navigate(IdeaDetailRoute(id)) }
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
                    composable<EpisodioDetailRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<EpisodioDetailRoute>()
                        EpisodioDetailScreen(
                            episodioId = route.episodioId,
                            viewModel = produccionViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // ── Admin ──
                    composable<AdminRoute> { AdminScreen(viewModel = adminViewModel) }

                    // ── Ajustes ──
                    composable<CuentaRoute> {
                        CuentaScreen(
                            user = user,
                            tema = tema,
                            onTemaChange = onTemaChange,
                            onGuardarPerfil = onGuardarPerfil,
                            onSignOutClick = onSignOutClick
                        )
                    }
            }
        }
    }
}

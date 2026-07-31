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
import com.nitanmal.app.core.localization.rememberStrings
import com.nitanmal.app.domain.model.User
import com.nitanmal.app.presentation.navigation.CoursesRoute
import com.nitanmal.app.presentation.navigation.GradesRoute
import com.nitanmal.app.presentation.navigation.HomeRoute
import com.nitanmal.app.presentation.navigation.SettingsRoute
import com.nitanmal.app.presentation.ui.components.NitanmalNavigationBar

/**
 * Dashboard principal con navbar inferior (misma estructura que uminer).
 * Home / Cursos / Notas son placeholders por ahora; Ajustes permite
 * cambiar tema, cambiar de portal y cerrar sesión.
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
    val strings = rememberStrings()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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
                    HomeScreen(user = user)
                }

                composable<CoursesRoute> {
                    PlaceholderScreen(title = strings.navCourses)
                }

                composable<GradesRoute> {
                    PlaceholderScreen(title = strings.navGrades)
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

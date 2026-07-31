package com.nitanmal.app.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.nitanmal.app.core.localization.rememberStrings
import com.nitanmal.app.presentation.navigation.*
import com.nitanmal.app.presentation.ui.icons.AppIcons

@Composable
fun NitanmalNavigationBar(
    currentRoute: String?,
    onNavigate: (Any) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val items = listOf(strings.navHome, strings.navCourses, strings.navGrades, strings.navSettings)
    val routes = listOf(HomeRoute, CoursesRoute, GradesRoute, SettingsRoute)
    val selectedIcons = listOf(
        AppIcons.Home,
        AppIcons.School,
        AppIcons.Star,
        AppIcons.Settings
    )
    val unselectedIcons = listOf(
        AppIcons.Home,
        AppIcons.School,
        AppIcons.Star,
        AppIcons.Settings
    )

    val homeRouteName     = HomeRoute::class.qualifiedName
    val coursesRouteName  = CoursesRoute::class.qualifiedName
    val gradesRouteName   = GradesRoute::class.qualifiedName
    val settingsRouteName = SettingsRoute::class.qualifiedName

    val selectedIndex = when (currentRoute) {
        homeRouteName     -> 0
        coursesRouteName  -> 1
        gradesRouteName   -> 2
        settingsRouteName -> 3
        else              -> 0
    }

    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(colorScheme.surface)
    ) {
        NavigationBar(
            containerColor = colorScheme.surface,
            contentColor = colorScheme.onSurface,
            // El fondo llega hasta el borde inferior pero el contenido respeta
            // la barra de navegación del sistema (gestos/botones).
            windowInsets = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
        ) {
            items.forEachIndexed { index, item ->
                val selected = selectedIndex == index
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = if (selected) selectedIcons[index] else unselectedIcons[index],
                            contentDescription = item,
                        )
                    },
                    label = { Text(item) },
                    selected = selected,
                    onClick = { onNavigate(routes[index]) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor   = colorScheme.primary,
                        selectedTextColor   = colorScheme.primary,
                        indicatorColor      = colorScheme.primary.copy(alpha = 0.15f),
                        unselectedIconColor = colorScheme.onSurface.copy(alpha = 0.55f),
                        unselectedTextColor = colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                )
            }
        }
    }
}

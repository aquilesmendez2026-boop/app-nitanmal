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
import com.nitanmal.app.presentation.ui.icons.AppIcons2

@Composable
fun NitanmalNavigationBar(
    currentRoute: String?,
    onNavigate: (Any) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val items = listOf(
        strings.navHome,
        strings.navIdeas,
        strings.navProduccion,
        strings.navReuniones,
        strings.navBuzon
    )
    val routes = listOf(HomeRoute, IdeasRoute, ProduccionRoute, ReunionesRoute, BuzonRoute)
    val icons = listOf(
        AppIcons.Home,
        AppIcons2.Lightbulb,
        AppIcons2.Movie,
        AppIcons2.Event,
        AppIcons2.Mail
    )

    val homeRouteName        = HomeRoute::class.qualifiedName
    val ideasRouteName       = IdeasRoute::class.qualifiedName
    val produccionRouteName  = ProduccionRoute::class.qualifiedName
    val reunionesRouteName   = ReunionesRoute::class.qualifiedName
    val buzonRouteName       = BuzonRoute::class.qualifiedName
    // Los detalles pertenecen a su pestaña: la navbar los marca como tal.
    val ideaDetailRouteName  = IdeaDetailRoute::class.qualifiedName
    val episodioDetailRouteName = EpisodioDetailRoute::class.qualifiedName

    val selectedIndex = when {
        currentRoute == homeRouteName -> 0
        currentRoute == ideasRouteName -> 1
        currentRoute == produccionRouteName -> 2
        currentRoute == reunionesRouteName -> 3
        currentRoute == buzonRouteName -> 4
        ideaDetailRouteName != null && currentRoute?.startsWith(ideaDetailRouteName) == true -> 1
        episodioDetailRouteName != null && currentRoute?.startsWith(episodioDetailRouteName) == true -> 2
        else -> 0
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
                            imageVector = icons[index],
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

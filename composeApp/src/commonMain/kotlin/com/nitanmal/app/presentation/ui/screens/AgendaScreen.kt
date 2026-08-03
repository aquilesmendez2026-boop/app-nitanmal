package com.nitanmal.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nitanmal.app.core.localization.rememberStrings
import com.nitanmal.app.domain.model.MetricaActual
import com.nitanmal.app.domain.model.plataformaLabel
import com.nitanmal.app.presentation.viewmodel.BuzonViewModel
import com.nitanmal.app.presentation.viewmodel.CanalesViewModel
import com.nitanmal.app.presentation.viewmodel.ReunionesViewModel

/**
 * Agenda: agrupa Reuniones / Buzón / Métricas en sub-pestañas
 * (mismo patrón que la página Agenda del web).
 */
@Composable
fun AgendaScreen(
    initialTab: String,
    reunionesViewModel: ReunionesViewModel,
    buzonViewModel: BuzonViewModel,
    canalesViewModel: CanalesViewModel,
    currentUserId: String,
    isAdmin: Boolean,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val buzonState by buzonViewModel.uiState.collectAsState()
    var tab by remember { mutableStateOf(initialTab) }

    val tabs = listOf(
        "reuniones" to strings.navReuniones,
        "buzon" to strings.navBuzon,
        "metricas" to strings.navMetricas
    )
    val selectedIndex = tabs.indexOfFirst { it.first == tab }.coerceAtLeast(0)

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, (key, label) ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = { tab = key },
                    text = {
                        if (key == "buzon" && buzonState.pendientes > 0) {
                            BadgedBox(badge = {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) { Text("${buzonState.pendientes}") }
                            }) { Text(label) }
                        } else {
                            Text(label)
                        }
                    }
                )
            }
        }

        when (tab) {
            "buzon" -> BuzonScreen(
                viewModel = buzonViewModel,
                modifier = Modifier.weight(1f)
            )

            "metricas" -> MetricasSection(
                viewModel = canalesViewModel,
                modifier = Modifier.weight(1f)
            )

            else -> ReunionesScreen(
                viewModel = reunionesViewModel,
                currentUserId = currentUserId,
                isAdmin = isAdmin,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricasSection(
    viewModel: CanalesViewModel,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState.metricas == null) viewModel.loadMetricas()
        if (uiState.canales.isEmpty()) viewModel.load()
    }

    val actuales = uiState.metricas?.actuales ?: emptyList()

    when {
        uiState.metricas == null -> {
            Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        actuales.isEmpty() -> {
            Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = strings.metricasVacio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(32.dp)
                )
            }
        }

        else -> {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = modifier
            ) {
                item {
                    Text(
                        text = strings.metricasTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                items(actuales, key = { it.plataforma }) { metrica ->
                    MetricaCard(metrica)
                }
            }
        }
    }
}

@Composable
private fun MetricaCard(metrica: MetricaActual, modifier: Modifier = Modifier) {
    val meta = com.nitanmal.app.domain.model.PLATAFORMA_META[metrica.plataforma]
    val color = meta?.color?.let { androidx.compose.ui.graphics.Color(it) }
        ?: MaterialTheme.colorScheme.onSurface

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = color,
                modifier = Modifier.size(10.dp)
            ) {}
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meta?.label ?: plataformaLabel(metrica.plataforma),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${com.nitanmal.app.domain.model.fmtSeguidores(metrica.seguidores.toString())} " +
                        (meta?.noun ?: rememberStrings().metricasSeguidores),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (metrica.delta != 0L) {
                val positivo = metrica.delta > 0
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (positivo) {
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                    }
                ) {
                    Text(
                        text = (if (positivo) "▲ +" else "▼ ") + metrica.delta,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (positivo) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

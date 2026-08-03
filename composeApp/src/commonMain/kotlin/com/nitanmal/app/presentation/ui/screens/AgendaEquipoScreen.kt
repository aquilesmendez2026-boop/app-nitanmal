package com.nitanmal.app.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nitanmal.app.core.localization.rememberStrings
import com.nitanmal.app.domain.model.MetricaActual
import com.nitanmal.app.domain.model.plataformaLabel
import com.nitanmal.app.presentation.viewmodel.BuzonViewModel
import com.nitanmal.app.presentation.viewmodel.CanalesViewModel
import com.nitanmal.app.presentation.viewmodel.IdeasViewModel
import com.nitanmal.app.presentation.viewmodel.PlanificadorViewModel
import com.nitanmal.app.presentation.viewmodel.ProduccionViewModel
import com.nitanmal.app.presentation.viewmodel.ReunionesViewModel

/**
 * Agenda del equipo: los 6 módulos del staff en tabs horizontales
 * (mismo patrón que la página /agenda del web).
 */
@Composable
fun AgendaEquipoScreen(
    user: com.nitanmal.app.domain.model.User,
    isAdmin: Boolean,
    produccionViewModel: ProduccionViewModel,
    reunionesViewModel: ReunionesViewModel,
    ideasViewModel: IdeasViewModel,
    planificadorViewModel: PlanificadorViewModel,
    canalesViewModel: CanalesViewModel,
    buzonViewModel: BuzonViewModel,
    onOpenEpisodio: (String) -> Unit,
    onOpenIdea: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    var tab by remember { mutableStateOf(0) }
    val buzonState by buzonViewModel.uiState.collectAsState()

    val tabs = listOf(
        "🎬 ${strings.navProduccion}",
        "🤝 ${strings.navReuniones}",
        "💡 ${strings.navIdeas}",
        "📆 ${strings.navPlanificador}",
        "📈 ${strings.navMetricas}",
        "💬 ${strings.navBuzon}"
    )

    Column(modifier = modifier.fillMaxSize()) {
        // Píldoras de sección, más amistosas que las tabs clásicas
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            tabs.forEachIndexed { index, label ->
                val selected = tab == index
                val texto = if (index == 5 && buzonState.pendientes > 0)
                    "$label (${buzonState.pendientes})" else label
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        else Color.White.copy(alpha = 0.10f)
                    ),
                    modifier = Modifier.clip(RoundedCornerShape(999.dp)).clickable { tab = index }
                ) {
                    Text(
                        text = texto,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        when (tab) {
            0 -> ProduccionScreen(
                viewModel = produccionViewModel,
                currentUserId = user.id,
                isAdmin = isAdmin,
                onOpenEpisodio = onOpenEpisodio,
                modifier = Modifier.weight(1f)
            )

            1 -> ReunionesScreen(
                viewModel = reunionesViewModel,
                currentUserId = user.id,
                isAdmin = isAdmin,
                modifier = Modifier.weight(1f)
            )

            2 -> IdeasScreen(
                viewModel = ideasViewModel,
                currentUserId = user.id,
                isAdmin = isAdmin,
                onOpenIdea = onOpenIdea,
                modifier = Modifier.weight(1f)
            )

            3 -> PlanificadorScreen(
                viewModel = planificadorViewModel,
                modifier = Modifier.weight(1f)
            )

            4 -> MetricasEquipoSection(
                viewModel = canalesViewModel,
                modifier = Modifier.weight(1f)
            )

            else -> BuzonScreen(
                viewModel = buzonViewModel,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MetricasEquipoSection(
    viewModel: CanalesViewModel,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState.metricas == null) viewModel.loadMetricas()
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
                items(actuales, key = { it.plataforma }) { metrica ->
                    MetricaEquipoCard(metrica)
                }
            }
        }
    }
}

@Composable
private fun MetricaEquipoCard(metrica: MetricaActual, modifier: Modifier = Modifier) {
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

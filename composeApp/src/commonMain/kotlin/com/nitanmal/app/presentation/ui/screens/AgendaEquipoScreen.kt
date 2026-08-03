package com.nitanmal.app.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.nitanmal.app.presentation.ui.components.atoms.glass
import com.nitanmal.app.presentation.viewmodel.BuzonViewModel
import com.nitanmal.app.presentation.viewmodel.CanalesViewModel
import com.nitanmal.app.presentation.viewmodel.IdeasViewModel
import com.nitanmal.app.presentation.viewmodel.PlanificadorViewModel
import com.nitanmal.app.presentation.viewmodel.ProduccionViewModel
import com.nitanmal.app.presentation.viewmodel.ReunionesViewModel

private data class SeccionAgenda(
    val emoji: String,
    val titulo: String,
    val resumen: String,
    val badge: Int = 0
)

/**
 * Agenda del equipo: plana de resumen con una grilla de cards grandes
 * (2 por fila); cada card abre su propia hoja con flecha para volver.
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
    var seccion by remember { mutableStateOf<Int?>(null) }

    val prodState by produccionViewModel.uiState.collectAsState()
    val reunionesState by reunionesViewModel.uiState.collectAsState()
    val ideasState by ideasViewModel.uiState.collectAsState()
    val planState by planificadorViewModel.uiState.collectAsState()
    val buzonState by buzonViewModel.uiState.collectAsState()

    // Carga ligera para poder mostrar los contadores del resumen.
    LaunchedEffect(Unit) {
        if (prodState.episodios.isEmpty()) produccionViewModel.load()
        if (reunionesState.reuniones.isEmpty()) reunionesViewModel.load()
        if (ideasState.notas.isEmpty()) ideasViewModel.load()
        if (planState.posts.isEmpty()) planificadorViewModel.load()
        if (buzonState.preguntas.isEmpty()) buzonViewModel.load()
    }

    val ideasActivas = ideasState.notas.count {
        it.estado == null || it.estado == "nueva" || it.estado == "revision"
    }
    val secciones = listOf(
        SeccionAgenda("🎬", strings.navProduccion, plural(prodState.episodios.size, "episodio", "episodios")),
        SeccionAgenda("🤝", strings.navReuniones, plural(reunionesState.proximas.size, "próxima", "próximas")),
        SeccionAgenda("💡", strings.navIdeas, plural(ideasActivas, "idea activa", "ideas activas")),
        SeccionAgenda("📆", strings.navPlanificador, plural(planState.posts.size, "post", "posts")),
        SeccionAgenda("📈", strings.navMetricas, "Seguidores por canal"),
        SeccionAgenda(
            "💬", strings.navBuzon,
            plural(buzonState.pendientes, "sin responder", "sin responder"),
            badge = buzonState.pendientes
        )
    )

    val actual = seccion
    if (actual == null) {
        // ── Plana de resumen ──
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = strings.navAgenda,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Todo el trabajo del staff en un solo lugar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(18.dp))
            secciones.chunked(2).forEachIndexed { fila, par ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    par.forEachIndexed { col, s ->
                        SeccionAgendaCard(
                            seccion = s,
                            onClick = { seccion = fila * 2 + col },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    } else {
        // ── Hoja de la sección con flecha para volver ──
        Column(modifier = modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                IconButton(onClick = { seccion = null }) {
                    Icon(
                        com.nitanmal.app.presentation.ui.icons.AppIcons2.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "${secciones[actual].emoji}  ${secciones[actual].titulo}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            when (actual) {
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
}

private fun plural(n: Int, singular: String, plurales: String) =
    if (n == 1) "1 $singular" else "$n $plurales"

/** Card grande de sección: mitad del ancho, casi cuadrada. */
@Composable
private fun SeccionAgendaCard(
    seccion: SeccionAgenda,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1.15f)
            .glass(22.dp)
            .clickable(onClick = onClick)
    ) {
        if (seccion.badge > 0) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
            ) {
                Text(
                    text = "${seccion.badge}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Text(
                text = seccion.emoji,
                style = MaterialTheme.typography.headlineMedium
            )
            Column {
                Text(
                    text = seccion.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = seccion.resumen,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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

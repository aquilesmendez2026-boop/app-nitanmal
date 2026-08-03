package com.nitanmal.app.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nitanmal.app.core.localization.rememberStrings
import com.nitanmal.app.core.util.todayIsoDate
import com.nitanmal.app.domain.model.Plantillas
import com.nitanmal.app.domain.model.User
import com.nitanmal.app.presentation.viewmodel.IdeasViewModel
import com.nitanmal.app.presentation.viewmodel.PlanificadorViewModel
import com.nitanmal.app.presentation.viewmodel.ProduccionViewModel

/** Ítem unificado de trabajo asignado, ordenado por fecha límite. */
private data class ItemTrabajo(
    val tipo: String,        // etapa | idea | post
    val titulo: String,
    val subtitulo: String,
    val fecha: String,       // "" = sin fecha (va al final)
    val overdue: Boolean,
    val onClick: () -> Unit
)

/** Todo lo asignado a mí (etapas, ideas, posts) por vencimiento. */
@Composable
fun MiTrabajoScreen(
    user: User,
    produccionViewModel: ProduccionViewModel,
    ideasViewModel: IdeasViewModel,
    planificadorViewModel: PlanificadorViewModel,
    onOpenEpisodio: (String) -> Unit,
    onOpenIdea: (String) -> Unit,
    onGoToPlanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val prodState by produccionViewModel.uiState.collectAsState()
    val ideasState by ideasViewModel.uiState.collectAsState()
    val planState by planificadorViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (prodState.episodios.isEmpty()) produccionViewModel.load()
        if (ideasState.notas.isEmpty()) ideasViewModel.load()
        if (planState.posts.isEmpty()) planificadorViewModel.load()
    }

    val hoy = todayIsoDate()
    val items = remember(prodState.episodios, ideasState.notas, planState.posts) {
        buildList {
            // Etapas de producción asignadas a mí y no aprobadas
            prodState.episodios.forEach { ep ->
                Plantillas.STAGES.forEach { stage ->
                    val etapa = ep.etapa(stage)
                    if (etapa.responsableId == user.id && etapa.estado != "aprobada") {
                        val fecha = etapa.fecha ?: ""
                        add(
                            ItemTrabajo(
                                tipo = "etapa",
                                titulo = "${Plantillas.LABELS[stage]} · ${ep.titulo}",
                                subtitulo = Plantillas.ESTADOS.firstOrNull { it.first == etapa.estado }?.second
                                    ?: "Pendiente",
                                fecha = fecha,
                                overdue = fecha.isNotBlank() && fecha < hoy,
                                onClick = { onOpenEpisodio(ep.id) }
                            )
                        )
                    }
                }
            }
            // Ideas asignadas a mí, activas
            ideasState.notas.forEach { nota ->
                if (nota.responsableId == user.id &&
                    (nota.estado == null || nota.estado == "nueva" || nota.estado == "revision")
                ) {
                    val fecha = nota.fechaObjetivo ?: ""
                    add(
                        ItemTrabajo(
                            tipo = "idea",
                            titulo = nota.titulo?.takeIf { it.isNotBlank() } ?: nota.contenido.take(50),
                            subtitulo = strings.trabajoIdea,
                            fecha = fecha,
                            overdue = fecha.isNotBlank() && fecha < hoy,
                            onClick = { onOpenIdea(nota.id) }
                        )
                    )
                }
            }
            // Posts del planificador asignados a mí, no publicados
            planState.posts.forEach { post ->
                if (post.responsableId == user.id &&
                    post.estado in listOf("sugerido", "borrador", "programado")
                ) {
                    val fecha = post.fecha?.take(10) ?: ""
                    add(
                        ItemTrabajo(
                            tipo = "post",
                            titulo = post.titulo?.takeIf { it.isNotBlank() } ?: post.copy.take(50),
                            subtitulo = "${strings.trabajoPost} · ${post.plataforma}",
                            fecha = fecha,
                            overdue = fecha.isNotBlank() && fecha < hoy,
                            onClick = onGoToPlanner
                        )
                    )
                }
            }
        }.sortedWith(compareBy({ it.fecha.isBlank() }, { it.fecha }))
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = strings.trabajoTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
        )

        if (items.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (prodState.isLoading || ideasState.isLoading || planState.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                } else {
                    Text(
                        text = strings.trabajoVacio,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(items) { item ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = item.onClick)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Text(
                                text = when (item.tipo) {
                                    "etapa" -> "🎬"
                                    "idea" -> "💡"
                                    else -> "📣"
                                },
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.titulo,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = listOfNotNull(
                                        item.subtitulo,
                                        item.fecha.takeIf { it.isNotBlank() }
                                    ).joinToString(" · "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                                )
                            }
                            if (item.overdue) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = strings.trabajoAtrasada,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

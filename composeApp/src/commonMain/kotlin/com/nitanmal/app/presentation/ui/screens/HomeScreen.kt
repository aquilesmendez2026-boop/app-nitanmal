package com.nitanmal.app.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.nitanmal.app.core.localization.rememberStrings
import com.nitanmal.app.core.util.formatFecha
import com.nitanmal.app.domain.model.NotaEstado
import com.nitanmal.app.domain.model.User
import com.nitanmal.app.presentation.ui.components.molecules.EstadoChip
import com.nitanmal.app.presentation.ui.icons.AppIcons2
import com.nitanmal.app.presentation.viewmodel.BuzonViewModel
import com.nitanmal.app.presentation.viewmodel.IdeasViewModel

/** Inicio: resumen del trabajo del equipo — ideas y buzón. */
@Composable
fun HomeScreen(
    user: User,
    ideasViewModel: IdeasViewModel,
    buzonViewModel: BuzonViewModel,
    onGoToIdeas: () -> Unit,
    onGoToBuzon: () -> Unit,
    onOpenIdea: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val ideasState by ideasViewModel.uiState.collectAsState()
    val buzonState by buzonViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (ideasState.notas.isEmpty()) ideasViewModel.load()
        if (buzonState.preguntas.isEmpty()) buzonViewModel.load()
    }

    val ideasActivas = ideasState.notas.count {
        NotaEstado.fromKey(it.estado) in listOf(NotaEstado.NUEVA, NotaEstado.REVISION, NotaEstado.APROBADA)
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Saludo
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                user.photoUrl?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                    Spacer(Modifier.width(12.dp))
                }
                Column {
                    Text(
                        text = "¡Hola, ${user.name.split(" ").firstOrNull() ?: ""}!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    user.role?.let { role ->
                        Text(
                            text = role.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Contadores
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    valor = "$ideasActivas",
                    etiqueta = strings.homeIdeasActivas,
                    icono = { Icon(AppIcons2.Lightbulb, null, tint = MaterialTheme.colorScheme.primary) },
                    onClick = onGoToIdeas,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    valor = "${buzonState.pendientes}",
                    etiqueta = strings.homePreguntasPendientes,
                    icono = { Icon(AppIcons2.Mail, null, tint = MaterialTheme.colorScheme.secondary) },
                    onClick = onGoToBuzon,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Últimas ideas
        item {
            SectionHeader(strings.homeUltimasIdeas, strings.homeVerTodas, onGoToIdeas)
        }
        item {
            val ultimas = ideasState.ordenadas.take(3)
            if (ultimas.isEmpty()) {
                EmptyHint(strings.homeSinIdeas)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ultimas.forEach { nota ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onOpenIdea(nota.id) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = nota.titulo?.takeIf { it.isNotBlank() }
                                            ?: nota.contenido.take(60),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = listOfNotNull(
                                            nota.createdByName?.takeIf { it.isNotBlank() },
                                            formatFecha(nota.createdAt).takeIf { it.isNotBlank() }
                                        ).joinToString(" · "),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                EstadoChip(estado = NotaEstado.fromKey(nota.estado))
                            }
                        }
                    }
                }
            }
        }

        // Último del buzón
        item {
            SectionHeader(strings.homeUltimasPreguntas, strings.homeVerTodas, onGoToBuzon)
        }
        item {
            val ultimasPreguntas = buzonState.preguntas.filterNot { it.answered }.take(3)
            if (ultimasPreguntas.isEmpty()) {
                EmptyHint(strings.homeSinPreguntas)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ultimasPreguntas.forEach { pregunta ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(onClick = onGoToBuzon)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                                Text(
                                    text = pregunta.contenido,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = listOfNotNull(
                                        pregunta.fromName ?: pregunta.fromEmail,
                                        formatFecha(pregunta.createdAt).takeIf { it.isNotBlank() }
                                    ).joinToString(" · "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    valor: String,
    etiqueta: String,
    icono: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            icono()
            Spacer(Modifier.height(8.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun SectionHeader(titulo: String, accion: String, onAccion: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = accion,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onAccion)
        )
    }
}

@Composable
private fun EmptyHint(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

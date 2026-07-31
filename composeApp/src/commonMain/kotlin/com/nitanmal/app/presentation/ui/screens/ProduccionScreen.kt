package com.nitanmal.app.presentation.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nitanmal.app.core.localization.rememberStrings
import com.nitanmal.app.core.util.formatFecha
import com.nitanmal.app.domain.model.Episodio
import com.nitanmal.app.domain.model.Plantillas
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalButton
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalTextField
import com.nitanmal.app.presentation.ui.icons.AppIcons2
import com.nitanmal.app.presentation.viewmodel.ProduccionViewModel

/** Color por estado de etapa (mismo esquema del web). */
@Composable
fun estadoEtapaColor(estado: String?): Color = when (estado) {
    "en_progreso" -> Color(0xFFf59e0b)
    "en_revision" -> Color(0xFF8b5cf6)
    "aprobada" -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProduccionScreen(
    viewModel: ProduccionViewModel,
    currentUserId: String,
    isAdmin: Boolean,
    onOpenEpisodio: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState.episodios.isEmpty()) viewModel.load()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading && uiState.episodios.isEmpty() -> {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            uiState.episodios.isEmpty() && uiState.error == null -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = strings.prodTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                    )
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = strings.prodVacio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = strings.prodTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(uiState.ordenados, key = { it.id }) { episodio ->
                        EpisodioCard(
                            episodio = episodio,
                            puedeBorrar = episodio.createdByUserId == currentUserId || isAdmin,
                            onClick = { onOpenEpisodio(episodio.id) },
                            onDelete = { viewModel.delete(episodio.id) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.setShowCreateSheet(true) },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(AppIcons2.Add, contentDescription = strings.prodNuevo)
        }

        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                action = { TextButton(onClick = { viewModel.clearError() }) { Text("OK") } }
            ) { Text(error) }
        }
    }

    if (uiState.showCreateSheet) {
        CreateEpisodioSheet(
            isCreating = uiState.isCreating,
            onDismiss = { viewModel.setShowCreateSheet(false) },
            onCreate = { titulo, idea -> viewModel.create(titulo, idea) }
        )
    }
}

@Composable
private fun EpisodioCard(
    episodio: Episodio,
    puedeBorrar: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    var confirmDelete by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = episodio.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${episodio.aprobadas}/${Plantillas.STAGES.size}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (episodio.aprobadas == Plantillas.STAGES.size) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                if (puedeBorrar) {
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = { confirmDelete = true }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            AppIcons2.Delete,
                            contentDescription = strings.prodBorrar,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Pipeline de etapas
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Plantillas.STAGES.forEach { stage ->
                    val color = estadoEtapaColor(episodio.etapa(stage).estado)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Row {
                Plantillas.STAGES.forEach { stage ->
                    Text(
                        text = Plantillas.LABELS[stage] ?: stage,
                        style = MaterialTheme.typography.labelSmall,
                        color = estadoEtapaColor(episodio.etapa(stage).estado),
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = listOfNotNull(
                    episodio.createdByName?.takeIf { it.isNotBlank() },
                    formatFecha(episodio.createdAt).takeIf { it.isNotBlank() }
                ).joinToString(" · "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            )
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(strings.prodBorrar) },
            text = { Text(episodio.titulo) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) {
                    Text("Borrar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateEpisodioSheet(
    isCreating: Boolean,
    onDismiss: () -> Unit,
    onCreate: (titulo: String, idea: String) -> Unit
) {
    val strings = rememberStrings()
    var titulo by remember { mutableStateOf("") }
    var idea by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = strings.prodNuevo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(16.dp))

            NitanmalTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text(strings.prodTituloLabel) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isCreating
            )

            Spacer(Modifier.height(12.dp))

            NitanmalTextField(
                value = idea,
                onValueChange = { idea = it },
                label = { Text(strings.prodIdeaLabel) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4,
                enabled = !isCreating
            )

            Spacer(Modifier.height(24.dp))

            NitanmalButton(
                text = strings.prodCrear,
                onClick = { onCreate(titulo.trim(), idea) },
                modifier = Modifier.fillMaxWidth(),
                isLoading = isCreating,
                enabled = !isCreating && titulo.isNotBlank()
            )
        }
    }
}

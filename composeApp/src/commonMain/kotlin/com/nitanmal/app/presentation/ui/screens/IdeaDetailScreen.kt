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
import com.nitanmal.app.core.util.formatFecha
import com.nitanmal.app.domain.model.NOTA_EMOJIS
import com.nitanmal.app.domain.model.NotaComentario
import com.nitanmal.app.domain.model.NotaEstado
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalTextField
import com.nitanmal.app.presentation.ui.components.molecules.EstadoChip
import com.nitanmal.app.presentation.ui.components.molecules.EtiquetaChip
import com.nitanmal.app.presentation.ui.components.molecules.ReaccionesRow
import com.nitanmal.app.presentation.ui.icons.AppIcons2
import com.nitanmal.app.presentation.viewmodel.IdeasViewModel

/** Detalle de una idea: contenido completo + comentarios. */
@Composable
fun IdeaDetailScreen(
    notaId: String,
    viewModel: IdeasViewModel,
    currentUserId: String,
    isAdmin: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val uiState by viewModel.uiState.collectAsState()
    val nota = uiState.notas.firstOrNull { it.id == notaId }

    if (nota == null) {
        // La idea ya no existe (borrada o estado perdido): volvemos.
        LaunchedEffect(Unit) { onNavigateBack() }
        return
    }

    var comentario by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {
        // Barra superior
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    AppIcons2.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = nota.titulo?.takeIf { it.isNotBlank() } ?: strings.ideasTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            EstadoChip(
                estado = NotaEstado.fromKey(nota.estado),
                onEstadoChange = { viewModel.setEstado(nota.id, it.key) }
            )
            Spacer(Modifier.width(8.dp))
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (!nota.etiquetas.isNullOrEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                nota.etiquetas.forEach { EtiquetaChip(it) }
                            }
                            Spacer(Modifier.height(10.dp))
                        }
                        if (nota.contenido.isNotBlank()) {
                            Text(
                                text = nota.contenido,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                        ReaccionesRow(
                            reacciones = nota.reacciones ?: emptyMap(),
                            currentUserId = currentUserId,
                            emojis = NOTA_EMOJIS,
                            onReaccionar = { viewModel.reaccionar(nota.id, it) }
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = listOfNotNull(
                                nota.createdByName?.takeIf { it.isNotBlank() },
                                formatFecha(nota.createdAt).takeIf { it.isNotBlank() }
                            ).joinToString(" · "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "${strings.ideasComentarios} (${nota.comentarios?.size ?: 0})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(nota.comentarios ?: emptyList(), key = { it.id }) { c ->
                ComentarioItem(
                    comentario = c,
                    puedeBorrar = c.userId == currentUserId || isAdmin,
                    onBorrar = { viewModel.borrarComentario(nota.id, c.id) }
                )
            }
        }

        // Input de comentario
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            NitanmalTextField(
                value = comentario,
                onValueChange = { comentario = it },
                placeholder = { Text(strings.ideasComentar) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = {
                    if (comentario.isNotBlank()) {
                        viewModel.comentar(nota.id, comentario.trim())
                        comentario = ""
                    }
                },
                enabled = comentario.isNotBlank(),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(AppIcons2.Send, contentDescription = strings.ideasComentar)
            }
        }
    }
}

@Composable
private fun ComentarioItem(
    comentario: NotaComentario,
    puedeBorrar: Boolean,
    onBorrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = comentario.nombre ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = formatFecha(comentario.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = comentario.texto,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (puedeBorrar) {
                IconButton(onClick = onBorrar, modifier = Modifier.size(28.dp)) {
                    Icon(
                        AppIcons2.Close,
                        contentDescription = "Borrar comentario",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

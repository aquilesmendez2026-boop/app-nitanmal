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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nitanmal.app.core.localization.rememberStrings
import com.nitanmal.app.core.util.formatFecha
import com.nitanmal.app.domain.model.NOTA_EMOJIS
import com.nitanmal.app.domain.model.NOTA_ETIQUETAS
import com.nitanmal.app.domain.model.Nota
import com.nitanmal.app.domain.model.NotaEstado
import com.nitanmal.app.domain.repository.AudioAdjunto
import com.nitanmal.app.domain.util.createAudioRecorder
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalButton
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalTextField
import com.nitanmal.app.presentation.ui.components.molecules.EstadoChip
import com.nitanmal.app.presentation.ui.components.molecules.EtiquetaChip
import com.nitanmal.app.presentation.ui.components.molecules.ReaccionesRow
import com.nitanmal.app.presentation.ui.icons.AppIcons2
import com.nitanmal.app.presentation.viewmodel.IdeasViewModel
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeasScreen(
    viewModel: IdeasViewModel,
    currentUserId: String,
    isAdmin: Boolean,
    onOpenIdea: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState.notas.isEmpty()) viewModel.load()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading && uiState.notas.isEmpty() -> {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            uiState.notas.isEmpty() && uiState.error == null -> {
                Text(
                    text = strings.ideasVacio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center).padding(32.dp)
                )
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.ordenadas, key = { it.id }) { nota ->
                        NotaCard(
                            nota = nota,
                            currentUserId = currentUserId,
                            isAdmin = isAdmin,
                            onClick = { onOpenIdea(nota.id) },
                            onReaccionar = { emoji -> viewModel.reaccionar(nota.id, emoji) },
                            onEstadoChange = { estado -> viewModel.setEstado(nota.id, estado.key) },
                            onTogglePin = { viewModel.togglePin(nota.id) },
                            onDelete = { viewModel.delete(nota.id) }
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
            Icon(AppIcons2.Add, contentDescription = strings.ideasNueva)
        }

        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) { Text("OK") }
                }
            ) { Text(error) }
        }

        uiState.info?.let { info ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                action = {
                    TextButton(onClick = { viewModel.clearInfo() }) {
                        Text("OK", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            ) { Text(info) }
        }
    }

    if (uiState.showCreateSheet) {
        CreateIdeaSheet(
            isCreating = uiState.isCreating,
            onDismiss = { viewModel.setShowCreateSheet(false) },
            onCreate = { titulo, contenido, etiquetas, audios ->
                viewModel.create(titulo, contenido, etiquetas, audios)
            }
        )
    }
}

@Composable
fun NotaCard(
    nota: Nota,
    currentUserId: String,
    isAdmin: Boolean,
    onClick: () -> Unit,
    onReaccionar: (String) -> Unit,
    onEstadoChange: (NotaEstado) -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val esAutor = nota.createdByUserId == currentUserId
    var confirmDelete by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera: estado + pin + borrar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EstadoChip(
                    estado = NotaEstado.fromKey(nota.estado),
                    onEstadoChange = onEstadoChange
                )
                nota.etiquetas?.take(2)?.forEach { etiqueta ->
                    Spacer(Modifier.width(6.dp))
                    EtiquetaChip(etiqueta)
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onTogglePin, modifier = Modifier.size(32.dp)) {
                    Icon(
                        AppIcons2.PushPin,
                        contentDescription = strings.ideasFijar,
                        tint = if (nota.pinned) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (esAutor || isAdmin) {
                    IconButton(onClick = { confirmDelete = true }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            AppIcons2.Delete,
                            contentDescription = strings.ideasBorrar,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            nota.titulo?.takeIf { it.isNotBlank() }?.let { titulo ->
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
            }

            if (nota.contenido.isNotBlank()) {
                Text(
                    text = nota.contenido,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(10.dp))
            }

            // Reacciones + comentarios + autor
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReaccionesRow(
                    reacciones = nota.reacciones ?: emptyMap(),
                    currentUserId = currentUserId,
                    emojis = NOTA_EMOJIS,
                    onReaccionar = onReaccionar,
                    modifier = Modifier.weight(1f)
                )
                MetaCounter(AppIcons2.PlayArrow, nota.audios?.count { !it.url.isNullOrBlank() } ?: 0)
                MetaCounter(AppIcons2.Image, nota.imagenes?.count { !it.url.isNullOrBlank() } ?: 0)
                MetaCounter(AppIcons2.Link, nota.enlaces?.size ?: 0)
                MetaCounter(AppIcons2.ChatBubble, nota.comentarios?.size ?: 0)
            }

            Spacer(Modifier.height(6.dp))
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

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(strings.ideasBorrar) },
            text = { Text(nota.titulo?.takeIf { it.isNotBlank() } ?: nota.contenido.take(80)) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) {
                    Text(strings.ideasBorrar, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") }
            }
        )
    }
}

/** Iconito + conteo (solo si > 0) para el pie de la tarjeta. */
@Composable
private fun MetaCounter(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int
) {
    if (count <= 0) return
    Spacer(Modifier.width(8.dp))
    Icon(
        icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        modifier = Modifier.size(14.dp)
    )
    Spacer(Modifier.width(3.dp))
    Text(
        text = "$count",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    )
}

/** Audio grabado en la hoja, pendiente de publicar. */
private data class GrabacionLocal(val bytes: ByteArray, val duracion: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateIdeaSheet(
    isCreating: Boolean,
    onDismiss: () -> Unit,
    onCreate: (titulo: String, contenido: String, etiquetas: List<String>, audios: List<AudioAdjunto>) -> Unit
) {
    val strings = rememberStrings()
    var titulo by remember { mutableStateOf("") }
    var contenido by remember { mutableStateOf("") }
    var seleccionadas by remember { mutableStateOf(setOf<String>()) }

    // ── Grabación de audio (se permiten varias, como en el web) ──
    val recorder = remember { createAudioRecorder() }
    var isRecording by remember { mutableStateOf(false) }
    var grabaciones by remember { mutableStateOf(listOf<GrabacionLocal>()) }
    var segundos by remember { mutableStateOf(0) }
    var micDenegado by remember { mutableStateOf(false) }

    LaunchedEffect(isRecording) {
        segundos = 0
        while (isRecording) {
            kotlinx.coroutines.delay(1000)
            segundos++
        }
    }
    DisposableEffect(Unit) {
        onDispose { recorder.cancel() }
    }

    ModalBottomSheet(onDismissRequest = {
        recorder.cancel()
        onDismiss()
    }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = strings.ideasNueva,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(16.dp))

            NitanmalTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text(strings.ideasTituloLabel) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isCreating
            )

            Spacer(Modifier.height(12.dp))

            NitanmalTextField(
                value = contenido,
                onValueChange = { contenido = it },
                label = { Text(strings.ideasContenidoLabel) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5,
                enabled = !isCreating
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = strings.ideasEtiquetas,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                NOTA_ETIQUETAS.take(3).forEach { (etiqueta, _) ->
                    EtiquetaChip(
                        etiqueta = etiqueta,
                        selected = etiqueta in seleccionadas,
                        onClick = {
                            seleccionadas = if (etiqueta in seleccionadas) {
                                seleccionadas - etiqueta
                            } else seleccionadas + etiqueta
                        }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                NOTA_ETIQUETAS.drop(3).forEach { (etiqueta, _) ->
                    EtiquetaChip(
                        etiqueta = etiqueta,
                        selected = etiqueta in seleccionadas,
                        onClick = {
                            seleccionadas = if (etiqueta in seleccionadas) {
                                seleccionadas - etiqueta
                            } else seleccionadas + etiqueta
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Audios grabados ──
            grabaciones.forEachIndexed { index, grabacion ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        AppIcons2.Mic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "${strings.ideasAudioListo} ${index + 1} (${grabacion.duracion}s)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { grabaciones = grabaciones.filterIndexed { i, _ -> i != index } },
                        modifier = Modifier.size(32.dp),
                        enabled = !isCreating
                    ) {
                        Icon(
                            AppIcons2.Delete,
                            contentDescription = "Descartar audio",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ── Grabando / botón grabar ──
            if (isRecording) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        AppIcons2.Mic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "${strings.ideasGrabando} ${segundos}s",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    FilledIconButton(
                        onClick = {
                            val bytes = recorder.stop()
                            if (bytes != null) {
                                grabaciones = grabaciones + GrabacionLocal(bytes, segundos)
                            }
                            isRecording = false
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(AppIcons2.Stop, contentDescription = "Detener", modifier = Modifier.size(16.dp))
                    }
                }
            } else {
                OutlinedButton(
                    onClick = {
                        micDenegado = false
                        recorder.requestPermission { granted ->
                            if (granted) {
                                if (recorder.start()) isRecording = true
                            } else {
                                micDenegado = true
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isCreating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(AppIcons2.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(strings.ideasGrabar)
                }
                if (micDenegado) {
                    Text(
                        text = strings.ideasMicPermiso,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            NitanmalButton(
                text = strings.ideasPublicar,
                onClick = {
                    var finales = grabaciones
                    if (isRecording) {
                        val bytes = recorder.stop()
                        if (bytes != null) finales = finales + GrabacionLocal(bytes, segundos)
                        isRecording = false
                        grabaciones = finales
                    }
                    val audios = finales.map {
                        AudioAdjunto(
                            bytes = it.bytes,
                            filename = "idea-${Random.nextLong().toString(16)}.${recorder.fileExtension}",
                            contentType = recorder.mimeType
                        )
                    }
                    onCreate(titulo, contenido, seleccionadas.toList(), audios)
                },
                modifier = Modifier.fillMaxWidth(),
                isLoading = isCreating,
                enabled = !isCreating &&
                    (titulo.isNotBlank() || contenido.isNotBlank() || grabaciones.isNotEmpty() || isRecording)
            )
        }
    }
}

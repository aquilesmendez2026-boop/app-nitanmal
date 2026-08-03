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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.nitanmal.app.core.localization.rememberStrings
import com.nitanmal.app.core.util.formatFecha
import com.nitanmal.app.domain.model.NOTA_EMOJIS
import com.nitanmal.app.domain.model.Nota
import com.nitanmal.app.domain.model.NotaComentario
import com.nitanmal.app.domain.model.NotaEstado
import com.nitanmal.app.domain.util.createAudioPlayer
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalTextField
import com.nitanmal.app.presentation.ui.components.molecules.EstadoChip
import com.nitanmal.app.presentation.ui.components.molecules.EtiquetaChip
import com.nitanmal.app.presentation.ui.components.molecules.ReaccionesRow
import com.nitanmal.app.presentation.ui.icons.AppIcons2
import com.nitanmal.app.presentation.viewmodel.IdeasViewModel

/** Detalle de una idea: contenido completo, media, enlaces y comentarios. */
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
    var confirmConvertir by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    val esAutor = nota.createdByUserId == currentUserId

    // Reproductor de audio (una pista a la vez)
    val audioPlayer = remember { createAudioPlayer() }
    var playingKey by remember { mutableStateOf<String?>(null) }
    DisposableEffect(Unit) {
        onDispose { audioPlayer.release() }
    }

    // Mientras haya transcripciones en curso, refrescamos cada 5 s
    // (el backend actualiza el estado del job al listar).
    val hayProcesando = nota.transcripciones?.values?.any { it.estado == "procesando" } == true
    LaunchedEffect(hayProcesando, nota.id) {
        while (hayProcesando) {
            kotlinx.coroutines.delay(5000)
            viewModel.refresh()
        }
    }

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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (esAutor || isAdmin) {
                IconButton(onClick = { showEditSheet = true }) {
                    Icon(
                        AppIcons2.Edit,
                        contentDescription = strings.ideasEditar,
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
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

            // Imágenes
            val imagenes = nota.imagenes?.filter { !it.url.isNullOrBlank() } ?: emptyList()
            items(imagenes, key = { "img-${it.key}" }) { imagen ->
                AsyncImage(
                    model = imagen.url,
                    contentDescription = imagen.nombre,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            // Audios
            val audios = nota.audios?.filter { !it.url.isNullOrBlank() } ?: emptyList()
            if (audios.isNotEmpty()) {
                item {
                    Text(
                        text = strings.ideasAudios,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                items(audios, key = { "audio-${it.key}" }) { audio ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                val isPlaying = playingKey == audio.key
                                FilledIconButton(
                                    onClick = {
                                        if (isPlaying) {
                                            audioPlayer.stop()
                                            playingKey = null
                                        } else {
                                            playingKey = audio.key
                                            audioPlayer.play(audio.url!!) { playingKey = null }
                                        }
                                    },
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        if (isPlaying) AppIcons2.Stop else AppIcons2.PlayArrow,
                                        contentDescription = if (isPlaying) "Detener" else "Reproducir",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = audio.nombre?.takeIf { it.isNotBlank() } ?: "Audio",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // ── Transcripción del audio ──
                            val tx = nota.transcripciones?.get(audio.key)
                            when {
                                tx == null -> {
                                    TextButton(
                                        onClick = { viewModel.transcribir(nota.id, audio.key) },
                                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                                    ) {
                                        Text(
                                            text = strings.ideasTranscribir,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                tx.estado == "procesando" -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = strings.ideasTranscribiendo,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }

                                tx.estado == "listo" && tx.texto.isNotBlank() -> {
                                    Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                                        Text(
                                            text = strings.ideasTranscripcion,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = tx.texto,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }
                                }

                                tx.estado == "error" -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 14.dp, bottom = 4.dp)
                                    ) {
                                        Text(
                                            text = strings.ideasTranscripcionError,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        TextButton(onClick = { viewModel.transcribir(nota.id, audio.key) }) {
                                            Text(
                                                text = strings.ideasTranscribir,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Enlaces
            val enlaces = nota.enlaces ?: emptyList()
            if (enlaces.isNotEmpty()) {
                item {
                    Text(
                        text = strings.ideasEnlaces,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                items(enlaces, key = { "link-$it" }) { enlace ->
                    EnlaceItem(enlace)
                }
            }

            // Convertir a episodio
            item {
                ConvertirSection(
                    nota = nota,
                    onConvertir = { confirmConvertir = true }
                )
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

    if (confirmConvertir) {
        AlertDialog(
            onDismissRequest = { confirmConvertir = false },
            title = { Text(strings.ideasConvertir) },
            text = { Text(strings.ideasConvertirConfirm) },
            confirmButton = {
                TextButton(onClick = {
                    confirmConvertir = false
                    viewModel.convertir(nota.id)
                }) {
                    Text(strings.ideasConvertir, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmConvertir = false }) { Text("Cancelar") }
            }
        )
    }

    if (showEditSheet) {
        EditIdeaSheet(
            nota = nota,
            onDismiss = { showEditSheet = false },
            onGuardar = { titulo, contenido, etiquetas, enlaces ->
                viewModel.editar(nota.id, titulo, contenido, etiquetas, enlaces)
                showEditSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditIdeaSheet(
    nota: com.nitanmal.app.domain.model.Nota,
    onDismiss: () -> Unit,
    onGuardar: (titulo: String, contenido: String, etiquetas: List<String>, enlaces: List<String>) -> Unit
) {
    val strings = rememberStrings()
    var titulo by remember { mutableStateOf(nota.titulo ?: "") }
    var contenido by remember { mutableStateOf(nota.contenido) }
    var seleccionadas by remember { mutableStateOf(nota.etiquetas?.toSet() ?: emptySet()) }
    var enlacesTexto by remember { mutableStateOf(nota.enlaces?.joinToString("\n") ?: "") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = strings.ideasEditar,
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
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            NitanmalTextField(
                value = contenido,
                onValueChange = { contenido = it },
                label = { Text(strings.ideasContenidoLabel) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5
            )

            Spacer(Modifier.height(12.dp))

            NitanmalTextField(
                value = enlacesTexto,
                onValueChange = { enlacesTexto = it },
                label = { Text(strings.ideasEnlacesHint) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = strings.ideasEtiquetas,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            com.nitanmal.app.domain.model.NOTA_ETIQUETAS.chunked(3).forEach { fila ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    fila.forEach { (etiqueta, _) ->
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
            }

            Spacer(Modifier.height(16.dp))

            com.nitanmal.app.presentation.ui.components.atoms.NitanmalButton(
                text = strings.ideasGuardar,
                onClick = {
                    val enlaces = enlacesTexto.lines()
                        .map { it.trim() }
                        .filter { it.startsWith("http") }
                    onGuardar(titulo, contenido, seleccionadas.toList(), enlaces)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = titulo.isNotBlank() || contenido.isNotBlank()
            )
        }
    }
}

@Composable
private fun EnlaceItem(enlace: String, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable {
                runCatching { uriHandler.openUri(enlace) }
            }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Icon(
            AppIcons2.Link,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = enlace,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ConvertirSection(
    nota: Nota,
    onConvertir: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val convertida = NotaEstado.fromKey(nota.estado) == NotaEstado.CONVERTIDA

    if (convertida) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = androidx.compose.ui.graphics.Color(0xFF8b5cf6).copy(alpha = 0.12f),
            modifier = modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Icon(
                    AppIcons2.Movie,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color(0xFF8b5cf6),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Idea convertida en episodio 🎬",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = androidx.compose.ui.graphics.Color(0xFF8b5cf6)
                )
            }
        }
    } else {
        OutlinedButton(
            onClick = onConvertir,
            shape = RoundedCornerShape(12.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            Icon(
                AppIcons2.Movie,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(strings.ideasConvertir)
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

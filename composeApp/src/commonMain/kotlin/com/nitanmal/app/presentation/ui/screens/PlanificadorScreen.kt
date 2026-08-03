package com.nitanmal.app.presentation.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.nitanmal.app.core.localization.rememberStrings
import com.nitanmal.app.core.util.formatFecha
import com.nitanmal.app.domain.model.PLAN_ESTADOS
import com.nitanmal.app.domain.model.PLATAFORMAS
import com.nitanmal.app.domain.model.Post
import com.nitanmal.app.domain.model.plataformaLabel
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalButton
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalTextField
import com.nitanmal.app.presentation.ui.components.molecules.EtiquetaChip
import com.nitanmal.app.presentation.ui.icons.AppIcons2
import com.nitanmal.app.presentation.viewmodel.PlanificadorViewModel

@Composable
fun planEstadoColor(estado: String?): Color =
    PLAN_ESTADOS.firstOrNull { it.first == estado }?.third?.let { Color(it) }
        ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanificadorScreen(
    viewModel: PlanificadorViewModel,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState.posts.isEmpty()) viewModel.load()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 8.dp)
            ) {
                Spacer(Modifier.weight(1f))
                // Generar con IA
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.setShowGenerarSheet(true) }
                ) {
                    Text(
                        text = "✨ ${strings.planGenerar}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            // Filtros por estado
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                FilterChip(
                    selected = uiState.filtro == null,
                    onClick = { viewModel.setFiltro(null) },
                    label = { Text(strings.planTodos) }
                )
                PLAN_ESTADOS.forEach { (key, label, colorLong) ->
                    val color = Color(colorLong)
                    FilterChip(
                        selected = uiState.filtro == key,
                        onClick = { viewModel.setFiltro(if (uiState.filtro == key) null else key) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(alpha = 0.18f),
                            selectedLabelColor = color
                        )
                    )
                }
            }

            when {
                uiState.isLoading && uiState.posts.isEmpty() -> {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                uiState.filtrados.isEmpty() -> {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = strings.planVacio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(uiState.filtrados, key = { it.id }) { post ->
                            PostCard(
                                post = post,
                                onEstadoChange = { estado -> viewModel.setEstado(post.id, estado) },
                                onDelete = { viewModel.delete(post.id) }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.setShowCrearSheet(true) },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(AppIcons2.Add, contentDescription = strings.planCrear)
        }

        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                action = { TextButton(onClick = { viewModel.clearError() }) { Text("OK") } }
            ) { Text(error) }
        }

        uiState.info?.let { info ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White,
                action = {
                    TextButton(onClick = { viewModel.clearInfo() }) {
                        Text("OK", color = Color.White)
                    }
                }
            ) { Text(info) }
        }
    }

    if (uiState.showGenerarSheet) {
        GenerarSheet(
            isGenerating = uiState.isGenerating,
            onDismiss = { viewModel.setShowGenerarSheet(false) },
            onGenerar = { tema, tono, cta, plataformas ->
                viewModel.generar(tema, tono, cta, plataformas)
            }
        )
    }

    if (uiState.showCrearSheet) {
        CrearPostSheet(
            isSaving = uiState.isSaving,
            onDismiss = { viewModel.setShowCrearSheet(false) },
            onCrear = { plataforma, titulo, copy, fecha ->
                viewModel.crear(plataforma, titulo, copy, fecha)
            }
        )
    }
}

@Composable
private fun PostCard(
    post: Post,
    onEstadoChange: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    var menuOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var expandido by remember { mutableStateOf(false) }
    val color = planEstadoColor(post.estado)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { expandido = !expandido }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Plataforma
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = plataformaLabel(post.plataforma),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                if (post.generadoPorIA) {
                    Spacer(Modifier.width(6.dp))
                    Text(text = "✨", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.weight(1f))
                // Estado con menú
                Box {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = color.copy(alpha = 0.15f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { menuOpen = true }
                    ) {
                        Text(
                            text = PLAN_ESTADOS.firstOrNull { it.first == post.estado }?.second ?: post.estado,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = color,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        PLAN_ESTADOS.forEach { (key, label, colorLong) ->
                            DropdownMenuItem(
                                text = { Text(label, color = Color(colorLong)) },
                                onClick = {
                                    menuOpen = false
                                    if (key != post.estado) onEstadoChange(key)
                                }
                            )
                        }
                    }
                }
                IconButton(onClick = { confirmDelete = true }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        AppIcons2.Delete,
                        contentDescription = strings.planBorrar,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            post.titulo?.takeIf { it.isNotBlank() }?.let { titulo ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (post.copy.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = post.copy,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (expandido) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!post.hashtags.isNullOrEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = post.hashtags.joinToString(" ") { if (it.startsWith("#")) it else "#$it" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = if (expandido) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (expandido && !post.assetUrl.isNullOrBlank() && post.assetTipo == "image") {
                Spacer(Modifier.height(10.dp))
                AsyncImage(
                    model = post.assetUrl,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = listOfNotNull(
                    post.fecha?.takeIf { it.isNotBlank() },
                    post.createdByName?.takeIf { it.isNotBlank() },
                    formatFecha(post.createdAt).takeIf { it.isNotBlank() }
                ).joinToString(" · "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            )
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(strings.planBorrar) },
            text = { Text(post.copy.take(120)) },
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
private fun GenerarSheet(
    isGenerating: Boolean,
    onDismiss: () -> Unit,
    onGenerar: (tema: String, tono: String, cta: String, plataformas: List<String>) -> Unit
) {
    val strings = rememberStrings()
    var tema by remember { mutableStateOf("") }
    var tono by remember { mutableStateOf("") }
    var cta by remember { mutableStateOf("") }
    var plataformas by remember { mutableStateOf(setOf<String>()) }

    ModalBottomSheet(onDismissRequest = { if (!isGenerating) onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "✨ ${strings.planGenerar}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = strings.planGenerarDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            NitanmalTextField(
                value = tema,
                onValueChange = { tema = it },
                label = { Text(strings.planTema) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                enabled = !isGenerating
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NitanmalTextField(
                    value = tono,
                    onValueChange = { tono = it },
                    label = { Text(strings.planTono) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isGenerating
                )
                NitanmalTextField(
                    value = cta,
                    onValueChange = { cta = it },
                    label = { Text(strings.planCta) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isGenerating
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = strings.planPlataformas,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            PLATAFORMAS.chunked(5).forEach { fila ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    fila.forEach { p ->
                        EtiquetaChip(
                            etiqueta = plataformaLabel(p),
                            selected = p in plataformas,
                            onClick = {
                                plataformas = if (p in plataformas) plataformas - p else plataformas + p
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (isGenerating) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                        .padding(14.dp)
                ) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = strings.planGenerando,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                NitanmalButton(
                    text = strings.planGenerar,
                    onClick = { onGenerar(tema, tono, cta, plataformas.toList()) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = tema.isNotBlank() && plataformas.isNotEmpty()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CrearPostSheet(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onCrear: (plataforma: String, titulo: String, copy: String, fecha: String) -> Unit
) {
    val strings = rememberStrings()
    var plataforma by remember { mutableStateOf(PLATAFORMAS.first()) }
    var titulo by remember { mutableStateOf("") }
    var copy by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = strings.planCrear,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = strings.planPlataformas,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            PLATAFORMAS.chunked(5).forEach { fila ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    fila.forEach { p ->
                        EtiquetaChip(
                            etiqueta = plataformaLabel(p),
                            selected = p == plataforma,
                            onClick = { plataforma = p }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            NitanmalTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text(strings.ideasTituloLabel) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSaving
            )

            Spacer(Modifier.height(12.dp))

            NitanmalTextField(
                value = copy,
                onValueChange = { copy = it },
                label = { Text(strings.planCopyLabel) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5,
                enabled = !isSaving
            )

            Spacer(Modifier.height(12.dp))

            NitanmalTextField(
                value = fecha,
                onValueChange = { fecha = it },
                label = { Text(strings.planFechaLabel) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSaving
            )

            Spacer(Modifier.height(24.dp))

            NitanmalButton(
                text = strings.planCrear,
                onClick = { onCrear(plataforma, titulo, copy, fecha) },
                modifier = Modifier.fillMaxWidth(),
                isLoading = isSaving,
                enabled = !isSaving && copy.isNotBlank()
            )
        }
    }
}

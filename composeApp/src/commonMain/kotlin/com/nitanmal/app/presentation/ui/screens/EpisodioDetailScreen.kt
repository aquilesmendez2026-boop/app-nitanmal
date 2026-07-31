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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nitanmal.app.core.localization.rememberStrings
import com.nitanmal.app.data.remote.model.StageDataInput
import com.nitanmal.app.domain.model.CampoTipo
import com.nitanmal.app.domain.model.Episodio
import com.nitanmal.app.domain.model.MiembroEquipo
import com.nitanmal.app.domain.model.Plantillas
import com.nitanmal.app.domain.model.Subtarea
import com.nitanmal.app.domain.model.boolValue
import com.nitanmal.app.domain.model.fileValue
import com.nitanmal.app.domain.model.stringValue
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalButton
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalTextField
import com.nitanmal.app.presentation.ui.icons.AppIcons2
import com.nitanmal.app.presentation.viewmodel.ProduccionViewModel
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.random.Random

/** Detalle de un episodio: pipeline de etapas editables. */
@Composable
fun EpisodioDetailScreen(
    episodioId: String,
    viewModel: ProduccionViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val uiState by viewModel.uiState.collectAsState()
    val episodio = uiState.episodios.firstOrNull { it.id == episodioId }

    if (episodio == null) {
        LaunchedEffect(Unit) { onNavigateBack() }
        return
    }

    var expandedStage by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                    text = episodio.titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${episodio.aprobadas}/${Plantillas.STAGES.size}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(Plantillas.STAGES, key = { it }) { stage ->
                    EtapaCard(
                        episodio = episodio,
                        stage = stage,
                        equipo = uiState.equipo,
                        expanded = expandedStage == stage,
                        isSaving = uiState.isSaving,
                        onToggle = {
                            expandedStage = if (expandedStage == stage) null else stage
                        },
                        onGuardar = { data ->
                            viewModel.updateStage(episodio.id, stage, data) {
                                expandedStage = null
                            }
                        }
                    )
                }
            }
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
}

@Composable
private fun EtapaCard(
    episodio: Episodio,
    stage: String,
    equipo: List<MiembroEquipo>,
    expanded: Boolean,
    isSaving: Boolean,
    onToggle: () -> Unit,
    onGuardar: (StageDataInput) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val etapa = episodio.etapa(stage)
    val color = estadoEtapaColor(etapa.estado)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            // Cabecera
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(color)
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Plantillas.LABELS[stage] ?: stage,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val sub = listOfNotNull(
                        etapa.responsable?.takeIf { it.isNotBlank() },
                        etapa.fecha?.takeIf { it.isNotBlank() }
                    ).joinToString(" · ")
                    if (sub.isNotBlank()) {
                        Text(
                            text = sub,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        )
                    }
                }
                Text(
                    text = Plantillas.ESTADOS.firstOrNull { it.first == etapa.estado }?.second ?: "Pendiente",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
            }

            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                EtapaEditor(
                    episodio = episodio,
                    stage = stage,
                    equipo = equipo,
                    isSaving = isSaving,
                    onGuardar = onGuardar,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EtapaEditor(
    episodio: Episodio,
    stage: String,
    equipo: List<MiembroEquipo>,
    isSaving: Boolean,
    onGuardar: (StageDataInput) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val etapa = episodio.etapa(stage)
    val campos = Plantillas.CAMPOS[stage] ?: emptyList()
    val uriHandler = LocalUriHandler.current

    // Estado editable inicializado desde la etapa actual
    var estado by remember(episodio.id, stage) { mutableStateOf(etapa.estado ?: "pendiente") }
    var responsable by remember(episodio.id, stage) { mutableStateOf(etapa.responsable ?: "") }
    var responsableId by remember(episodio.id, stage) { mutableStateOf(etapa.responsableId ?: "") }
    var fecha by remember(episodio.id, stage) { mutableStateOf(etapa.fecha ?: "") }
    var subtareas by remember(episodio.id, stage) { mutableStateOf(etapa.subtareas ?: emptyList()) }
    var nuevaSubtarea by remember(episodio.id, stage) { mutableStateOf("") }

    // Valores tipados editables (los file no se editan aquí)
    val textValues = remember(episodio.id, stage) {
        mutableStateMapOf<String, String>().apply {
            campos.filter { it.tipo != CampoTipo.FILE && it.tipo != CampoTipo.CHECKBOX }
                .forEach { put(it.key, etapa.values.stringValue(it.key)) }
        }
    }
    val boolValues = remember(episodio.id, stage) {
        mutableStateMapOf<String, Boolean>().apply {
            campos.filter { it.tipo == CampoTipo.CHECKBOX }
                .forEach { put(it.key, etapa.values.boolValue(it.key)) }
        }
    }

    Column(modifier = modifier) {
        Text(
            text = Plantillas.ENTREGAS[stage] ?: "",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )

        Spacer(Modifier.height(12.dp))

        // Estado
        DropdownField(
            label = "Estado",
            value = Plantillas.ESTADOS.firstOrNull { it.first == estado }?.second ?: estado,
            options = Plantillas.ESTADOS.map { it.second },
            onSelect = { index -> estado = Plantillas.ESTADOS[index].first }
        )

        Spacer(Modifier.height(10.dp))

        // Responsable (del equipo)
        DropdownField(
            label = strings.prodResponsable,
            value = responsable.ifBlank { "—" },
            options = equipo.map { it.nombre },
            onSelect = { index ->
                responsable = equipo[index].nombre
                responsableId = equipo[index].userId
            }
        )

        Spacer(Modifier.height(10.dp))

        NitanmalTextField(
            value = fecha,
            onValueChange = { fecha = it },
            label = { Text(strings.prodFecha) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isSaving
        )

        // Campos de la plantilla
        campos.forEach { campo ->
            Spacer(Modifier.height(10.dp))
            when (campo.tipo) {
                CampoTipo.CHECKBOX -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = campo.label + if (campo.required) " *" else "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = boolValues[campo.key] == true,
                            onCheckedChange = { boolValues[campo.key] = it },
                            enabled = !isSaving
                        )
                    }
                }

                CampoTipo.FILE -> {
                    val archivo = etapa.values.fileValue(campo.key)
                    Column {
                        Text(
                            text = campo.label + if (campo.required) " *" else "",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        if (archivo != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                    .clickable(enabled = archivo.second != null) {
                                        archivo.second?.let { runCatching { uriHandler.openUri(it) } }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    AppIcons2.Link,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = archivo.first,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Text(
                                text = strings.prodArchivoWeb,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                            )
                        }
                    }
                }

                CampoTipo.SELECT -> {
                    DropdownField(
                        label = campo.label + if (campo.required) " *" else "",
                        value = textValues[campo.key]?.ifBlank { "—" } ?: "—",
                        options = campo.options,
                        onSelect = { index -> textValues[campo.key] = campo.options[index] }
                    )
                }

                else -> {
                    NitanmalTextField(
                        value = textValues[campo.key] ?: "",
                        onValueChange = { textValues[campo.key] = it },
                        label = { Text(campo.label + if (campo.required) " *" else "") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = campo.tipo != CampoTipo.TEXTO_LARGO,
                        maxLines = if (campo.tipo == CampoTipo.TEXTO_LARGO) 4 else 1,
                        enabled = !isSaving
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Subtareas
        Text(
            text = strings.prodSubtareas,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        subtareas.forEach { sub ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = sub.hecha,
                    onCheckedChange = { hecha ->
                        subtareas = subtareas.map {
                            if (it.id == sub.id) it.copy(hecha = hecha) else it
                        }
                    },
                    enabled = !isSaving
                )
                Text(
                    text = sub.texto,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (sub.hecha) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { subtareas = subtareas.filterNot { it.id == sub.id } },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        AppIcons2.Close,
                        contentDescription = "Quitar",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            NitanmalTextField(
                value = nuevaSubtarea,
                onValueChange = { nuevaSubtarea = it },
                placeholder = { Text(strings.prodAgregarSubtarea) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                enabled = !isSaving
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = {
                    if (nuevaSubtarea.isNotBlank()) {
                        subtareas = subtareas + Subtarea(
                            id = "st-${Random.nextLong().toString(16)}",
                            texto = nuevaSubtarea.trim()
                        )
                        nuevaSubtarea = ""
                    }
                },
                enabled = nuevaSubtarea.isNotBlank() && !isSaving,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(AppIcons2.Add, contentDescription = strings.prodAgregarSubtarea, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        NitanmalButton(
            text = strings.prodGuardar,
            onClick = {
                val values = buildJsonObject {
                    campos.forEach { campo ->
                        when (campo.tipo) {
                            CampoTipo.FILE -> Unit // se gestiona desde el web
                            CampoTipo.CHECKBOX ->
                                put(campo.key, JsonPrimitive(boolValues[campo.key] == true))
                            else ->
                                put(campo.key, JsonPrimitive(textValues[campo.key] ?: ""))
                        }
                    }
                }
                onGuardar(
                    StageDataInput(
                        estado = estado,
                        responsable = responsable,
                        responsableId = responsableId,
                        fecha = fecha,
                        subtareas = subtareas,
                        values = values
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            isLoading = isSaving,
            enabled = !isSaving
        )
    }
}

/** Campo desplegable simple (label + valor + menú). */
@Composable
private fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var open by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    .clickable(enabled = options.isNotEmpty()) { open = true }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "▾",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                options.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            open = false
                            onSelect(index)
                        }
                    )
                }
            }
        }
    }
}

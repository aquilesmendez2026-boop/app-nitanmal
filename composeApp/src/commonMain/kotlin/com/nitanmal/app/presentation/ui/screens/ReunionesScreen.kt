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
import com.nitanmal.app.domain.model.Reunion
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalButton
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalTextField
import com.nitanmal.app.presentation.ui.icons.AppIcons2
import com.nitanmal.app.presentation.viewmodel.ReunionesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReunionesScreen(
    viewModel: ReunionesViewModel,
    currentUserId: String,
    isAdmin: Boolean,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState.reuniones.isEmpty()) viewModel.load()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading && uiState.reuniones.isEmpty() -> {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            uiState.reuniones.isEmpty() -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Titulo(strings.reuTitle)
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = strings.reuVacio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item { Titulo(strings.reuTitle) }

                    if (uiState.proximas.isNotEmpty()) {
                        item { Seccion(strings.reuProximas) }
                        items(uiState.proximas, key = { it.id }) { reunion ->
                            ReunionCard(
                                reunion = reunion,
                                destacada = true,
                                puedeBorrar = reunion.createdByUserId == currentUserId || isAdmin,
                                onDelete = { viewModel.delete(reunion.id) }
                            )
                        }
                    }

                    if (uiState.pasadas.isNotEmpty()) {
                        item { Seccion(strings.reuPasadas) }
                        items(uiState.pasadas, key = { it.id }) { reunion ->
                            ReunionCard(
                                reunion = reunion,
                                destacada = false,
                                puedeBorrar = reunion.createdByUserId == currentUserId || isAdmin,
                                onDelete = { viewModel.delete(reunion.id) }
                            )
                        }
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
            Icon(AppIcons2.Add, contentDescription = strings.reuNueva)
        }

        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) { Text("OK") }
                }
            ) { Text(error) }
        }
    }

    if (uiState.showCreateSheet) {
        CreateReunionSheet(
            isCreating = uiState.isCreating,
            onDismiss = { viewModel.setShowCreateSheet(false) },
            onCreate = { date, time, title, description, lugar ->
                viewModel.create(date, time, title, description, lugar)
            }
        )
    }
}

@Composable
private fun Titulo(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun Seccion(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun ReunionCard(
    reunion: Reunion,
    destacada: Boolean,
    puedeBorrar: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmDelete by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (destacada) 2.dp else 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bloque fecha/hora
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (destacada) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    // "2026-08-05" → día "05" y mes "08"
                    val dia = reunion.date.takeLast(2)
                    val mes = reunion.date.drop(5).take(2)
                    Text(
                        text = dia,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (destacada) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = nombreMes(mes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reunion.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = listOfNotNull(
                        reunion.time.takeIf { it.isNotBlank() },
                        reunion.lugar?.takeIf { it.isNotBlank() }
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                reunion.description?.takeIf { it.isNotBlank() }?.let { desc ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }
            }

            if (puedeBorrar) {
                IconButton(onClick = { confirmDelete = true }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        AppIcons2.Delete,
                        contentDescription = "Borrar",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Borrar reunión") },
            text = { Text(reunion.title) },
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

private fun nombreMes(mes: String): String = when (mes) {
    "01" -> "ENE"; "02" -> "FEB"; "03" -> "MAR"; "04" -> "ABR"
    "05" -> "MAY"; "06" -> "JUN"; "07" -> "JUL"; "08" -> "AGO"
    "09" -> "SEP"; "10" -> "OCT"; "11" -> "NOV"; "12" -> "DIC"
    else -> mes
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateReunionSheet(
    isCreating: Boolean,
    onDismiss: () -> Unit,
    onCreate: (date: String, time: String, title: String, description: String, lugar: String) -> Unit
) {
    val strings = rememberStrings()
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var lugar by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val dateOk = Regex("""\d{4}-\d{2}-\d{2}""").matches(date)
    val timeOk = Regex("""\d{2}:\d{2}""").matches(time)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = strings.reuNueva,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(16.dp))

            NitanmalTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(strings.reuTituloLabel) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isCreating
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NitanmalTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text(strings.reuFechaLabel) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    isError = date.isNotBlank() && !dateOk,
                    enabled = !isCreating
                )
                NitanmalTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text(strings.reuHoraLabel) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    isError = time.isNotBlank() && !timeOk,
                    enabled = !isCreating
                )
            }

            Spacer(Modifier.height(12.dp))

            NitanmalTextField(
                value = lugar,
                onValueChange = { lugar = it },
                label = { Text(strings.reuLugarLabel) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isCreating
            )

            Spacer(Modifier.height(12.dp))

            NitanmalTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(strings.reuDescripcionLabel) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                enabled = !isCreating
            )

            Spacer(Modifier.height(24.dp))

            NitanmalButton(
                text = strings.reuCrear,
                onClick = { onCreate(date, time, title.trim(), description, lugar) },
                modifier = Modifier.fillMaxWidth(),
                isLoading = isCreating,
                enabled = !isCreating && title.isNotBlank() && dateOk && timeOk
            )
        }
    }
}

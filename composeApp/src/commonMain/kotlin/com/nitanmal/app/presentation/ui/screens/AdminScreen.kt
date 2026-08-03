package com.nitanmal.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nitanmal.app.core.localization.rememberStrings
import com.nitanmal.app.domain.model.LiveState
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalButton
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalTextField
import com.nitanmal.app.presentation.viewmodel.AdminViewModel

/** Panel admin móvil: solo lo urgente — en vivo y sorteos. */
@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    var isLive by remember(uiState.live) { mutableStateOf(uiState.live?.isLive ?: false) }
    var videoId by remember(uiState.live) { mutableStateOf(uiState.live?.videoId ?: "") }
    var titulo by remember(uiState.live) { mutableStateOf(uiState.live?.title ?: "") }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text(
                    text = strings.adminTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // ── En vivo ──
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🔴 ${strings.adminEnVivo}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = isLive,
                                onCheckedChange = { isLive = it },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = Color(0xFFdc2626)
                                )
                            )
                        }
                        if (isLive) {
                            Text(
                                text = strings.adminTransmitiendo,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFdc2626),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        NitanmalTextField(
                            value = videoId,
                            onValueChange = { videoId = it },
                            label = { Text(strings.adminVideoId) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(10.dp))
                        NitanmalTextField(
                            value = titulo,
                            onValueChange = { titulo = it },
                            label = { Text(strings.adminTituloLive) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(14.dp))
                        NitanmalButton(
                            text = strings.adminGuardar,
                            onClick = {
                                viewModel.guardarLive(
                                    LiveState(isLive = isLive, videoId = videoId.trim(), title = titulo.trim())
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            isLoading = uiState.isSavingLive
                        )
                    }
                }
            }

            // ── Sorteos ──
            if (uiState.sorteos.isNotEmpty()) {
                item {
                    Text(
                        text = "🎁 ${strings.adminSorteos}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                items(uiState.sorteos, key = { it.id }) { sorteo ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = sorteo.titulo,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (sorteo.activo) {
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                    }
                                ) {
                                    Text(
                                        text = if (sorteo.activo) "Activo" else "Cerrado",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (sorteo.activo) MaterialTheme.colorScheme.tertiary
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${sorteo.participantes} participantes · ${sorteo.premio}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            uiState.ganadores[sorteo.id]?.let { ganador ->
                                Spacer(Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "🏆 ${strings.adminGanador}: ${ganador.nombre} (${ganador.email})",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = { viewModel.toggleSorteo(sorteo.id) },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(if (sorteo.activo) strings.adminCerrar else strings.adminAbrir)
                                }
                                if (sorteo.participantes > 0) {
                                    Button(
                                        onClick = { viewModel.elegirGanador(sorteo.id) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondary
                                        )
                                    ) {
                                        Text(strings.adminElegirGanador, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
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
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = Color.White,
                action = {
                    TextButton(onClick = { viewModel.clearInfo() }) {
                        Text("OK", color = Color.White)
                    }
                }
            ) { Text(info) }
        }
    }
}

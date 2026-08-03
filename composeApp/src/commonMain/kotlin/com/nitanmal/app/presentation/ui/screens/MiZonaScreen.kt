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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nitanmal.app.core.localization.rememberStrings
import com.nitanmal.app.domain.model.Encuesta
import com.nitanmal.app.domain.model.Sorteo
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalButton
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalTextField
import com.nitanmal.app.presentation.ui.icons.AppIcons2
import com.nitanmal.app.presentation.viewmodel.MiZonaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiZonaScreen(
    viewModel: MiZonaViewModel,
    esPremiumUsuario: Boolean,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val uiState by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        if (uiState.zona == null) viewModel.load()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading && uiState.zona == null -> {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                val zona = uiState.zona
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = strings.zonaTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Referidos
                    zona?.let {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = strings.zonaReferidos,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = strings.zonaReferidosDesc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = "${it.referidos}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Sorteos
                    val sorteos = zona?.sorteos?.filter { it.activo } ?: emptyList()
                    if (sorteos.isNotEmpty()) {
                        item { SeccionZona("🎁 ${strings.zonaSorteos}") }
                        items(sorteos, key = { "sorteo-${it.id}" }) { sorteo ->
                            SorteoCard(
                                sorteo = sorteo,
                                onParticipar = { viewModel.participarSorteo(sorteo.id) }
                            )
                        }
                    }

                    // Encuestas
                    val encuestas = zona?.encuestas?.filter { it.activa } ?: emptyList()
                    if (encuestas.isNotEmpty()) {
                        item { SeccionZona("📊 ${strings.zonaEncuestas}") }
                        items(encuestas, key = { "encuesta-${it.id}" }) { encuesta ->
                            EncuestaCard(
                                encuesta = encuesta,
                                onVotar = { opcionId -> viewModel.votarEncuesta(encuesta.id, opcionId) }
                            )
                        }
                    }

                    // Sugerencias (temas / invitados)
                    zona?.let {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SeccionZona(
                                    "💡 ${strings.zonaSugerencias}",
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { viewModel.setShowSugerirSheet(true) }) {
                                    Text(
                                        text = "+ ${strings.zonaSugerir}",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        if (it.sugerencias.isEmpty()) {
                            item {
                                Text(
                                    text = strings.zonaVacio,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        items(it.sugerencias, key = { s -> "sug-${s.id}" }) { sug ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = if (sug.tipo == "invitado") strings.zonaSugerirInvitado else strings.zonaSugerirTema,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = sug.texto,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    // Voto
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (sug.miVoto) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                                        },
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { viewModel.votarSugerencia(sug.id) }
                                    ) {
                                        Text(
                                            text = "▲ ${sug.votos}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (sug.miVoto) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Descargas
                    if (uiState.descargas.isNotEmpty()) {
                        item { SeccionZona("⬇️ ${strings.zonaDescargas}") }
                        items(uiState.descargas, key = { "dl-${it.id}" }) { descarga ->
                            val bloqueada = descarga.premium && !esPremiumUsuario
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = !bloqueada && !descarga.url.isNullOrBlank()) {
                                        descarga.url?.let { u -> runCatching { uriHandler.openUri(u) } }
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = descarga.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = listOfNotNull(
                                                descarga.type,
                                                descarga.size?.takeIf { s -> s.isNotBlank() }
                                            ).joinToString(" · "),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                                        )
                                    }
                                    Text(
                                        text = if (bloqueada) "🔒" else strings.zonaDescargar,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (bloqueada) MaterialTheme.colorScheme.secondary
                                        else MaterialTheme.colorScheme.primary
                                    )
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

    if (uiState.showSugerirSheet) {
        ModalBottomSheet(onDismissRequest = { viewModel.setShowSugerirSheet(false) }) {
            var tipo by remember { mutableStateOf("tema") }
            var texto by remember { mutableStateOf("") }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = strings.zonaSugerir,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = tipo == "tema",
                        onClick = { tipo = "tema" },
                        label = { Text(strings.zonaSugerirTema) }
                    )
                    FilterChip(
                        selected = tipo == "invitado",
                        onClick = { tipo = "invitado" },
                        label = { Text(strings.zonaSugerirInvitado) }
                    )
                }
                Spacer(Modifier.height(12.dp))
                NitanmalTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    placeholder = { Text(strings.fanBuzonPlaceholder) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    enabled = !uiState.isSugiriendo
                )
                Spacer(Modifier.height(20.dp))
                NitanmalButton(
                    text = strings.fanEnviar,
                    onClick = { viewModel.sugerir(tipo, texto.trim()) },
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = uiState.isSugiriendo,
                    enabled = !uiState.isSugiriendo && texto.isNotBlank()
                )
            }
        }
    }
}

@Composable
private fun SeccionZona(titulo: String, modifier: Modifier = Modifier) {
    Text(
        text = titulo,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.padding(top = 6.dp)
    )
}

@Composable
private fun SorteoCard(
    sorteo: Sorteo,
    onParticipar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = sorteo.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "🏆 ${sorteo.premio}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (sorteo.fecha.isNotBlank()) {
                Text(
                    text = sorteo.fecha,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${sorteo.participantes} ${strings.zonaParticipantes}" +
                        (sorteo.misChances?.takeIf { it > 0 }?.let { " · $it ${strings.zonaChances}" } ?: ""),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.weight(1f)
                )
                if (sorteo.participa) {
                    Text(
                        text = strings.zonaParticipando,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                } else {
                    Button(
                        onClick = onParticipar,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(strings.zonaParticipar, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EncuestaCard(
    encuesta: Encuesta,
    onVotar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = encuesta.pregunta,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(10.dp))
            encuesta.opciones.forEach { opcion ->
                val esMiVoto = encuesta.miVoto == opcion.id
                val fraccion = if (encuesta.total > 0) opcion.votos.toFloat() / encuesta.total else 0f
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        .clickable(enabled = encuesta.miVoto == null) { onVotar(opcion.id) }
                ) {
                    // Barra de resultado
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraccion.coerceIn(0f, 1f))
                            .height(40.dp)
                            .background(
                                if (esMiVoto) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = (if (esMiVoto) "✓ " else "") + opcion.texto,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (esMiVoto) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${opcion.votos}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            Text(
                text = "${encuesta.total} ${strings.zonaVotos}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

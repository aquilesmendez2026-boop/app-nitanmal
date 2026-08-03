package com.nitanmal.app.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.nitanmal.app.domain.model.Plantillas
import com.nitanmal.app.domain.model.plataformaLabel
import com.nitanmal.app.presentation.viewmodel.BuzonViewModel
import com.nitanmal.app.presentation.viewmodel.CanalesViewModel
import com.nitanmal.app.presentation.viewmodel.IdeasViewModel
import com.nitanmal.app.presentation.viewmodel.NotificacionesViewModel
import com.nitanmal.app.presentation.viewmodel.ProduccionViewModel
import com.nitanmal.app.presentation.viewmodel.ReunionesViewModel

/** Inicio: resumen del trabajo del equipo — ideas, buzón, producción, reuniones y canales. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: User,
    ideasViewModel: IdeasViewModel,
    buzonViewModel: BuzonViewModel,
    notificacionesViewModel: NotificacionesViewModel,
    produccionViewModel: ProduccionViewModel,
    reunionesViewModel: ReunionesViewModel,
    canalesViewModel: CanalesViewModel,
    onGoToIdeas: () -> Unit,
    onGoToBuzon: () -> Unit,
    onGoToProduccion: () -> Unit,
    onGoToReuniones: () -> Unit,
    onGoToSettings: () -> Unit,
    onOpenIdea: (String) -> Unit,
    onOpenEpisodio: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val ideasState by ideasViewModel.uiState.collectAsState()
    val buzonState by buzonViewModel.uiState.collectAsState()
    val notifState by notificacionesViewModel.uiState.collectAsState()
    val prodState by produccionViewModel.uiState.collectAsState()
    val reuState by reunionesViewModel.uiState.collectAsState()
    val canalesState by canalesViewModel.uiState.collectAsState()
    var showNotifSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (ideasState.notas.isEmpty()) ideasViewModel.load()
        if (buzonState.preguntas.isEmpty()) buzonViewModel.load()
        if (notifState.notificaciones.isEmpty()) notificacionesViewModel.load()
        if (prodState.episodios.isEmpty()) produccionViewModel.load()
        if (reuState.reuniones.isEmpty()) reunionesViewModel.load()
        if (canalesState.canales.isEmpty()) canalesViewModel.load()
    }

    val ideasActivas = ideasState.notas.count {
        NotaEstado.fromKey(it.estado) in listOf(NotaEstado.NUEVA, NotaEstado.REVISION, NotaEstado.APROBADA)
    }
    val episodiosEnCurso = prodState.episodios.count { it.aprobadas < Plantillas.STAGES.size }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Saludo + campana de notificaciones
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
                Column(modifier = Modifier.weight(1f)) {
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
                BadgedBox(
                    badge = {
                        if (notifState.noLeidas > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error
                            ) { Text("${notifState.noLeidas}") }
                        }
                    }
                ) {
                    IconButton(onClick = {
                        showNotifSheet = true
                        notificacionesViewModel.load()
                    }) {
                        Icon(
                            AppIcons2.Bell,
                            contentDescription = strings.notifTitle,
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }
                IconButton(onClick = onGoToSettings) {
                    Icon(
                        com.nitanmal.app.presentation.ui.icons.AppIcons.Settings,
                        contentDescription = strings.navSettings,
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Contadores (2×2)
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
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    valor = "$episodiosEnCurso",
                    etiqueta = strings.homeEpisodiosEnCurso,
                    icono = { Icon(AppIcons2.Movie, null, tint = androidx.compose.ui.graphics.Color(0xFF8b5cf6)) },
                    onClick = onGoToProduccion,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    valor = "${reuState.proximas.size}",
                    etiqueta = strings.homeReunionesProximas,
                    icono = { Icon(AppIcons2.Event, null, tint = MaterialTheme.colorScheme.tertiary) },
                    onClick = onGoToReuniones,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Nuestros canales (fila horizontal con seguidores y EN VIVO)
        if (canalesState.visibles.isNotEmpty()) {
            item {
                Text(
                    text = strings.canalesTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    canalesState.visibles.forEach { canal ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = plataformaLabel(canal.plataforma),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (canal.enVivo) {
                                        Spacer(Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.error
                                        ) {
                                            Text(
                                                text = strings.canalesEnVivo,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = androidx.compose.ui.graphics.Color.White,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                if (canal.seguidores.isNotBlank()) {
                                    Text(
                                        text = canal.seguidores,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // En producción
        item {
            SectionHeader(strings.homeEnProduccion, strings.homeVerTodas, onGoToProduccion)
        }
        item {
            val enCurso = prodState.ordenados.filter { it.aprobadas < Plantillas.STAGES.size }.take(2)
            if (enCurso.isEmpty()) {
                EmptyHint(strings.homeSinEpisodios)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    enCurso.forEach { episodio ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onOpenEpisodio(episodio.id) }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = episodio.titulo,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${episodio.aprobadas}/${Plantillas.STAGES.size}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Plantillas.STAGES.forEach { stage ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(5.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(estadoEtapaColor(episodio.etapa(stage).estado))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Próxima reunión
        item {
            SectionHeader(strings.homeProximaReunion, strings.homeVerTodas, onGoToReuniones)
        }
        item {
            val proxima = reuState.proximas.firstOrNull()
            if (proxima == null) {
                EmptyHint(strings.homeSinReuniones)
            } else {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onGoToReuniones)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            AppIcons2.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = proxima.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = listOfNotNull(
                                    proxima.date,
                                    proxima.time.takeIf { it.isNotBlank() },
                                    proxima.lugar?.takeIf { it.isNotBlank() }
                                ).joinToString(" · "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                            )
                        }
                    }
                }
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

    // Hoja de notificaciones
    if (showNotifSheet) {
        ModalBottomSheet(onDismissRequest = {
            showNotifSheet = false
            notificacionesViewModel.marcarLeidas()
        }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = strings.notifTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(12.dp))

                if (notifState.notificaciones.isEmpty()) {
                    Text(
                        text = strings.notifVacio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        notifState.notificaciones.take(20).forEach { notif ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (notif.leida) {
                                        MaterialTheme.colorScheme.surface
                                    } else {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = notif.texto,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (notif.leida) FontWeight.Normal else FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = formatFecha(notif.createdAt),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                    )
                                }
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

package com.nitanmal.app.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nitanmal.app.core.localization.rememberStrings
import com.nitanmal.app.core.util.todayIsoDate
import com.nitanmal.app.domain.model.EpisodioFan
import com.nitanmal.app.domain.model.Evento
import com.nitanmal.app.domain.model.Plantillas
import com.nitanmal.app.domain.model.User
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalButton
import com.nitanmal.app.presentation.ui.components.atoms.glass
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalTextField
import com.nitanmal.app.presentation.ui.components.molecules.CanalCard
import com.nitanmal.app.presentation.ui.icons.AppIcons2
import com.nitanmal.app.presentation.viewmodel.BuzonViewModel
import com.nitanmal.app.presentation.viewmodel.CanalesViewModel
import com.nitanmal.app.presentation.viewmodel.FanViewModel
import com.nitanmal.app.presentation.viewmodel.IdeasViewModel
import com.nitanmal.app.presentation.viewmodel.MiZonaViewModel
import com.nitanmal.app.presentation.viewmodel.PlanificadorViewModel
import com.nitanmal.app.presentation.viewmodel.ProduccionViewModel
import com.nitanmal.app.presentation.viewmodel.ReunionesViewModel

private fun youtubeUrl(videoId: String) = "https://www.youtube.com/watch?v=$videoId"

/** "1 sorteo activo" / "3 sorteos activos" */
private fun cuenta(n: Int, singular: String, plural: String) = if (n == 1) "1 $singular" else "$n $plural"

// ═══════════════ INICIO FAN ═══════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioFanScreen(
    user: User,
    fanViewModel: FanViewModel,
    canalesViewModel: CanalesViewModel,
    miZonaViewModel: MiZonaViewModel,
    onGoToEnVivo: () -> Unit,
    onOpenEpisodio: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val uiState by fanViewModel.uiState.collectAsState()
    val zonaState by miZonaViewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current
    val clipboard = LocalClipboardManager.current

    LaunchedEffect(Unit) {
        if (uiState.episodios.isEmpty()) fanViewModel.load()
        if (zonaState.zona == null) miZonaViewModel.load()
    }

    val zona = zonaState.zona
    val sorteosActivos = zona?.sorteos?.filter { it.activo } ?: emptyList()
    val encuestasActivas = zona?.encuestas?.filter { it.activa } ?: emptyList()
    val sugerencias = zona?.sugerencias ?: emptyList()
    val referidos = zona?.referidos ?: 0
    val nombre = user.apodo?.takeIf { it.isNotBlank() } ?: user.name.split(" ").firstOrNull() ?: ""
    val refLink = "https://nitanmal.cl/?ref=${user.id}"

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // ── Saludo ──
            item {
                Column {
                    Text(
                        text = "🔒 ZONA DE REGISTRADOS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("Hola, ")
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) { append(nombre) }
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Contenido que no verás en las plataformas públicas y descargables solo para la comunidad.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChipInfo(
                            texto = "Rol: " + when (user.role) {
                                "superadmin" -> "Superadmin"
                                "admin" -> "Admin"
                                "participante" -> "Participante"
                                else -> "Miembro"
                            }
                        )
                        if (user.esPremium) ChipInfo(texto = "👑 Premium")
                    }
                }
            }

            // Invitación a premium (como la web)
            if (!user.esPremium) {
                item {
                    Spacer(Modifier.height(20.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFf59e0b).copy(alpha = 0.10f))
                            .padding(18.dp)
                    ) {
                        Text(text = "👑", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Desbloquea todo con Premium",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Descargas premium, episodios exclusivos y sin anuncios.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ── Contenido solo para miembros ──
            item {
                SeccionZonaTitulo("Exclusivo", "Contenido solo para miembros")
            }
            items(EXCLUSIVO_MIEMBROS, key = { "ex-${it.titulo}" }) { item ->
                ExclusivoCard(item)
                Spacer(Modifier.height(12.dp))
            }

            // ── Descarga tu material ──
            item { SeccionZonaTitulo("Descargables", "Descarga tu material") }
            if (zonaState.descargas.isEmpty()) {
                item {
                    Text(
                        text = "Aún no hay archivos para descargar. ¡Pronto!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
            items(zonaState.descargas, key = { "dl-${it.id}" }) { descarga ->
                DescargaZonaCard(
                    descarga = descarga,
                    esPremiumUsuario = user.esPremium,
                    onAbrir = { url -> runCatching { uriHandler.openUri(url) } }
                )
                Spacer(Modifier.height(12.dp))
            }

            // ── Trae a tus amigos ──
            item { SeccionZonaTitulo("Invita y gana", "Trae a tus amigos") }
            item {
                ReferidosZona(
                    referidos = referidos,
                    refLink = refLink,
                    haySorteo = sorteosActivos.isNotEmpty(),
                    onCopiar = {
                        clipboard.setText(AnnotatedString(refLink))
                        miZonaViewModel.mostrarInfo("Link copiado ✅")
                    }
                )
                Spacer(Modifier.height(12.dp))
            }

            // ── Sorteo activo ──
            item {
                SeccionZonaTitulo(
                    "Sorteos",
                    if (sorteosActivos.size > 1) "Sorteos activos" else "Sorteo activo"
                )
            }
            if (sorteosActivos.isEmpty()) {
                item {
                    Text(
                        text = "No hay sorteos activos ahora mismo. Vuelve pronto 🎁",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
            items(sorteosActivos, key = { "s-${it.id}" }) { sorteo ->
                SorteoZonaCard(
                    sorteo = sorteo,
                    onParticipar = { miZonaViewModel.participarSorteo(sorteo.id) }
                )
                Spacer(Modifier.height(12.dp))
            }

            // ── Tu opinión cuenta ──
            item { SeccionZonaTitulo("Encuestas", "Tu opinión cuenta") }
            if (encuestasActivas.isEmpty()) {
                item {
                    Text(
                        text = "No hay encuestas abiertas. Pronto habrá más 🗳️",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
            items(encuestasActivas, key = { "e-${it.id}" }) { encuesta ->
                EncuestaZonaCard(
                    encuesta = encuesta,
                    onVotar = { opcionId -> miZonaViewModel.votarEncuesta(encuesta.id, opcionId) }
                )
                Spacer(Modifier.height(12.dp))
            }

            // ── Vota temas y sugiere invitados ──
            item { SeccionZonaTitulo("Comunidad", "Vota temas y sugiere invitados") }
            item {
                NitanmalButton(
                    text = "+ ${strings.zonaSugerir}",
                    onClick = { miZonaViewModel.setShowSugerirSheet(true) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
            }
            if (sugerencias.isEmpty()) {
                item {
                    Text(
                        text = "Sé el primero en sugerir un tema o invitado 💡",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }
            }
            items(sugerencias, key = { "sg-${it.id}" }) { sug ->
                SugerenciaZonaCard(
                    sugerencia = sug,
                    onVotar = { miZonaViewModel.votarSugerencia(sug.id) }
                )
                Spacer(Modifier.height(10.dp))
            }

            item { Spacer(Modifier.height(24.dp)) }
        }

        zonaState.error?.let { error ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                action = { TextButton(onClick = { miZonaViewModel.clearError() }) { Text("OK") } }
            ) { Text(error) }
        }
        zonaState.info?.let { info ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = Color.White,
                action = {
                    TextButton(onClick = { miZonaViewModel.clearInfo() }) { Text("OK", color = Color.White) }
                }
            ) { Text(info) }
        }
    }

    // Hoja para sugerir tema / invitado
    if (zonaState.showSugerirSheet) {
        ModalBottomSheet(onDismissRequest = { miZonaViewModel.setShowSugerirSheet(false) }) {
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
                    enabled = !zonaState.isSugiriendo
                )
                Spacer(Modifier.height(20.dp))
                NitanmalButton(
                    text = strings.fanEnviar,
                    onClick = { miZonaViewModel.sugerir(tipo, texto.trim()) },
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = zonaState.isSugiriendo,
                    enabled = !zonaState.isSugiriendo && texto.isNotBlank()
                )
            }
        }
    }
}

/** Contenido exclusivo de la comunidad (mismo listado que la web). */
private data class ExclusivoItem(
    val titulo: String,
    val etiqueta: String,
    val duracion: String,
    val descripcion: String
)

private val EXCLUSIVO_MIEMBROS = listOf(
    ExclusivoItem(
        titulo = "Episodio 12 — versión sin censura",
        etiqueta = "Extendido",
        duracion = "1h 52m",
        descripcion = "La versión completa que no subimos a plataformas públicas: 40 minutos extra de puro caos."
    ),
    ExclusivoItem(
        titulo = "Detrás de cámaras: la noche de la apuesta",
        etiqueta = "Detrás de cámaras",
        duracion = "27m",
        descripcion = "Lo que pasó antes y después de grabar. Cámaras encendidas cuando nadie creía que grababan."
    ),
    ExclusivoItem(
        titulo = "Blooper reel — temporada 1",
        etiqueta = "Solo miembros",
        duracion = "18m",
        descripcion = "Todas las veces que perdimos el hilo, el trago o la dignidad. Compilado exclusivo."
    )
)

@Composable
private fun ExclusivoCard(item: ExclusivoItem) {
    Column(modifier = Modifier.fillMaxWidth().glass(18.dp).padding(18.dp)) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = Color(0xFFf59e0b).copy(alpha = 0.12f)
        ) {
            Text(
                text = "🥃 ${item.etiqueta.uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFfbbf24),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = item.titulo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = item.descripcion,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "▶  Reproducir · ${item.duracion}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/** Título de sección alineado a la izquierda con subrayado degradado (como la web). */
@Composable
private fun SeccionZonaTitulo(eyebrow: String, titulo: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 32.dp, bottom = 16.dp)) {
        Text(
            text = eyebrow.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                    )
                )
        )
    }
}

@Composable
private fun VacioZona(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
    )
}

@Composable
private fun ReferidosZona(
    referidos: Int,
    refLink: String,
    haySorteo: Boolean,
    onCopiar: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().glass(20.dp).padding(20.dp)) {
        Text(
            text = "Comparte tu link de invitación",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (referidos == 0) {
                if (haySorteo) "Cada amigo que se registre te suma +1 chance en el sorteo."
                else "Cada amigo que se registre suma para los próximos beneficios."
            } else {
                "Has invitado a $referidos " + (if (referidos == 1) "amigo" else "amigos") + ". " +
                    (if (haySorteo) "Cada uno te suma +1 chance en el sorteo."
                    else "Cada uno suma para los próximos beneficios.")
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(14.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f)
        ) {
            Text(
                text = refLink,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }
        Spacer(Modifier.height(14.dp))
        NitanmalButton(
            text = "Copiar link",
            onClick = onCopiar,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SorteoZonaCard(
    sorteo: com.nitanmal.app.domain.model.Sorteo,
    onParticipar: () -> Unit
) {
    val strings = rememberStrings()
    Column(modifier = Modifier.fillMaxWidth().glass(18.dp).padding(18.dp)) {
        Text(
            text = sorteo.titulo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (sorteo.premio.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = sorteo.premio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (sorteo.comoParticipar.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = sorteo.comoParticipar,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = cuenta(sorteo.participantes, "participante", "participantes"),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                modifier = Modifier.weight(1f)
            )
            if (sorteo.participa) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "✅ Participando",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            } else {
                Button(onClick = onParticipar, shape = RoundedCornerShape(999.dp)) {
                    Text(strings.zonaParticipar, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EncuestaZonaCard(
    encuesta: com.nitanmal.app.domain.model.Encuesta,
    onVotar: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().glass(18.dp).padding(18.dp)) {
        Text(
            text = encuesta.pregunta,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(12.dp))
        encuesta.opciones.forEach { opcion ->
            val votada = encuesta.miVoto == opcion.id
            val pct = if (encuesta.total > 0) opcion.votos * 100 / encuesta.total else 0
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = encuesta.miVoto == null) { onVotar(opcion.id) }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = (if (votada) "✅ " else "") + opcion.texto,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (votada) FontWeight.Bold else FontWeight.Normal,
                        color = if (votada) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (encuesta.miVoto != null) {
                        Text(
                            text = "$pct%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (encuesta.miVoto != null) {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(pct / 100f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (votada) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                )
                        )
                    }
                }
            }
        }
        Text(
            text = cuenta(encuesta.total, "voto", "votos"),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun SugerenciaZonaCard(
    sugerencia: com.nitanmal.app.domain.model.Sugerencia,
    onVotar: () -> Unit
) {
    val strings = rememberStrings()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().glass(14.dp).padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
        ) {
            Text(
                text = if (sugerencia.tipo == "invitado") strings.zonaSugerirInvitado
                else strings.zonaSugerirTema,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = sugerencia.texto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (sugerencia.miVoto) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
            modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable(onClick = onVotar)
        ) {
            Text(
                text = "▲ " + sugerencia.votos,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (sugerencia.miVoto) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun DescargaZonaCard(
    descarga: com.nitanmal.app.domain.model.Descarga,
    esPremiumUsuario: Boolean,
    onAbrir: (String) -> Unit
) {
    val strings = rememberStrings()
    val bloqueada = descarga.premium && !esPremiumUsuario
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .glass(14.dp)
            .clickable(enabled = !bloqueada && !descarga.url.isNullOrBlank()) {
                descarga.url?.let(onAbrir)
            }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = (if (descarga.premium) "🥃 " else "") + descarga.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = listOfNotNull(
                    descarga.type,
                    descarga.size?.takeIf { it.isNotBlank() }
                ).joinToString(" · "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
        Text(
            text = if (bloqueada) "🔒 Premium" else strings.zonaDescargar,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (bloqueada) MaterialTheme.colorScheme.secondary
            else MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ChipInfo(texto: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
        )
    }
}

/** Card de resumen de una sección accesible; toca para abrirla. */
@Composable
private fun ResumenCard(
    emoji: String,
    titulo: String,
    resumen: String,
    accent: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glass(18.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accent.copy(alpha = 0.12f)
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = resumen,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "→",
                style = MaterialTheme.typography.titleMedium,
                color = accent
            )
        }
    }
}

// ═══════════════ EN VIVO ═══════════════

@Composable
fun EnVivoScreen(
    fanViewModel: FanViewModel,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val uiState by fanViewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        if (uiState.episodios.isEmpty() && uiState.live == null) fanViewModel.load()
        else fanViewModel.refreshLive()
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item {
            val live = uiState.live
            if (live?.isLive == true) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(listOf(Color(0xFFdc2626), Color(0xFF450a0a)))
                        )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 20.dp)
                    ) {
                        Icon(
                            AppIcons2.Live,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = strings.fanEnVivoAhora,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        if (live.title.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = live.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = {
                                if (live.videoId.isNotBlank()) {
                                    runCatching { uriHandler.openUri(youtubeUrl(live.videoId)) }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFFdc2626)
                            ),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                text = "▶ ${strings.fanVerEnYouTube}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                val proximo = uiState.proximoEvento
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 20.dp)
                    ) {
                        Icon(
                            AppIcons2.Live,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = strings.fanProximoShow,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(6.dp))
                        if (proximo != null) {
                            Text(
                                text = proximo.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${proximo.date} · ${proximo.time} hrs",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = strings.fanSinShows,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Horarios
        if (uiState.proximosEventos.isNotEmpty()) {
            item {
                Text(
                    text = strings.fanHorarios,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            items(uiState.proximosEventos, key = { it.id }) { evento ->
                EventoCard(evento)
            }
        }
    }
}

@Composable
internal fun EventoCard(evento: Evento, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().glass(14.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = evento.date.takeLast(2),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = evento.time,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = evento.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!evento.description.isNullOrBlank()) {
                    Text(
                        text = evento.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ═══════════════ EPISODIOS ═══════════════

@Composable
fun EpisodiosFanScreen(
    fanViewModel: FanViewModel,
    esPremiumUsuario: Boolean,
    onOpenEpisodio: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val uiState by fanViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState.episodios.isEmpty()) fanViewModel.load()
    }

    if (uiState.isLoading && uiState.episodios.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = strings.fanNavEpisodios,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        items(uiState.episodios, key = { it.id }) { episodio ->
            EpisodioCardFan(
                episodio = episodio,
                esPremiumUsuario = esPremiumUsuario,
                onClick = { onOpenEpisodio(episodio.id) }
            )
        }
    }
}

@Composable
fun EpisodioCardFan(
    episodio: EpisodioFan,
    esPremiumUsuario: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val bloqueado = episodio.premium && !esPremiumUsuario

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glass(16.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "#${episodio.number}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = episodio.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = listOfNotNull(
                        episodio.duration.takeIf { it.isNotBlank() },
                        episodio.date?.takeIf { it.isNotBlank() }
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
            if (episodio.premium) {
                Spacer(Modifier.width(6.dp))
                Text(text = if (bloqueado) "🔒" else "🥃", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun EpisodioFanDetailScreen(
    episodioId: String,
    fanViewModel: FanViewModel,
    esPremiumUsuario: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val uiState by fanViewModel.uiState.collectAsState()
    val episodio = uiState.episodios.firstOrNull { it.id == episodioId }
    val uriHandler = LocalUriHandler.current

    if (episodio == null) {
        LaunchedEffect(Unit) { onNavigateBack() }
        return
    }
    val bloqueado = episodio.premium && !esPremiumUsuario

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        AppIcons2.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "#${episodio.number}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        item {
            Text(
                text = episodio.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        item {
            Text(
                text = listOfNotNull(
                    episodio.duration.takeIf { it.isNotBlank() },
                    episodio.date?.takeIf { it.isNotBlank() }
                ).joinToString(" · "),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }

        if (bloqueado) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = strings.fanPremiumLock,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            if (episodio.description.isNotBlank()) {
                item {
                    Text(
                        text = episodio.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Enlaces de escucha
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    episodio.links?.youtube?.takeIf { it.isNotBlank() }?.let { url ->
                        Button(
                            onClick = { runCatching { uriHandler.openUri(url) } },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFff0033)),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("▶ YouTube", color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                    episodio.links?.spotify?.takeIf { it.isNotBlank() }?.let { url ->
                        Button(
                            onClick = { runCatching { uriHandler.openUri(url) } },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1db954)),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("♪ Spotify", color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                }
            }

            episodio.showNotes?.takeIf { it.isNotBlank() }?.let { notes ->
                item {
                    Text(
                        text = strings.fanShowNotes,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

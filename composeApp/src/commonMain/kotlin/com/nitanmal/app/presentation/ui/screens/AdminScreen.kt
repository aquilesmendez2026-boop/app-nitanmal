package com.nitanmal.app.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.nitanmal.app.core.util.todayIsoDate
import com.nitanmal.app.domain.model.Canal
import com.nitanmal.app.domain.model.EpisodioFan
import com.nitanmal.app.domain.model.LiveState
import com.nitanmal.app.domain.model.UsuarioAdmin
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalButton
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalTextField
import com.nitanmal.app.presentation.ui.components.atoms.glass
import com.nitanmal.app.presentation.ui.icons.AppIcons2
import com.nitanmal.app.presentation.viewmodel.AdminUiState
import com.nitanmal.app.presentation.viewmodel.AdminViewModel

private data class SeccionAdmin(
    val emoji: String,
    val titulo: String,
    val resumen: String,
    val badge: Int = 0
)

/** Tipos de show que acepta el backend. */
private val TIPOS_SHOW = listOf("stream" to "Stream de juegos", "charla" to "Charla con trago", "especial" to "Especial")

/** Saca el ID de video de una URL de YouTube (o lo devuelve tal cual si ya es un ID). */
internal fun youtubeVideoId(entrada: String): String {
    val v = entrada.trim()
    if (v.isEmpty()) return ""
    Regex("""(?:v=|youtu\.be/|/live/|/embed/|/shorts/)([A-Za-z0-9_-]{6,})""")
        .find(v)?.groupValues?.get(1)?.let { return it }
    return if (Regex("""^[A-Za-z0-9_-]{6,}$""").matches(v)) v else ""
}

/**
 * Panel de administración: plana con grilla de cards (2 por fila, como la
 * Agenda); cada card abre su hoja de configuración con flecha para volver.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    /** Canales y usuarios solo los edita el super admin (lo exige el backend). */
    isSuperAdmin: Boolean = false,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val uiState by viewModel.uiState.collectAsState()
    var seccion by remember { mutableStateOf<Int?>(null) }

    // Hojas de creación / edición
    var showSheetShow by remember { mutableStateOf(false) }
    var episodioEnEdicion by remember { mutableStateOf<EpisodioFan?>(null) }
    var showSheetEpisodio by remember { mutableStateOf(false) }
    var showSheetSorteo by remember { mutableStateOf(false) }
    var sorteoEnEdicion by remember { mutableStateOf<com.nitanmal.app.domain.model.Sorteo?>(null) }
    var showSheetEncuesta by remember { mutableStateOf(false) }
    var encuestaEnEdicion by remember { mutableStateOf<com.nitanmal.app.domain.model.Encuesta?>(null) }
    var canalEnEdicion by remember { mutableStateOf<Canal?>(null) }
    var usuarioEnEdicion by remember { mutableStateOf<UsuarioAdmin?>(null) }

    LaunchedEffect(Unit) { viewModel.load() }

    val hoy = todayIsoDate()
    val showsProximos = uiState.eventos.filter { it.date >= hoy }.sortedBy { it.date + it.time }
    val showsArchivados = uiState.eventos.filter { it.date < hoy }.sortedByDescending { it.date + it.time }

    val secciones = buildList {
        add(SeccionAdmin("📊", "Resumen", "Todo lo que hay que vigilar"))
        add(
            SeccionAdmin(
                "🔴", strings.adminEnVivo,
                if (uiState.live?.isLive == true) "Transmitiendo ahora" else "Apagado · configurar",
                badge = if (uiState.live?.isLive == true) 1 else 0
            )
        )
        add(
            SeccionAdmin(
                "🎤", "Shows",
                if (showsProximos.isEmpty()) "Sin shows programados"
                else cuentaAdmin(showsProximos.size, "próximo", "próximos")
            )
        )
        add(SeccionAdmin("🎧", "Episodios", cuentaAdmin(uiState.episodios.size, "episodio", "episodios")))
        add(SeccionAdmin("⬇️", "Descargas", cuentaAdmin(uiState.descargas.size, "archivo", "archivos")))
        add(
            SeccionAdmin(
                "🎁", "Sorteo",
                if (uiState.sorteos.isEmpty()) "Crear el primero"
                else "${cuentaAdmin(uiState.sorteosActivos, "activo", "activos")} · " +
                    cuentaAdmin(uiState.participantes, "participante", "participantes")
            )
        )
        add(
            SeccionAdmin(
                "🗳️", "Encuestas",
                if (uiState.encuestas.isEmpty()) "Sin encuestas"
                else "${cuentaAdmin(uiState.encuestasActivas, "activa", "activas")} · " +
                    cuentaAdmin(uiState.votos, "voto", "votos")
            )
        )
        add(SeccionAdmin("📣", "Canales", cuentaAdmin(uiState.canales.size, "canal", "canales")))
        add(
            SeccionAdmin(
                "👥", "Usuarios",
                if (uiState.usuarios.isEmpty()) cuentaAdmin(uiState.equipo.size, "en el equipo", "en el equipo")
                else "${cuentaAdmin(uiState.usuarios.size, "registrado", "registrados")} · " +
                    "${uiState.enEquipo} en el equipo"
            )
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        val actual = seccion
        if (actual == null) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
            ) {
                Text(
                    text = strings.adminTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Gestión del podcast.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(18.dp))
                secciones.chunked(2).forEachIndexed { fila, par ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        par.forEachIndexed { col, s ->
                            SeccionAdminCard(
                                seccion = s,
                                onClick = { seccion = fila * 2 + col },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (par.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    IconButton(onClick = { seccion = null }) {
                        Icon(
                            AppIcons2.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "${secciones[actual].emoji}  ${secciones[actual].titulo}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    // Acción de crear según la sección
                    when (actual) {
                        2 -> PillNuevo("Nuevo show") { showSheetShow = true }
                        3 -> PillNuevo("Nuevo episodio") {
                            episodioEnEdicion = null
                            showSheetEpisodio = true
                        }
                        5 -> PillNuevo("Nuevo sorteo") { showSheetSorteo = true }
                        6 -> PillNuevo("Nueva encuesta") { showSheetEncuesta = true }
                        else -> {}
                    }
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    when (actual) {
                        0 -> ResumenAdmin(uiState)
                        1 -> EnVivoAdmin(uiState, viewModel)

                        2 -> {
                            if (showsProximos.isEmpty() && showsArchivados.isEmpty()) {
                                VacioAdmin("Aún no hay shows. Crea el primero con “+ Nuevo”.")
                            }
                            if (showsProximos.isNotEmpty()) {
                                TituloListaAdmin("Próximos")
                                showsProximos.forEach { ev ->
                                    ShowAdminFila(
                                        titulo = ev.title,
                                        detalle = "${ev.date} · ${ev.time} · ${tipoLabel(ev.type)}",
                                        onBorrar = { viewModel.borrarShow(ev.id) }
                                    )
                                }
                            }
                            if (showsArchivados.isNotEmpty()) {
                                var verArchivo by remember { mutableStateOf(false) }
                                TituloListaAdmin(
                                    texto = "Archivados (${showsArchivados.size})",
                                    accion = if (verArchivo) "Ocultar" else "Ver",
                                    onAccion = { verArchivo = !verArchivo }
                                )
                                if (verArchivo) showsArchivados.forEach { ev ->
                                    ShowAdminFila(
                                        titulo = ev.title,
                                        detalle = "${ev.date} · ${ev.time} · ${tipoLabel(ev.type)}",
                                        atenuado = true,
                                        onBorrar = { viewModel.borrarShow(ev.id) }
                                    )
                                }
                            }
                        }

                        3 -> {
                            val recientes = uiState.episodios.sortedByDescending { it.number }.take(5)
                            val archivo = uiState.episodios.sortedByDescending { it.number }.drop(5)
                            if (uiState.episodios.isEmpty()) {
                                VacioAdmin("Aún no hay episodios. Crea el primero con “+ Nuevo”.")
                            }
                            if (recientes.isNotEmpty()) TituloListaAdmin("Recientes")
                            recientes.forEach { ep ->
                                EpisodioAdminFila(
                                    episodio = ep,
                                    onEditar = { episodioEnEdicion = ep; showSheetEpisodio = true },
                                    onBorrar = { viewModel.borrarEpisodio(ep.id) }
                                )
                            }
                            if (archivo.isNotEmpty()) {
                                var verArchivo by remember { mutableStateOf(false) }
                                TituloListaAdmin(
                                    texto = "Archivo (${archivo.size})",
                                    accion = if (verArchivo) "Ocultar" else "Ver",
                                    onAccion = { verArchivo = !verArchivo }
                                )
                                if (verArchivo) archivo.forEach { ep ->
                                    EpisodioAdminFila(
                                        episodio = ep,
                                        atenuado = true,
                                        onEditar = { episodioEnEdicion = ep; showSheetEpisodio = true },
                                        onBorrar = { viewModel.borrarEpisodio(ep.id) }
                                    )
                                }
                            }
                        }

                        4 -> {
                            uiState.descargas.forEach { d ->
                                FilaAdmin(
                                    titulo = d.title,
                                    detalle = listOfNotNull(
                                        d.type.takeIf { it.isNotBlank() },
                                        d.size?.takeIf { it.isNotBlank() },
                                        if (d.premium) "🔒 premium" else null
                                    ).joinToString(" · ")
                                )
                            }
                            if (uiState.descargas.isEmpty()) {
                                VacioAdmin("Sin descargas publicadas. Se suben desde la web (requiere archivo).")
                            }
                        }

                        5 -> SorteosAdmin(uiState, viewModel, onEditar = { sorteoEnEdicion = it })
                        6 -> EncuestasAdmin(uiState, viewModel, onEditar = { encuestaEnEdicion = it })

                        7 -> {
                            if (!isSuperAdmin) {
                                AvisoAdmin("Solo el super admin puede editar los canales.")
                            }
                            uiState.canales.forEach { c ->
                                CanalAdminFila(
                                    canal = c,
                                    editable = isSuperAdmin,
                                    onEditar = { canalEnEdicion = c }
                                )
                            }
                            if (uiState.canales.isEmpty()) VacioAdmin("Sin canales configurados")
                        }

                        else -> {
                            if (!isSuperAdmin) {
                                AvisoAdmin("Solo el super admin puede cambiar roles y ver todos los registrados.")
                            }
                            if (uiState.usuarios.isEmpty()) {
                                uiState.equipo.forEach { m ->
                                    FilaAdmin(titulo = m.nombre, detalle = "miembro del equipo")
                                }
                                if (uiState.equipo.isEmpty()) VacioAdmin("Sin miembros en el equipo")
                            } else uiState.usuarios.forEach { u ->
                                UsuarioAdminFila(
                                    usuario = u,
                                    editable = isSuperAdmin,
                                    onEditar = { usuarioEnEdicion = u }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
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
                    TextButton(onClick = { viewModel.clearInfo() }) { Text("OK", color = Color.White) }
                }
            ) { Text(info) }
        }
    }

    // ── Hojas de formulario ──
    if (showSheetShow) {
        ShowFormSheet(
            isGuardando = uiState.isGuardando,
            onDismiss = { showSheetShow = false },
            onGuardar = { date, time, title, tipo, desc ->
                viewModel.crearShow(date, time, title, tipo, desc)
                showSheetShow = false
            }
        )
    }
    if (showSheetEpisodio) {
        EpisodioFormSheet(
            episodio = episodioEnEdicion,
            siguienteNumero = (uiState.episodios.maxOfOrNull { it.number } ?: 0) + 1,
            isGuardando = uiState.isGuardando,
            onDismiss = { showSheetEpisodio = false; episodioEnEdicion = null },
            onGuardar = { num, title, desc, dur, fecha, premium, yt, sp ->
                viewModel.guardarEpisodio(
                    id = episodioEnEdicion?.id, number = num, title = title, description = desc,
                    duration = dur, date = fecha, premium = premium, youtube = yt, spotify = sp
                )
                showSheetEpisodio = false
                episodioEnEdicion = null
            }
        )
    }
    if (showSheetEncuesta) {
        EncuestaFormSheet(
            isGuardando = uiState.isGuardando,
            onDismiss = { showSheetEncuesta = false },
            onGuardar = { pregunta, tipo, opciones ->
                viewModel.crearEncuesta(pregunta, tipo, opciones)
                showSheetEncuesta = false
            }
        )
    }
    if (showSheetSorteo) {
        SorteoFormSheet(
            isGuardando = uiState.isGuardando,
            onDismiss = { showSheetSorteo = false },
            onGuardar = { titulo, premio, como, fecha ->
                viewModel.crearSorteo(titulo, premio, como, fecha)
                showSheetSorteo = false
            }
        )
    }
    sorteoEnEdicion?.let { sorteo ->
        SorteoFormSheet(
            sorteo = sorteo,
            isGuardando = uiState.isGuardando,
            onDismiss = { sorteoEnEdicion = null },
            onGuardar = { titulo, premio, como, fecha ->
                viewModel.editarSorteo(sorteo.id, titulo, premio, como, fecha)
                sorteoEnEdicion = null
            }
        )
    }
    encuestaEnEdicion?.let { encuesta ->
        EncuestaEditSheet(
            encuesta = encuesta,
            isGuardando = uiState.isGuardando,
            onDismiss = { encuestaEnEdicion = null },
            onGuardar = { pregunta ->
                viewModel.editarEncuesta(encuesta.id, pregunta)
                encuestaEnEdicion = null
            }
        )
    }
    canalEnEdicion?.let { canal ->
        CanalFormSheet(
            canal = canal,
            isGuardando = uiState.isGuardando,
            onDismiss = { canalEnEdicion = null },
            onGuardar = { actualizado ->
                viewModel.guardarCanal(actualizado)
                canalEnEdicion = null
            }
        )
    }
    usuarioEnEdicion?.let { usuario ->
        RolFormSheet(
            usuario = usuario,
            isGuardando = uiState.isGuardando,
            onDismiss = { usuarioEnEdicion = null },
            onGuardar = { rol ->
                viewModel.cambiarRol(usuario.userId, rol)
                usuarioEnEdicion = null
            }
        )
    }
}

private fun cuentaAdmin(n: Int, singular: String, plural: String) =
    if (n == 1) "1 $singular" else "$n $plural"

private fun tipoLabel(tipo: String) = TIPOS_SHOW.firstOrNull { it.first == tipo }?.second ?: tipo

// ─────────── Secciones ───────────

@Composable
private fun ResumenAdmin(uiState: AdminUiState) {
    val tiles = listOf(
        if (uiState.usuarios.isNotEmpty())
            Triple("👥", "REGISTRADOS", "${uiState.usuarios.size}" to "${uiState.enEquipo} en el equipo")
        else
            Triple("👥", "EQUIPO", "${uiState.equipo.size}" to "miembros del staff"),
        Triple(
            "📡", "EN VIVO",
            (if (uiState.live?.isLive == true) "Sí" else "No") to (uiState.live?.title ?: "")
        ),
        Triple(
            "🎁", "SORTEOS",
            "${uiState.sorteosActivos}" to cuentaAdmin(uiState.participantes, "participante", "participantes")
        ),
        Triple(
            "🗳️", "ENCUESTAS",
            "${uiState.encuestasActivas}" to cuentaAdmin(uiState.votos, "voto", "votos")
        ),
        Triple("💬", "BUZÓN", "${uiState.preguntasPendientes}" to "sin responder"),
        Triple("💡", "SUGERENCIAS", "${uiState.sugerencias.size}" to "temas / invitados"),
        Triple("🎧", "EPISODIOS", "${uiState.episodios.size}" to "publicados"),
        Triple("⬇️", "DESCARGAS", "${uiState.descargas.size}" to "archivos")
    )
    tiles.chunked(2).forEach { par ->
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            par.forEach { (emoji, label, valores) ->
                Column(modifier = Modifier.weight(1f).glass(16.dp).padding(14.dp)) {
                    Text(
                        text = "$emoji $label",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = valores.first,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = valores.second,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (par.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun EnVivoAdmin(uiState: AdminUiState, viewModel: AdminViewModel) {
    val strings = rememberStrings()
    var isLive by remember(uiState.live) { mutableStateOf(uiState.live?.isLive ?: false) }
    var videoId by remember(uiState.live) { mutableStateOf(uiState.live?.videoId ?: "") }
    var titulo by remember(uiState.live) { mutableStateOf(uiState.live?.title ?: "") }
    val idLimpio = youtubeVideoId(videoId)

    Column(modifier = Modifier.fillMaxWidth().glass(20.dp).padding(16.dp)) {
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
                colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFFdc2626))
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

        // Vista previa: confirma que el video es el correcto antes de guardar
        Spacer(Modifier.height(12.dp))
        if (idLimpio.isNotBlank()) {
            Text(
                text = "Vista previa",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(6.dp))
            AsyncImage(
                model = "https://img.youtube.com/vi/$idLimpio/hqdefault.jpg",
                contentDescription = "Miniatura del video",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .glass(12.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "ID: $idLimpio",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        } else if (videoId.isNotBlank()) {
            Text(
                text = "⚠️ No reconozco un ID de YouTube en eso. Pega la URL del video o el ID.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(12.dp))
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
                    LiveState(isLive = isLive, videoId = idLimpio, title = titulo.trim())
                )
            },
            modifier = Modifier.fillMaxWidth(),
            isLoading = uiState.isSavingLive,
            enabled = !isLive || idLimpio.isNotBlank()
        )
    }
}

@Composable
private fun SorteosAdmin(
    uiState: AdminUiState,
    viewModel: AdminViewModel,
    onEditar: (com.nitanmal.app.domain.model.Sorteo) -> Unit
) {
    val strings = rememberStrings()
    if (uiState.sorteos.isEmpty()) {
        VacioAdmin("Aún no hay sorteos. Crea el primero con “+ Nuevo”.")
        return
    }
    uiState.sorteos.forEach { sorteo ->
        Column(modifier = Modifier.fillMaxWidth().glass(16.dp).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = sorteo.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (sorteo.activo) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                ) {
                    Text(
                        text = if (sorteo.activo) "Activo" else "Cerrado",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (sorteo.activo) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
            var confirmarBorrado by remember { mutableStateOf(false) }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.toggleSorteo(sorteo.id) },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (sorteo.activo) strings.adminCerrar else strings.adminAbrir)
                }
                OutlinedButton(
                    onClick = { onEditar(sorteo) },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Editar")
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
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { confirmarBorrado = true }) {
                    Icon(
                        AppIcons2.Delete,
                        contentDescription = "Eliminar sorteo",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            if (confirmarBorrado) {
                AlertDialog(
                    onDismissRequest = { confirmarBorrado = false },
                    title = { Text("Eliminar sorteo") },
                    text = { Text("${sorteo.titulo}\n\nSe perderán sus ${sorteo.participantes} participantes.") },
                    confirmButton = {
                        TextButton(onClick = { confirmarBorrado = false; viewModel.borrarSorteo(sorteo.id) }) {
                            Text("Eliminar", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmarBorrado = false }) { Text("Cancelar") }
                    }
                )
            }
        }
    }
}

@Composable
private fun EncuestasAdmin(
    uiState: AdminUiState,
    viewModel: AdminViewModel,
    onEditar: (com.nitanmal.app.domain.model.Encuesta) -> Unit
) {
    if (uiState.encuestas.isEmpty()) {
        VacioAdmin("Aún no hay encuestas. Crea la primera con “+ Nueva encuesta”.")
        return
    }
    uiState.encuestas.forEach { encuesta ->
        Column(modifier = Modifier.fillMaxWidth().glass(16.dp).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = encuesta.pregunta,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (encuesta.activa) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                ) {
                    Text(
                        text = if (encuesta.activa) "Activa" else "Cerrada",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (encuesta.activa) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            encuesta.opciones.forEach { opcion ->
                val pct = if (encuesta.total > 0) opcion.votos * 100 / encuesta.total else 0
                Text(
                    text = "${opcion.texto} — ${opcion.votos} ($pct%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            Spacer(Modifier.height(4.dp))
            var confirmarBorrado by remember { mutableStateOf(false) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = cuentaAdmin(encuesta.total, "voto en total", "votos en total"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = { viewModel.toggleEncuesta(encuesta.id) },
                    shape = RoundedCornerShape(999.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(if (encuesta.activa) "Cerrar" else "Reabrir")
                }
                OutlinedButton(
                    onClick = { onEditar(encuesta) },
                    shape = RoundedCornerShape(999.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Editar")
                }
                IconButton(onClick = { confirmarBorrado = true }) {
                    Icon(
                        AppIcons2.Delete,
                        contentDescription = "Eliminar encuesta",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            if (confirmarBorrado) {
                AlertDialog(
                    onDismissRequest = { confirmarBorrado = false },
                    title = { Text("Eliminar encuesta") },
                    text = { Text("${encuesta.pregunta}\n\nSe perderán sus ${encuesta.total} votos.") },
                    confirmButton = {
                        TextButton(onClick = { confirmarBorrado = false; viewModel.borrarEncuesta(encuesta.id) }) {
                            Text("Eliminar", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmarBorrado = false }) { Text("Cancelar") }
                    }
                )
            }
        }
    }
}

/** Botón de crear, tipo pill, en el encabezado de cada sección. */
@Composable
private fun PillNuevo(texto: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        ),
        modifier = Modifier.padding(end = 8.dp).clip(RoundedCornerShape(999.dp)).clickable(onClick = onClick)
    ) {
        Text(
            text = "+  $texto",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

// ─────────── Filas ───────────

@Composable
private fun TituloListaAdmin(texto: String, accion: String? = null, onAccion: (() -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        if (accion != null && onAccion != null) {
            TextButton(onClick = onAccion) { Text(accion) }
        }
    }
}

@Composable
private fun ShowAdminFila(
    titulo: String,
    detalle: String,
    atenuado: Boolean = false,
    onBorrar: () -> Unit
) {
    var confirmar by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().glass(14.dp).padding(start = 14.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (atenuado) 0.6f else 1f)
            )
            Text(
                text = detalle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = { confirmar = true }) {
            Icon(
                AppIcons2.Delete,
                contentDescription = "Eliminar",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text("Eliminar show") },
            text = { Text(titulo) },
            confirmButton = {
                TextButton(onClick = { confirmar = false; onBorrar() }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun EpisodioAdminFila(
    episodio: EpisodioFan,
    atenuado: Boolean = false,
    onEditar: () -> Unit,
    onBorrar: () -> Unit
) {
    var confirmar by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .glass(14.dp)
            .clickable(onClick = onEditar)
            .padding(start = 14.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ) {
            Text(
                text = "#${episodio.number}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = (if (episodio.premium) "🥃 " else "") + episodio.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (atenuado) 0.6f else 1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = listOfNotNull(
                    episodio.duration.takeIf { it.isNotBlank() },
                    episodio.date?.takeIf { it.isNotBlank() }
                ).joinToString(" · ").ifBlank { "Toca para editar" },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = { confirmar = true }) {
            Icon(
                AppIcons2.Delete,
                contentDescription = "Eliminar",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text("Eliminar episodio") },
            text = { Text("#${episodio.number} · ${episodio.title}") },
            confirmButton = {
                TextButton(onClick = { confirmar = false; onBorrar() }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun CanalAdminFila(canal: Canal, editable: Boolean, onEditar: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .glass(14.dp)
            .clickable(enabled = editable, onClick = onEditar)
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = canal.plataforma.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = listOfNotNull(
                    canal.handle.takeIf { it.isNotBlank() }?.let { "@$it" },
                    canal.seguidores.takeIf { it.isNotBlank() }?.let { "$it seguidores" },
                    if (canal.visible) null else "oculto"
                ).joinToString(" · "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (editable) {
            Text(
                text = "Editar",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun UsuarioAdminFila(usuario: UsuarioAdmin, editable: Boolean, onEditar: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .glass(14.dp)
            .clickable(enabled = editable, onClick = onEditar)
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = usuario.name.ifBlank { usuario.email },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = listOfNotNull(
                    usuario.email.takeIf { it.isNotBlank() && usuario.name.isNotBlank() },
                    usuario.referidos.takeIf { it > 0 }?.let { "$it referidos" }
                ).joinToString(" · "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ) {
            Text(
                text = usuario.role,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun FilaAdmin(titulo: String, detalle: String) {
    Column(modifier = Modifier.fillMaxWidth().glass(14.dp).padding(14.dp)) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (detalle.isNotBlank()) {
            Text(
                text = detalle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VacioAdmin(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
    )
}

@Composable
private fun AvisoAdmin(texto: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "🔒 $texto",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(12.dp)
        )
    }
}

// ─────────── Formularios ───────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShowFormSheet(
    isGuardando: Boolean,
    onDismiss: () -> Unit,
    onGuardar: (String, String, String, String, String) -> Unit
) {
    var fecha by remember { mutableStateOf(todayIsoDate()) }
    var hora by remember { mutableStateOf("21:00") }
    var titulo by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(TIPOS_SHOW.first().first) }
    var descripcion by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Nuevo show",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(16.dp))
            NitanmalTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NitanmalTextField(
                    value = fecha,
                    onValueChange = { fecha = it },
                    label = { Text("Fecha (AAAA-MM-DD)") },
                    modifier = Modifier.weight(1.4f),
                    singleLine = true
                )
                NitanmalTextField(
                    value = hora,
                    onValueChange = { hora = it },
                    label = { Text("Hora") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Tipo",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TIPOS_SHOW.forEach { (valor, label) ->
                    FilterChip(
                        selected = tipo == valor,
                        onClick = { tipo = valor },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            NitanmalTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            Spacer(Modifier.height(20.dp))
            NitanmalButton(
                text = "Crear show",
                onClick = { onGuardar(fecha.trim(), hora.trim(), titulo.trim(), tipo, descripcion.trim()) },
                modifier = Modifier.fillMaxWidth(),
                isLoading = isGuardando,
                enabled = !isGuardando && titulo.isNotBlank() && fecha.length == 10 && hora.isNotBlank()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EpisodioFormSheet(
    episodio: EpisodioFan?,
    siguienteNumero: Int,
    isGuardando: Boolean,
    onDismiss: () -> Unit,
    onGuardar: (Int, String, String, String, String, Boolean, String, String) -> Unit
) {
    var numero by remember { mutableStateOf((episodio?.number ?: siguienteNumero).toString()) }
    var titulo by remember { mutableStateOf(episodio?.title ?: "") }
    var descripcion by remember { mutableStateOf(episodio?.description ?: "") }
    var duracion by remember { mutableStateOf(episodio?.duration ?: "") }
    var fecha by remember { mutableStateOf(episodio?.date ?: "") }
    var premium by remember { mutableStateOf(episodio?.premium ?: false) }
    var youtube by remember { mutableStateOf(episodio?.links?.youtube ?: "") }
    var spotify by remember { mutableStateOf(episodio?.links?.spotify ?: "") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = if (episodio == null) "Nuevo episodio" else "Editar episodio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NitanmalTextField(
                    value = numero,
                    onValueChange = { numero = it.filter { c -> c.isDigit() } },
                    label = { Text("N°") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                NitanmalTextField(
                    value = duracion,
                    onValueChange = { duracion = it },
                    label = { Text("Duración") },
                    modifier = Modifier.weight(2f),
                    singleLine = true
                )
            }
            Spacer(Modifier.height(10.dp))
            NitanmalTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(10.dp))
            NitanmalTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )
            Spacer(Modifier.height(10.dp))
            NitanmalTextField(
                value = fecha,
                onValueChange = { fecha = it },
                label = { Text("Fecha (AAAA-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(10.dp))
            NitanmalTextField(
                value = youtube,
                onValueChange = { youtube = it },
                label = { Text("Link de YouTube") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            // Vista previa del episodio en YouTube
            val idEp = youtubeVideoId(youtube)
            if (idEp.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                AsyncImage(
                    model = "https://img.youtube.com/vi/$idEp/hqdefault.jpg",
                    contentDescription = "Miniatura del episodio",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).glass(12.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            NitanmalTextField(
                value = spotify,
                onValueChange = { spotify = it },
                label = { Text("Link de Spotify") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🥃 Solo para premium",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = premium, onCheckedChange = { premium = it })
            }
            Spacer(Modifier.height(20.dp))
            NitanmalButton(
                text = if (episodio == null) "Crear episodio" else "Guardar cambios",
                onClick = {
                    onGuardar(
                        numero.toIntOrNull() ?: siguienteNumero, titulo.trim(), descripcion.trim(),
                        duracion.trim(), fecha.trim(), premium, youtube.trim(), spotify.trim()
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                isLoading = isGuardando,
                enabled = !isGuardando && titulo.isNotBlank() && numero.isNotBlank()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SorteoFormSheet(
    sorteo: com.nitanmal.app.domain.model.Sorteo? = null,
    isGuardando: Boolean,
    onDismiss: () -> Unit,
    onGuardar: (String, String, String, String) -> Unit
) {
    var titulo by remember { mutableStateOf(sorteo?.titulo ?: "") }
    var premio by remember { mutableStateOf(sorteo?.premio ?: "") }
    var como by remember {
        mutableStateOf(sorteo?.comoParticipar ?: "Toca \"Participar\". Sorteamos entre todos los registrados.")
    }
    var fecha by remember { mutableStateOf(sorteo?.fecha ?: "") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = if (sorteo == null) "Nuevo sorteo" else "Editar sorteo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(16.dp))
            NitanmalTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(10.dp))
            NitanmalTextField(
                value = premio,
                onValueChange = { premio = it },
                label = { Text("Premio") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )
            Spacer(Modifier.height(10.dp))
            NitanmalTextField(
                value = como,
                onValueChange = { como = it },
                label = { Text("Cómo participar") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            Spacer(Modifier.height(10.dp))
            NitanmalTextField(
                value = fecha,
                onValueChange = { fecha = it },
                label = { Text("Fecha del sorteo (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(20.dp))
            NitanmalButton(
                text = if (sorteo == null) "Crear sorteo" else "Guardar cambios",
                onClick = { onGuardar(titulo.trim(), premio.trim(), como.trim(), fecha.trim()) },
                modifier = Modifier.fillMaxWidth(),
                isLoading = isGuardando,
                enabled = !isGuardando && titulo.isNotBlank()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EncuestaFormSheet(
    isGuardando: Boolean,
    onDismiss: () -> Unit,
    onGuardar: (String, String, List<String>) -> Unit
) {
    var pregunta by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("si_no") }
    var opciones by remember { mutableStateOf(listOf("", "")) }

    val opcionesValidas = opciones.map { it.trim() }.filter { it.isNotBlank() }
    val puedeGuardar = pregunta.isNotBlank() && (tipo == "si_no" || opcionesValidas.size >= 2)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Nueva encuesta",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(16.dp))
            NitanmalTextField(
                value = pregunta,
                onValueChange = { pregunta = it },
                label = { Text("Pregunta") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Tipo de respuesta",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = tipo == "si_no",
                    onClick = { tipo = "si_no" },
                    label = { Text("Sí / No") }
                )
                FilterChip(
                    selected = tipo == "multiple",
                    onClick = { tipo = "multiple" },
                    label = { Text("Varias opciones") }
                )
            }
            if (tipo == "multiple") {
                Spacer(Modifier.height(14.dp))
                opciones.forEachIndexed { i, valor ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        NitanmalTextField(
                            value = valor,
                            onValueChange = { nuevo ->
                                opciones = opciones.toMutableList().also { it[i] = nuevo }
                            },
                            label = { Text("Opción ${i + 1}") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        if (opciones.size > 2) {
                            IconButton(onClick = {
                                opciones = opciones.toMutableList().also { it.removeAt(i) }
                            }) {
                                Icon(
                                    AppIcons2.Delete,
                                    contentDescription = "Quitar opción",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                if (opciones.size < 8) {
                    TextButton(onClick = { opciones = opciones + "" }) {
                        Text("+ Agregar opción")
                    }
                }
            } else {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Las respuestas serán “Sí” y “No”.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
            Spacer(Modifier.height(20.dp))
            NitanmalButton(
                text = "Crear encuesta",
                onClick = { onGuardar(pregunta.trim(), tipo, opcionesValidas) },
                modifier = Modifier.fillMaxWidth(),
                isLoading = isGuardando,
                enabled = !isGuardando && puedeGuardar
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EncuestaEditSheet(
    encuesta: com.nitanmal.app.domain.model.Encuesta,
    isGuardando: Boolean,
    onDismiss: () -> Unit,
    onGuardar: (String) -> Unit
) {
    var pregunta by remember { mutableStateOf(encuesta.pregunta) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Editar encuesta",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Las opciones y los votos se conservan; solo cambia la pregunta.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            NitanmalTextField(
                value = pregunta,
                onValueChange = { pregunta = it },
                label = { Text("Pregunta") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )
            Spacer(Modifier.height(20.dp))
            NitanmalButton(
                text = "Guardar cambios",
                onClick = { onGuardar(pregunta.trim()) },
                modifier = Modifier.fillMaxWidth(),
                isLoading = isGuardando,
                enabled = !isGuardando && pregunta.isNotBlank()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CanalFormSheet(
    canal: Canal,
    isGuardando: Boolean,
    onDismiss: () -> Unit,
    onGuardar: (Canal) -> Unit
) {
    var handle by remember { mutableStateOf(canal.handle) }
    var url by remember { mutableStateOf(canal.url) }
    var seguidores by remember { mutableStateOf(canal.seguidores) }
    var visible by remember { mutableStateOf(canal.visible) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = canal.plataforma.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(16.dp))
            NitanmalTextField(
                value = handle,
                onValueChange = { handle = it },
                label = { Text("Usuario (handle)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(10.dp))
            NitanmalTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("URL del canal") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(10.dp))
            NitanmalTextField(
                value = seguidores,
                onValueChange = { seguidores = it },
                label = { Text("Seguidores") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Visible en la web y la app",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = visible, onCheckedChange = { visible = it })
            }
            Spacer(Modifier.height(20.dp))
            NitanmalButton(
                text = "Guardar canal",
                onClick = {
                    onGuardar(
                        canal.copy(
                            handle = handle.trim(),
                            url = url.trim(),
                            seguidores = seguidores.trim(),
                            visible = visible
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                isLoading = isGuardando,
                enabled = !isGuardando && url.startsWith("http")
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RolFormSheet(
    usuario: UsuarioAdmin,
    isGuardando: Boolean,
    onDismiss: () -> Unit,
    onGuardar: (String) -> Unit
) {
    val roles = listOf("miembro", "participante", "admin", "superadmin")
    var rol by remember { mutableStateOf(usuario.role) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = usuario.name.ifBlank { usuario.email },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (usuario.email.isNotBlank()) {
                Text(
                    text = usuario.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Rol",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            roles.forEach { r ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { rol = r }
                        .padding(vertical = 10.dp, horizontal = 4.dp)
                ) {
                    RadioButton(selected = rol == r, onClick = { rol = r })
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = r.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when (r) {
                                "miembro" -> "Solo la zona de registrados"
                                "participante" -> "Accede a la agenda del equipo"
                                "admin" -> "Agenda + panel de administración"
                                else -> "Control total, incluidos usuarios y canales"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            NitanmalButton(
                text = "Guardar rol",
                onClick = { onGuardar(rol) },
                modifier = Modifier.fillMaxWidth(),
                isLoading = isGuardando,
                enabled = !isGuardando && rol != usuario.role
            )
        }
    }
}

/** Card grande de sección admin: mitad del ancho, casi cuadrada. */
@Composable
private fun SeccionAdminCard(
    seccion: SeccionAdmin,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1.15f)
            .glass(22.dp)
            .clickable(onClick = onClick)
    ) {
        if (seccion.badge > 0) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color(0xFFdc2626),
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
            ) {
                Text(
                    text = "● EN VIVO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Text(text = seccion.emoji, style = MaterialTheme.typography.headlineMedium)
            Column {
                Text(
                    text = seccion.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = seccion.resumen,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

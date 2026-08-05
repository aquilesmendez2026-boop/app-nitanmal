package com.nitanmal.app.presentation.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.nitanmal.app.core.localization.rememberStrings
import com.nitanmal.app.data.repository.FanRepositoryImpl
import com.nitanmal.app.data.repository.TeamRepositoryImpl
import com.nitanmal.app.domain.auth.LocalPlatformAuth
import com.nitanmal.app.presentation.ui.components.atoms.FondoNocturno
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalButton
import com.nitanmal.app.presentation.ui.components.atoms.glass
import com.nitanmal.app.presentation.ui.components.molecules.CanalCard
import com.nitanmal.app.presentation.ui.icons.AppIcons2
import com.nitanmal.app.presentation.viewmodel.CanalesViewModel
import com.nitanmal.app.presentation.viewmodel.FanViewModel
import nitanmal.composeapp.generated.resources.Res
import nitanmal.composeapp.generated.resources.logo_nitanmal
import org.jetbrains.compose.resources.painterResource

// ═══════════════ PORTADA PÚBLICA (sin navbar) ═══════════════
// La ve cualquier visitante tras el splash. Cada bloque abre una
// página de detalle con flecha para volver, como en la web.

private sealed interface DetallePortada {
    data object Episodios : DetallePortada
    data class Episodio(val id: String) : DetallePortada
    data object EnVivo : DetallePortada
    data object Sorteo : DetallePortada
    data object Miembro : DetallePortada
    data object Show : DetallePortada
    data object Horarios : DetallePortada
    data object Canales : DetallePortada
    data object Buzon : DetallePortada
}

private data class FormatoPortada(val tag: String, val titulo: String, val descripcion: String)

private val FORMATOS_PORTADA = listOf(
    FormatoPortada(
        tag = "En vivo",
        titulo = "Streamers de juegos",
        descripcion = "Partidas en directo, reacciones honestas y retos que nadie en su sano juicio aceptaría. Los juegos son la excusa; el caos es el contenido."
    ),
    FormatoPortada(
        tag = "Sin filtro",
        titulo = "Noches de conversación con un trago",
        descripcion = "Cuando bajan las luces y sube el trago, salen las mejores historias. Charlas relajadas sobre la vida, las decisiones impulsivas y todo lo que hacemos antes de pensar."
    )
)

private val VALORES_PORTADA = listOf("Sin filtro", "Risas garantizadas", "Anécdotas reales", "Cero pretensiones")

private val BENEFICIOS_PORTADA = listOf(
    "✨ Contenido exclusivo",
    "⬇️ Descargas exclusivas",
    "🗳️ Vota temas y sugiere invitados",
    "🎁 Sorteos y novedades primero"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortadaPublicaScreen(
    onLoginClick: () -> Unit,
    user: com.nitanmal.app.domain.model.User? = null,
    onGoToZona: () -> Unit = {},
    onGoToAjustes: () -> Unit = {},
    fanViewModelExterno: FanViewModel? = null,
    canalesViewModelExterno: CanalesViewModel? = null,
    /** false cuando va embebida en el shell (que ya pone fondo e insets). */
    aplicarFondo: Boolean = true,
    modifier: Modifier = Modifier
) {
    val platformAuth = LocalPlatformAuth.current
    val fanRepository = remember { FanRepositoryImpl(platformAuth) }
    val teamRepository = remember { TeamRepositoryImpl(platformAuth) }
    val fanViewModel = remember { fanViewModelExterno ?: FanViewModel(fanRepository) }
    val canalesViewModel = remember { canalesViewModelExterno ?: CanalesViewModel(teamRepository) }
    val uiState by fanViewModel.uiState.collectAsState()
    val canalesState by canalesViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState.episodios.isEmpty()) fanViewModel.load() else fanViewModel.refreshLive()
        if (canalesState.canales.isEmpty()) canalesViewModel.load()
    }

    var detalle by remember { mutableStateOf<DetallePortada?>(null) }

    FondoNocturno(modifier = modifier, activo = aplicarFondo) {
        Box(
            modifier = if (aplicarFondo) Modifier.fillMaxSize().statusBarsPadding()
            else Modifier.fillMaxSize()
        ) {
        when (val d = detalle) {
            null -> PortadaHome(
                fanViewModel = fanViewModel,
                canalesViewModel = canalesViewModel,
                user = user,
                onLoginClick = onLoginClick,
                onGoToZona = onGoToZona,
                onGoToAjustes = onGoToAjustes,
                onOpen = { detalle = it }
            )

            is DetallePortada.Episodio -> EpisodioFanDetailScreen(
                episodioId = d.id,
                fanViewModel = fanViewModel,
                esPremiumUsuario = false,
                onNavigateBack = { detalle = DetallePortada.Episodios }
            )

            DetallePortada.Episodios -> DetallePagina("Episodios", onBack = { detalle = null }) {
                if (uiState.isLoading && uiState.episodios.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                uiState.episodios.forEach { episodio ->
                    EpisodioCardFan(
                        episodio = episodio,
                        esPremiumUsuario = false,
                        onClick = { detalle = DetallePortada.Episodio(episodio.id) }
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }

            DetallePortada.EnVivo -> DetallePagina("En vivo", onBack = { detalle = null }, scrolleable = false) {
                EnVivoScreen(fanViewModel = fanViewModel, modifier = Modifier.weight(1f))
            }

            DetallePortada.Sorteo -> DetallePagina("Sorteo", onBack = { detalle = null }) {
                val sorteo = uiState.sorteosPublicos.firstOrNull()
                if (sorteo == null) {
                    Text(
                        text = "No hay sorteos activos ahora mismo. Vuelve pronto.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(modifier = Modifier.fillMaxWidth().glass(20.dp)) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(text = "🎁", style = MaterialTheme.typography.headlineMedium)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = sorteo.titulo,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = sorteo.premio,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    BotonGradiente(
                        texto = if (user != null) "Participar desde tu zona" else "Participar gratis",
                        onClick = { if (user != null) onGoToZona() else onLoginClick() }
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (user != null) "Los sorteos se juegan en tu zona de miembro."
                        else "Necesitas una cuenta gratis para participar.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            DetallePortada.Miembro -> DetallePagina("Hazte miembro", onBack = { detalle = null }) {
                Text(
                    text = "⭐ ZONA DE REGISTRADOS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Regístrate gratis y desbloquea más",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(16.dp))
                BENEFICIOS_PORTADA.forEach { beneficio ->
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).glass(14.dp)
                    ) {
                        Text(
                            text = beneficio,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                uiState.sorteosPublicos.firstOrNull()?.let { sorteo ->
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color(0xFFf59e0b).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "🎁 Sorteo activo: ${sorteo.titulo}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFfbbf24),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
                BotonGradiente(texto = "Crear cuenta gratis", onClick = onLoginClick)
                Spacer(Modifier.height(10.dp))
                Text(
                    text = buildAnnotatedString {
                        append("¿Ya tienes cuenta? ")
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                            append("Inicia sesión")
                        }
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onLoginClick)
                )
            }

            DetallePortada.Show -> DetallePagina("El show", onBack = { detalle = null }) {
                SeccionCentrada(
                    eyebrow = "El show",
                    titulo = "Lo que pasa cuando primero se actúa y después se piensa",
                    subtitulo = "Ni Tan Mal es la mesa donde se cuentan las historias que no contarías sobrio. Un grupo de amigos, micrófonos abiertos y la honestidad brutal de quienes ya hicieron de todo… y lo volverían a hacer."
                )
                Spacer(Modifier.height(16.dp))
                ValoresChips()
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "“Al final del día, ninguna locura fue tan grave. Estuvo… ni tan mal.”",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(28.dp))
                SeccionCentrada(
                    eyebrow = "Formatos",
                    titulo = "Dos formas de meterse en problemas",
                    subtitulo = "Cada semana alternamos entre la consola y la copa. Mismo desorden, distinto escenario."
                )
                Spacer(Modifier.height(16.dp))
                FORMATOS_PORTADA.forEach { formato ->
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).glass(18.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = formato.tag,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = formato.titulo,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = formato.descripcion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            DetallePortada.Horarios -> DetallePagina("Horarios", onBack = { detalle = null }) {
                SeccionCentrada(
                    eyebrow = "Horarios",
                    titulo = "¿Cuándo nos sintonizas?",
                    subtitulo = "Estos son los próximos shows en vivo. Guárdalos antes de que se te olvide."
                )
                Spacer(Modifier.height(16.dp))
                if (uiState.proximosEventos.isEmpty()) {
                    Text(
                        text = "Aún no hay shows programados. Muy pronto anunciaremos las próximas fechas.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                    )
                } else {
                    uiState.proximosEventos.forEach { evento ->
                        EventoCard(evento)
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }

            DetallePortada.Canales -> DetallePagina("Nuestros canales", onBack = { detalle = null }) {
                Text(
                    text = "Síguenos y no te pierdas nada.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                canalesState.visibles.forEach { canal ->
                    Box(modifier = Modifier.padding(bottom = 10.dp)) { CanalCard(canal) }
                }
            }

            DetallePortada.Buzon -> DetallePagina("Buzón", onBack = { detalle = null }) {
                val strings = rememberStrings()
                Text(
                    text = strings.fanBuzonCta,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = strings.fanBuzonDesc,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
                NitanmalButton(
                    text = strings.fanEnviar,
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Necesitas una cuenta gratis para enviar tu pregunta.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Avisos (pregunta enviada, errores)
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                action = { TextButton(onClick = { fanViewModel.clearError() }) { Text("OK") } }
            ) { Text(error) }
        }
        uiState.info?.let { info ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = Color.White,
                action = {
                    TextButton(onClick = { fanViewModel.clearInfo() }) { Text("OK", color = Color.White) }
                }
            ) { Text(info) }
        }
        }
    }

    // Hoja del buzón (solo con sesión)
    if (uiState.showPreguntaSheet) {
        val strings = rememberStrings()
        ModalBottomSheet(onDismissRequest = { fanViewModel.setShowPreguntaSheet(false) }) {
            var contenido by remember { mutableStateOf("") }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = strings.fanBuzonCta,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))
                com.nitanmal.app.presentation.ui.components.atoms.NitanmalTextField(
                    value = contenido,
                    onValueChange = { contenido = it },
                    placeholder = { Text(strings.fanBuzonPlaceholder) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    enabled = !uiState.isEnviandoPregunta
                )
                Spacer(Modifier.height(20.dp))
                NitanmalButton(
                    text = strings.fanEnviar,
                    onClick = { fanViewModel.enviarPregunta(contenido.trim()) },
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = uiState.isEnviandoPregunta,
                    enabled = !uiState.isEnviandoPregunta && contenido.isNotBlank()
                )
            }
        }
    }
}

// ─────────── Home de la portada (página continua, como la web) ───────────

@Composable
private fun PortadaHome(
    fanViewModel: FanViewModel,
    canalesViewModel: CanalesViewModel,
    user: com.nitanmal.app.domain.model.User? = null,
    onLoginClick: () -> Unit,
    onGoToZona: () -> Unit = {},
    onGoToAjustes: () -> Unit = {},
    onOpen: (DetallePortada) -> Unit
) {
    val nombreCorto = user?.apodo?.takeIf { it.isNotBlank() }
        ?: user?.name?.split(" ")?.firstOrNull() ?: ""

    val strings = rememberStrings()
    val uiState by fanViewModel.uiState.collectAsState()
    val canalesState by canalesViewModel.uiState.collectAsState()
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        // Barra superior: identidad + login arriba a la derecha
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ni Tan Mal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            if (user != null) {
                // Sesión iniciada: el botón muestra tu nombre y abre Ajustes.
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    modifier = Modifier.clip(RoundedCornerShape(999.dp)).clickable(onClick = onGoToAjustes)
                ) {
                    Text(
                        text = "👤 $nombreCorto",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                    )
                }
            } else {
                Button(
                    onClick = onLoginClick,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(strings.fanEntrar, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Hero ──
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "PODCAST · EN VIVO Y SIN FILTRO",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            Image(
                painter = painterResource(Res.drawable.logo_nitanmal),
                contentDescription = "Ni Tan Mal",
                modifier = Modifier.size(200.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = buildAnnotatedString {
                    append("NI TAN ")
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) { append("MAL") }
                },
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "El podcast de los hombres y las locuras que hacen antes de pensar. Streamers de juegos, noches de conversación con un trago y cero pretensiones.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            )
                        )
                        .clickable { onOpen(DetallePortada.Episodios) }
                        .padding(horizontal = 24.dp, vertical = 14.dp)
                ) {
                    Text(text = "▶ Escúchanos", fontWeight = FontWeight.Bold, color = Color.White)
                }
                OutlinedButton(
                    onClick = { onOpen(DetallePortada.Horarios) },
                    shape = RoundedCornerShape(999.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text("Próximos shows", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(48.dp))

        // ── En vivo ──
        val live = uiState.live
        if (live?.isLive == true) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFFef4444).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFFef4444).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "● EN VIVO AHORA",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFfca5a5),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = live.title.ifBlank { "Estamos transmitiendo en vivo" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(14.dp))
                BotonGradiente(texto = "Abrir en YouTube") {
                    if (live.videoId.isNotBlank()) {
                        runCatching { uriHandler.openUri("https://www.youtube.com/watch?v=${live.videoId}") }
                    }
                }
            }
        } else {
            SeccionCentrada(
                eyebrow = "En vivo",
                titulo = "No estamos transmitiendo ahora mismo",
                subtitulo = uiState.proximoEvento?.let { "Pero se viene el próximo show. Prepárate:" }
                    ?: "Muy pronto anunciaremos la próxima transmisión."
            )
            uiState.proximoEvento?.let { proximo ->
                Spacer(Modifier.height(16.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().glass(20.dp).padding(20.dp)
                ) {
                    Text(
                        text = proximo.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${proximo.date} · ${proximo.time} hrs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(48.dp))

        // ── Zona de registrados (panel de vidrio con borde neón, como la web) ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                    RoundedCornerShape(24.dp)
                )
                .padding(22.dp)
        ) {
            if (user != null) {
            Text(
                text = "⭐ TU ZONA",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Hola, $nombreCorto 👋",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (uiState.sorteosPublicos.isNotEmpty())
                    "Hay un sorteo activo y novedades en la comunidad. Entra a tu zona: contenido exclusivo, descargas y votaciones."
                else
                    "Entra a tu zona: contenido exclusivo, descargas, y vota temas y sugiere invitados.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            BotonGradiente(texto = "Ir a tu zona →", onClick = onGoToZona)
            } else {
            Text(
                text = "⭐ ZONA DE REGISTRADOS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Regístrate gratis y desbloquea más",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            BENEFICIOS_PORTADA.forEach { beneficio ->
                Text(
                    text = beneficio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
            uiState.sorteosPublicos.firstOrNull()?.let { sorteo ->
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFFf59e0b).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFFf59e0b).copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "🎁 Sorteo activo: ${sorteo.titulo}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFfbbf24),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            BotonGradiente(texto = "Crear cuenta gratis", onClick = onLoginClick)
            Spacer(Modifier.height(10.dp))
            Text(
                text = buildAnnotatedString {
                    append("¿Ya tienes cuenta? ")
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                        append("Inicia sesión")
                    }
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().clickable(onClick = onLoginClick)
            )
            }
        }

        Spacer(Modifier.height(48.dp))

        // ── El show ──
        SeccionCentrada(
            eyebrow = "El show",
            titulo = "Lo que pasa cuando primero se actúa y después se piensa",
            subtitulo = "Ni Tan Mal es la mesa donde se cuentan las historias que no contarías sobrio. Un grupo de amigos, micrófonos abiertos y la honestidad brutal de quienes ya hicieron de todo… y lo volverían a hacer."
        )
        Spacer(Modifier.height(18.dp))
        ValoresChips()
        Spacer(Modifier.height(18.dp))
        Text(
            text = "“Al final del día, ninguna locura fue tan grave. Estuvo… ni tan mal.”",
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(48.dp))

        // ── Formatos ──
        SeccionCentrada(
            eyebrow = "Formatos",
            titulo = "Dos formas de meterse en problemas",
            subtitulo = "Cada semana alternamos entre la consola y la copa. Mismo desorden, distinto escenario."
        )
        Spacer(Modifier.height(18.dp))
        FORMATOS_PORTADA.forEach { formato ->
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).glass(18.dp).padding(18.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = formato.tag,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = formato.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = formato.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(48.dp))

        // ── Horarios ──
        SeccionCentrada(
            eyebrow = "Horarios",
            titulo = "¿Cuándo nos sintonizas?",
            subtitulo = "Estos son los próximos shows en vivo. Guárdalos antes de que se te olvide."
        )
        Spacer(Modifier.height(18.dp))
        if (uiState.proximosEventos.isEmpty()) {
            Text(
                text = "Aún no hay shows programados. Muy pronto anunciaremos las próximas fechas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            )
        } else {
            uiState.proximosEventos.take(4).forEach { evento ->
                EventoCard(evento)
                Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(48.dp))

        // ── Episodios ──
        SeccionCentrada(
            eyebrow = "Episodios",
            titulo = "Los últimos capítulos",
            subtitulo = "Con notas del show y enlaces para escucharlos donde quieras."
        )
        Spacer(Modifier.height(18.dp))
        uiState.recientes.forEach { episodio ->
            EpisodioCardFan(
                episodio = episodio,
                esPremiumUsuario = false,
                onClick = { onOpen(DetallePortada.Episodio(episodio.id)) }
            )
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height(6.dp))
        OutlinedButton(
            onClick = { onOpen(DetallePortada.Episodios) },
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Ver todos los episodios →", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(48.dp))

        // ── Buzón ──
        SeccionCentrada(
            eyebrow = "Buzón",
            titulo = strings.fanBuzonCta,
            subtitulo = strings.fanBuzonDesc
        )
        Spacer(Modifier.height(18.dp))
        BotonGradiente(
            texto = strings.fanEnviar,
            onClick = { if (user != null) fanViewModel.setShowPreguntaSheet(true) else onLoginClick() }
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (user != null) "Tu pregunta llega directo al buzón del equipo."
            else "Necesitas una cuenta gratis para enviar tu pregunta.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // ── Canales ──
        if (canalesState.visibles.isNotEmpty()) {
            Spacer(Modifier.height(48.dp))
            SeccionCentrada(
                eyebrow = "Canales",
                titulo = "Síguenos y no te pierdas nada"
            )
            Spacer(Modifier.height(18.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                canalesState.visibles.forEach { canal -> CanalCard(canal) }
            }
        }

        Spacer(Modifier.height(40.dp))
        Text(
            text = "© 2026 Ni Tan Mal",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )
    }
}

// ─────────── Piezas ───────────

/** Página de detalle con flecha para volver y título de sección. */
@Composable
private fun DetallePagina(
    titulo: String,
    onBack: () -> Unit,
    scrolleable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    AppIcons2.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        val base = Modifier.fillMaxSize().padding(horizontal = 16.dp)
        Column(modifier = if (scrolleable) base.verticalScroll(rememberScrollState()) else base) {
            content()
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Card alta de introducción de un bloque; toda la card navega al detalle. */
@Composable
private fun CardPortada(
    emoji: String,
    titulo: String,
    descripcion: String,
    accent: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
            .glass(22.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = accent.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "Ver más →",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            }
        }
    }
}

/** Título de sección centrado con subrayado degradado, como la web. */
@Composable
private fun SeccionCentrada(
    eyebrow: String,
    titulo: String,
    subtitulo: String? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = eyebrow.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
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
        if (subtitulo != null) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BotonGradiente(
    texto: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(999.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                )
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp)
    ) {
        Text(text = texto, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun ValoresChips(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VALORES_PORTADA.take(2).forEach { ValorChip(it) }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VALORES_PORTADA.drop(2).forEach { ValorChip(it) }
        }
    }
}

@Composable
private fun ValorChip(valor: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
    ) {
        Text(
            text = valor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

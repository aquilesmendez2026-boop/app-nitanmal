package com.nitanmal.app.presentation.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import nitanmal.composeapp.generated.resources.Res
import nitanmal.composeapp.generated.resources.logo_nitanmal
import org.jetbrains.compose.resources.painterResource
import com.nitanmal.app.core.localization.rememberStrings
import com.nitanmal.app.domain.model.EpisodioFan
import com.nitanmal.app.domain.model.Evento
import com.nitanmal.app.domain.model.User
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalButton
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalTextField
import com.nitanmal.app.presentation.ui.components.molecules.CanalCard
import com.nitanmal.app.presentation.ui.icons.AppIcons2
import com.nitanmal.app.presentation.viewmodel.CanalesViewModel
import com.nitanmal.app.presentation.viewmodel.FanViewModel

private fun youtubeUrl(videoId: String) = "https://www.youtube.com/watch?v=$videoId"

// ═══════════════ INICIO FAN ═══════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioFanScreen(
    user: User?,
    fanViewModel: FanViewModel,
    canalesViewModel: CanalesViewModel,
    onGoToEnVivo: () -> Unit,
    onGoToEpisodios: () -> Unit,
    onOpenEpisodio: (String) -> Unit,
    onSwitchToEquipo: (() -> Unit)?,
    onLoginClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val uiState by fanViewModel.uiState.collectAsState()
    val canalesState by canalesViewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        if (uiState.episodios.isEmpty()) fanViewModel.load() else fanViewModel.refreshLive()
        if (canalesState.canales.isEmpty()) canalesViewModel.load()
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Saludo + switch modo equipo
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ni Tan Mal",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (user != null) {
                                "¡Hola, ${user.apodo?.takeIf { it.isNotBlank() } ?: user.name.split(" ").firstOrNull() ?: ""}!"
                            } else {
                                "El podcast sin filtro"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (user == null && onLoginClick != null) {
                        // Botón de login para visitantes (arriba a la derecha)
                        Button(
                            onClick = onLoginClick,
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(strings.fanEntrar, fontWeight = FontWeight.Bold)
                        }
                    } else if (onSwitchToEquipo != null) {
                        TextButton(onClick = onSwitchToEquipo) {
                            Text(
                                text = strings.cuentaModoEquipo,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Hero de portada (visitantes): logo protagonista, como la web
            if (user == null) {
                item {
                    HeroPortada(
                        onEscuchanos = onGoToEpisodios,
                        onProximosShows = onGoToEnVivo
                    )
                }
            }

            // Banner EN VIVO / próximo show
            item {
                val live = uiState.live
                if (live?.isLive == true) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFdc2626), Color(0xFF7f1d1d))
                                )
                            )
                            .clickable(onClick = onGoToEnVivo)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = strings.fanEnVivoAhora,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            if (live.title.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = live.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                } else {
                    val proximo = uiState.proximoEvento
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(onClick = onGoToEnVivo)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = strings.fanProximoShow,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            if (proximo != null) {
                                Text(
                                    text = proximo.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${proximo.date} · ${proximo.time} hrs",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    text = strings.fanSinShows,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Sorteo activo (teaser público)
            uiState.sorteosPublicos.firstOrNull()?.let { sorteo ->
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "🎁 ${strings.fanSorteoActivo}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${sorteo.titulo} — ${sorteo.premio}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (user == null) {
                // Beneficios de registrarse (CTA de cuenta gratis)
                item {
                    BeneficiosRegistroCard(
                        sorteoTitulo = uiState.sorteosPublicos.firstOrNull()?.titulo,
                        onLoginClick = { onLoginClick?.invoke() }
                    )
                }
                // El show + formatos, mismo copy que la web
                item { AboutPortada() }
                item { FormatosPortada() }
                // Horarios: próximos shows
                if (uiState.proximosEventos.isNotEmpty()) {
                    item {
                        SeccionTitulo(
                            eyebrow = "Horarios",
                            titulo = "¿Cuándo nos sintonizas?",
                            subtitulo = "Estos son los próximos shows en vivo. Guárdalos antes de que se te olvide."
                        )
                    }
                    items(uiState.proximosEventos.take(6), key = { "ev-${it.id}" }) { evento ->
                        EventoCard(evento)
                    }
                }
            }

            // Episodios recientes
            if (uiState.recientes.isNotEmpty()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = strings.fanEpisodiosRecientes,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = strings.fanVerTodos,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable(onClick = onGoToEpisodios)
                        )
                    }
                }
                items(uiState.recientes, key = { it.id }) { episodio ->
                    EpisodioCardFan(
                        episodio = episodio,
                        esPremiumUsuario = user?.esPremium == true,
                        onClick = { onOpenEpisodio(episodio.id) }
                    )
                }
            }

            // Buzón CTA
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = strings.fanBuzonCta,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = strings.fanBuzonDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        NitanmalButton(
                            text = strings.fanEnviar,
                            onClick = {
                                // El buzón requiere sesión: los visitantes van al login.
                                if (user == null) onLoginClick?.invoke()
                                else fanViewModel.setShowPreguntaSheet(true)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Canales / redes sociales
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        canalesState.visibles.forEach { canal -> CanalCard(canal) }
                    }
                }
            }

            // Footer
            item {
                Text(
                    text = "© 2026 Ni Tan Mal",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
            }
        }

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
                    TextButton(onClick = { fanViewModel.clearInfo() }) {
                        Text("OK", color = Color.White)
                    }
                }
            ) { Text(info) }
        }
    }

    if (uiState.showPreguntaSheet) {
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
                NitanmalTextField(
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

// ═══════════════ SECCIONES DE PORTADA (visitantes, espejo de la web) ═══════════════

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

@Composable
private fun SeccionTitulo(
    eyebrow: String,
    titulo: String,
    subtitulo: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth().padding(top = 16.dp)
    ) {
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
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        if (subtitulo != null) {
            Spacer(Modifier.height(6.dp))
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
private fun HeroPortada(
    onEscuchanos: () -> Unit,
    onProximosShows: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Text(
            text = "PODCAST · EN VIVO Y SIN FILTRO",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(10.dp))
        Image(
            painter = painterResource(Res.drawable.logo_nitanmal),
            contentDescription = "Ni Tan Mal",
            modifier = Modifier.size(190.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = buildAnnotatedString {
                append("NI TAN ")
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) { append("MAL") }
            },
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "El podcast de los hombres y las locuras que hacen antes de pensar. Streamers de juegos, noches de conversación con un trago y cero pretensiones.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
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
                    .clickable(onClick = onEscuchanos)
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "▶ Escúchanos",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            OutlinedButton(
                onClick = onProximosShows,
                shape = RoundedCornerShape(999.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text("Próximos shows", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun BeneficiosRegistroCard(
    sorteoTitulo: String?,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "⭐ ZONA DE REGISTRADOS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Regístrate gratis y desbloquea más",
                style = MaterialTheme.typography.titleLarge,
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
            if (sorteoTitulo != null) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFFf59e0b).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "🎁 Sorteo activo: $sorteoTitulo",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFfbbf24),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        )
                    )
                    .clickable(onClick = onLoginClick)
                    .padding(vertical = 14.dp)
            ) {
                Text(
                    text = "Crear cuenta gratis",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(Modifier.height(8.dp))
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AboutPortada(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        SeccionTitulo(
            eyebrow = "El show",
            titulo = "Lo que pasa cuando primero se actúa y después se piensa",
            subtitulo = "Ni Tan Mal es la mesa donde se cuentan las historias que no contarías sobrio. Un grupo de amigos, micrófonos abiertos y la honestidad brutal de quienes ya hicieron de todo… y lo volverían a hacer."
        )
        Spacer(Modifier.height(14.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            VALORES_PORTADA.forEach { valor ->
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                ) {
                    Text(
                        text = valor,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = "“Al final del día, ninguna locura fue tan grave. Estuvo… ni tan mal.”",
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FormatosPortada(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        SeccionTitulo(
            eyebrow = "Formatos",
            titulo = "Dos formas de meterse en problemas",
            subtitulo = "Cada semana alternamos entre la consola y la copa. Mismo desorden, distinto escenario."
        )
        Spacer(Modifier.height(14.dp))
        FORMATOS_PORTADA.forEach { formato ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                    Spacer(Modifier.height(8.dp))
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
private fun EventoCard(evento: Evento, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
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

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
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

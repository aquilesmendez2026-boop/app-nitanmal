package com.nitanmal.app.presentation.ui.components.molecules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nitanmal.app.core.localization.rememberStrings
import com.nitanmal.app.domain.model.plataformaLabel

/**
 * Tarjeta de canal (espejo del CanalCard del web): color de marca, @handle,
 * seguidores formateados con su sustantivo, badge EN VIVO y enlace al canal.
 */
@Composable
fun CanalCard(
    canal: com.nitanmal.app.domain.model.Canal,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val meta = com.nitanmal.app.domain.model.PLATAFORMA_META[canal.plataforma]
    val color = meta?.color?.let { androidx.compose.ui.graphics.Color(it) }
        ?: MaterialTheme.colorScheme.onSurface

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .width(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = canal.url.isNotBlank()) {
                runCatching { uriHandler.openUri(canal.url) }
            }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Punto de color de la marca
                Surface(
                    shape = CircleShape,
                    color = color,
                    modifier = Modifier.size(10.dp)
                ) {}
                Spacer(Modifier.width(6.dp))
                Text(
                    text = meta?.label ?: plataformaLabel(canal.plataforma),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }

            if (canal.enVivo) {
                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(6.dp)
                        ) {}
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = strings.canalesEnVivo,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (canal.handle.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "@${canal.handle}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (canal.seguidores.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = com.nitanmal.app.domain.model.fmtSeguidores(canal.seguidores),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = meta?.noun ?: strings.metricasSeguidores,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Ir al canal ↗",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}


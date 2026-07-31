package com.nitanmal.app.presentation.ui.components.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nitanmal.app.domain.model.NOTA_ETIQUETAS
import com.nitanmal.app.domain.model.NotaEstado

/** Chip de estado de una idea, con menú para cambiarlo. */
@Composable
fun EstadoChip(
    estado: NotaEstado,
    onEstadoChange: ((NotaEstado) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var menuOpen by remember { mutableStateOf(false) }
    val color = estadoColor(estado)

    Box(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.15f),
            modifier = if (onEstadoChange != null) {
                Modifier.clip(RoundedCornerShape(8.dp)).clickable { menuOpen = true }
            } else Modifier
        ) {
            Text(
                text = estado.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = color,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        if (onEstadoChange != null) {
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                NotaEstado.entries.forEach { opcion ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = opcion.label,
                                color = estadoColor(opcion),
                                fontWeight = if (opcion == estado) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            menuOpen = false
                            if (opcion != estado) onEstadoChange(opcion)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun estadoColor(estado: NotaEstado): Color = when (estado) {
    NotaEstado.NUEVA -> MaterialTheme.colorScheme.primary
    NotaEstado.REVISION -> Color(0xFFf59e0b)
    NotaEstado.APROBADA -> MaterialTheme.colorScheme.tertiary
    NotaEstado.DESCARTADA -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    NotaEstado.CONVERTIDA -> Color(0xFF8b5cf6)
}

/** Chip de etiqueta con el color fijo del set del web. */
@Composable
fun EtiquetaChip(
    etiqueta: String,
    selected: Boolean = true,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val color = NOTA_ETIQUETAS.firstOrNull { it.first == etiqueta }?.second
        ?.let { Color(it) } ?: MaterialTheme.colorScheme.secondary

    var chipModifier = modifier.clip(RoundedCornerShape(8.dp))
    chipModifier = if (selected) {
        chipModifier.background(color.copy(alpha = 0.18f))
    } else {
        chipModifier.border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
    }
    if (onClick != null) chipModifier = chipModifier.clickable(onClick = onClick)

    Text(
        text = etiqueta,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Medium,
        color = if (selected) color else color.copy(alpha = 0.8f),
        modifier = chipModifier.padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

/** Fila de reacciones: cada emoji con su conteo; resalta la del usuario actual. */
@Composable
fun ReaccionesRow(
    reacciones: Map<String, String>,
    currentUserId: String,
    emojis: List<String>,
    onReaccionar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val propia = reacciones[currentUserId]
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        emojis.forEach { emoji ->
            val count = reacciones.values.count { it == emoji }
            val esPropia = propia == emoji
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (esPropia) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onReaccionar(emoji) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = emoji, style = MaterialTheme.typography.bodySmall)
                    if (count > 0) {
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "$count",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (esPropia) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

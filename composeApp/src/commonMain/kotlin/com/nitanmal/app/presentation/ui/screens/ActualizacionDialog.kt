package com.nitanmal.app.presentation.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nitanmal.app.presentation.viewmodel.ActualizacionUiState

/** Aviso de nueva versión disponible. Obligatoria = no se puede cerrar. */
@Composable
fun ActualizacionDialog(
    estado: ActualizacionUiState,
    versionInstalada: String,
    onDescartar: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    AlertDialog(
        onDismissRequest = { if (!estado.obligatoria) onDescartar() },
        title = {
            Text(
                text = if (estado.obligatoria) "Actualización necesaria" else "Hay una nueva versión 🎉",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = if (estado.obligatoria) {
                        "Esta versión ya no es compatible. Actualiza para seguir usando Ni Tan Mal."
                    } else {
                        "La versión ${estado.versionNueva} ya está disponible."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                if (estado.notas.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = estado.notas,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Tienes la $versionInstalada · nueva ${estado.versionNueva}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (estado.url.isNotBlank()) runCatching { uriHandler.openUri(estado.url) }
            }) {
                Text("Actualizar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            if (!estado.obligatoria) {
                TextButton(onClick = onDescartar) { Text("Ahora no") }
            }
        }
    )
}

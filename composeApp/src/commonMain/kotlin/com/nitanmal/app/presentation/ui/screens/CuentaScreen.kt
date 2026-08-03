package com.nitanmal.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.nitanmal.app.core.localization.rememberStrings
import com.nitanmal.app.domain.model.User
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalButton
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalButtonVariant
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalTextField

/** Cuenta (modo fan): perfil editable, plan, tema y acciones. */
@Composable
fun CuentaScreen(
    user: User,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onGuardarPerfil: (apodo: String, pais: String, region: String, telefono: String) -> Unit,
    onSwitchToEquipo: (() -> Unit)?,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()
    var apodo by remember(user.id) { mutableStateOf(user.apodo ?: "") }
    var pais by remember(user.id) { mutableStateOf(user.pais ?: "") }
    var region by remember(user.id) { mutableStateOf(user.region ?: "") }
    var telefono by remember(user.id) { mutableStateOf(user.telefono ?: "") }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = strings.cuentaTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Cabecera de perfil
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(20.dp)
                ) {
                    user.photoUrl?.let { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                        )
                        Spacer(Modifier.width(16.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            user.role?.let { role ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = role.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (user.esPremium) {
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                }
                            ) {
                                Text(
                                    text = if (user.esPremium) "🥃 Premium" else "Free",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (user.esPremium) MaterialTheme.colorScheme.secondary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Perfil editable
        item {
            Text(
                text = strings.cuentaPerfil,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                NitanmalTextField(
                    value = apodo,
                    onValueChange = { apodo = it },
                    label = { Text(strings.cuentaApodo) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NitanmalTextField(
                        value = pais,
                        onValueChange = { pais = it },
                        label = { Text(strings.cuentaPais) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    NitanmalTextField(
                        value = region,
                        onValueChange = { region = it },
                        label = { Text(strings.cuentaRegion) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                NitanmalTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text(strings.cuentaTelefono) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                NitanmalButton(
                    text = strings.cuentaGuardar,
                    onClick = { onGuardarPerfil(apodo, pais, region, telefono) },
                    modifier = Modifier.fillMaxWidth(),
                    variant = NitanmalButtonVariant.Secondary
                )
            }
        }

        // Tema oscuro
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.settingsDarkTheme,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(checked = isDarkTheme, onCheckedChange = { onThemeToggle() })
                }
            }
        }

        // Modo equipo
        if (onSwitchToEquipo != null) {
            item {
                NitanmalButton(
                    text = strings.cuentaModoEquipo,
                    onClick = onSwitchToEquipo,
                    modifier = Modifier.fillMaxWidth(),
                    variant = NitanmalButtonVariant.Secondary
                )
            }
        }

        item {
            NitanmalButton(
                text = strings.settingsSignOut,
                onClick = onSignOutClick,
                modifier = Modifier.fillMaxWidth(),
                variant = NitanmalButtonVariant.Primary
            )
        }
    }
}

package com.nitanmal.app.presentation.ui.components.molecules

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nitanmal.app.core.localization.rememberStrings
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalButton
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalButtonVariant
import com.nitanmal.app.presentation.ui.components.atoms.NitanmalTextField

@Composable
fun LoginForm(
    name: String,
    email: String,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSignInClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val strings = rememberStrings()

    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            NitanmalTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(strings.loginName) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            NitanmalTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text(strings.loginEmail) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            NitanmalButton(
                text = strings.loginButton,
                onClick = onSignInClick,
                modifier = Modifier.fillMaxWidth(),
                variant = NitanmalButtonVariant.Primary,
                isLoading = isLoading,
                enabled = !isLoading && name.isNotBlank() && email.isNotBlank()
            )

            Spacer(modifier = Modifier.height(16.dp))

            NitanmalButton(
                text = strings.loginGoogleButton,
                onClick = onGoogleSignInClick,
                modifier = Modifier.fillMaxWidth(),
                variant = NitanmalButtonVariant.Secondary,
                isLoading = isLoading,
                enabled = !isLoading
            )
        }
    }
}

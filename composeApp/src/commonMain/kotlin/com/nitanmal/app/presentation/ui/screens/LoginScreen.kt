package com.nitanmal.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nitanmal.app.presentation.ui.components.organisms.LoginSection
import com.nitanmal.app.presentation.viewmodel.AuthUiState

@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onGoogleSignInClick: () -> Unit,
    onClearError: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        LoginSection(
            name = "",
            email = "",
            onNameChange = {},
            onEmailChange = {},
            onSignInClick = { onGoogleSignInClick() },
            onGoogleSignInClick = onGoogleSignInClick,
            isLoading = uiState.isLoading
        )

        // Error snackbar
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = onClearError) {
                        Text("OK")
                    }
                }
            ) {
                Text(text = error)
            }
        }
    }
}

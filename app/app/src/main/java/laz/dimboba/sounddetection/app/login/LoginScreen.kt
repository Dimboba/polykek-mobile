package laz.dimboba.sounddetection.app.login

import BaseButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen (
    viewModel: LoginViewModel = viewModel(),
    onNavigateToHome: () -> Unit
) {
    val authState = viewModel.authState.collectAsState().value

    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    if(authState is AuthState.Success) {
        onNavigateToHome()
    }

    if(authState is AuthState.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.resetState() },
            title = { Text("Authentication Failed") },
            text = { Text(authState.message) },
            confirmButton = {
                TextButton(onClick = { viewModel.resetState() }) {
                    Text("OK")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Column (
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = username,
            onValueChange = { username = it }
        )
        TextField(
            value = password,
            onValueChange = { password = it},
        )
        BaseButton(
            "Log In",
            onClick = { viewModel.login(username, password) },
            enabled = authState != AuthState.Loading
        )
    }
}


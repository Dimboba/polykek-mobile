package laz.dimboba.sounddetection.app.screens.signup

import BaseButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SignupScreen(
    viewModel: SignupViewModel = viewModel(),
    onNavigateHome: () -> Unit
) {
    val state = viewModel.state.collectAsState().value

    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordRepeat by rememberSaveable { mutableStateOf("") }

    val showSupportText = state is RegisterState.PasswordsNotEqual
    val resetState = { viewModel.resetState(password, passwordRepeat) }

    if(state is RegisterState.Success) {
        onNavigateHome()
        return
    }

    if(state is RegisterState.Error) {
        AlertDialog(
            onDismissRequest = { resetState() },
            title = { Text("Sign Up Failed") },
            text = { Text(state.message) },
            confirmButton = {
                TextButton(onClick = { resetState() }) {
                    Text("OK")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Box (
        modifier = Modifier.fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Text("Sign Up",
            style = MaterialTheme.typography.displayMedium.copy(
                MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(top = 50.dp)
                .align(Alignment.TopCenter)

        )
        Column {
            TextField(
                label = { Text("Username") },
                modifier = Modifier.padding(bottom = 80.dp),
                value = username,
                onValueChange = { username = it }
            )
            TextField(
                label = { Text("Password") },
                modifier = Modifier.padding(bottom = 80.dp),
                value = password,
                visualTransformation = PasswordVisualTransformation(),
                onValueChange = {
                    password = it
                    if (password == passwordRepeat) {
                        resetState()
                    }
                },
            )
            TextField(
                label = { Text("Password repeat") },
                value = passwordRepeat,
                visualTransformation = PasswordVisualTransformation(),
                onValueChange = {
                    passwordRepeat = it
                    if (password == passwordRepeat) {
                        resetState()
                    }
                },
                supportingText = @Composable {
                    val text = if (showSupportText) "Passwords are not equal" else ""
                    Text(text, color = MaterialTheme.colorScheme.error)
                }
            )
        }
        BaseButton(
            "Sign Up",
            onClick = { viewModel.registerUser(username, password, passwordRepeat) },
            enabled = state != RegisterState.Loading,
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(bottom = 50.dp)
        )
    }
}

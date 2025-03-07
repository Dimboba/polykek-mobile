package laz.dimboba.sounddetection.app.signup

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
            onValueChange = {
                password = it
                if (password == passwordRepeat) {
                    resetState()
                }
            },
        )
        TextField(
            value = passwordRepeat,
            onValueChange = {
                passwordRepeat = it
                if (password == passwordRepeat) {
                    resetState()
                }
            },
            supportingText = @Composable {
                if (showSupportText) {
                    Text("Passwords are not equal", color = MaterialTheme.colorScheme.error)
                }
            }
        )
        BaseButton(
            "Log In",
            onClick = { viewModel.registerUser(username, password, passwordRepeat) },
            enabled = state != RegisterState.Loading
        )
    }
}

package laz.dimboba.sounddetection.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import laz.dimboba.sounddetection.app.home.HomeScreen
import laz.dimboba.sounddetection.app.login.LoginScreen
import laz.dimboba.sounddetection.app.login.LoginViewModel
import laz.dimboba.sounddetection.app.signup.SignupScreen
import laz.dimboba.sounddetection.app.signup.SignupViewModel
import laz.dimboba.sounddetection.app.ui.theme.AppTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: AppNavigator by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                App(modifier = Modifier.fillMaxSize(), viewModel)
            }
        }

        lifecycleScope.launch {
            viewModel.exitEvent.collect {
                finish()
            }
        }
    }

}

@Composable
fun App(modifier: Modifier = Modifier, viewModel: AppNavigator) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background
    ) {
        AppContent(viewModel)
    }
}


@Composable
fun AppContent(viewModel: AppNavigator) {
    var screen = viewModel.screen.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        if (viewModel.isEmpty()) {
            showExitDialog = true
            return@BackHandler
        }
        viewModel.goBack()
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Are you sure you want to exit?") },
            confirmButton = {
                TextButton(onClick = { viewModel.exit() }) {
                    Text("Leave")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Stay")
                }
            }
        )
    }

    when (screen.value) {
        Screen.Login -> LoginScreen(viewModel<LoginViewModel>(),
            onNavigateToHome = { viewModel.changeScreen(Screen.Home, true) })

        Screen.OnBoard -> OnboardScreen(
            onNavigateLogin = { viewModel.changeScreen(Screen.Login) },
            onNavigateSignup = { viewModel.changeScreen(Screen.SignUp) }
        )

        Screen.Home -> HomeScreen()
        Screen.SignUp -> SignupScreen(viewModel<SignupViewModel>(),
            onNavigateHome = { viewModel.changeScreen(Screen.Home, true) })
    }
}

enum class Screen { Login, SignUp, OnBoard, Home }

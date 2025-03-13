package laz.dimboba.sounddetection.app

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import laz.dimboba.sounddetection.app.api.AuthClient
import laz.dimboba.sounddetection.app.api.TokenState
import laz.dimboba.sounddetection.app.home.HomeScreen
import laz.dimboba.sounddetection.app.login.LoginScreen
import laz.dimboba.sounddetection.app.signup.SignupScreen
import laz.dimboba.sounddetection.app.ui.theme.AppTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var authClient: AuthClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel = AppViewModel(onExit = { this.finish() })
        setContent {
            AppTheme {
                App(modifier = Modifier.fillMaxSize(), viewModel)
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            if(authClient.validateTokenAtStartUp() == TokenState.ActiveTokens) {
                viewModel.changeScreen(Screen.Home)
            }
        }
    }
}

@Composable
fun App(modifier: Modifier = Modifier, viewModel: AppViewModel) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background
    ) {
        AppContent(viewModel)
    }
}


@Composable
fun AppContent(viewModel: AppViewModel) {
    var screen = viewModel.screen.collectAsState().value
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
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    when (screen) {
        Screen.Login -> LoginScreen(viewModel(),
            onNavigateToHome = { viewModel.changeScreen(Screen.Home, true) })

        Screen.OnBoard -> OnboardScreen(
            onNavigateLogin = { viewModel.changeScreen(Screen.Login) },
            onNavigateSignup = { viewModel.changeScreen(Screen.SignUp) }
        )

        Screen.Home -> HomeScreen()
        Screen.SignUp -> SignupScreen(viewModel(),
            onNavigateHome = { viewModel.changeScreen(Screen.Home, true) })
    }
}

@Composable
fun ExitDialog(activity: MainActivity) {

}

enum class Screen { Login, SignUp, OnBoard, Home }

@Preview
@Composable
fun AppPreview() {
    AppTheme {
        App(modifier = Modifier.fillMaxSize(), AppViewModel({}))
    }
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AppPreviewDark() {
    AppTheme {
        App(modifier = Modifier.fillMaxSize(), AppViewModel({}))
    }
}
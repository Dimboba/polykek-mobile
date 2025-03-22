package laz.dimboba.sounddetection.app.onboard

import BaseButton
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun OnboardScreen(
    onNavigateLogin: () -> Unit,
    onNavigateSignup: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            "Test",
            style = MaterialTheme.typography.displayMedium.copy(
                MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(top = 50.dp)
                .align(Alignment.TopCenter)
        )
        Icon(
            Icons.Filled.MusicNote, "",
            modifier = Modifier
                .padding(top = 50.dp)
                .size(50.dp)
                .align(Alignment.Center),
        )
        Buttons(
            modifier = Modifier.align(Alignment.BottomCenter),
            onLogin = onNavigateLogin,
            onSignup = onNavigateSignup
        )
    }
}

@Composable
fun Buttons(
    modifier: Modifier = Modifier,
    onLogin: () -> Unit,
    onSignup: () -> Unit
) {
    Column (
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("if you are new to app")
        BaseButton(
            "Sign Up",
            modifier = modifier.padding(vertical = 10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            onClick = onSignup
        )
        Text("if you already have an account")
        BaseButton(
            "Log In",
            modifier = modifier.padding(top = 10.dp, bottom = 50.dp),
            onClick = onLogin
        )
    }
}

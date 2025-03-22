package laz.dimboba.sounddetection.app.home.record

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun RecordPage(
    modifier: Modifier = Modifier,
    viewModel: RecordViewModel = viewModel()
) {
    val state = viewModel.state.collectAsState()
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(false) }

    RequestPermission(
        permission = Manifest.permission.RECORD_AUDIO,
        rationaleMessage = "Microphone access is needed to record audio.",
        onPermissionResult = { isGranted ->
            permissionGranted = isGranted
        }
    )

    Column (
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {}
        Text (
            text = "Press the button to detect a note!",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.secondary.copy(
                    alpha = 0.5f
                ),
                fontSize = 20.sp
            )
        )

        AnimatedRecordButton(state = state.value,
            onClick = { viewModel.recordAndSendAudio(context) },
            modifier = Modifier)

    }
}

@Composable
fun RequestPermission(
    permission: String,
    rationaleMessage: String = "This permission is required for the app to work properly.",
    onPermissionResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var showRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionResult(isGranted)
        if (!isGranted) {
            showRationale = true
        }
    }

    // Check permission status
    val permissionStatus = checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED

    // Request permission if not granted
    LaunchedEffect(key1 = permission) {
        if (!permissionStatus) {
            permissionLauncher.launch(permission)
        } else {
            onPermissionResult(true)
        }
    }

    // Show rationale dialog if needed
    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Permission Required") },
            text = { Text(rationaleMessage) },
            confirmButton = {
                Button(onClick = {
                    showRationale = false
                    permissionLauncher.launch(permission)
                }) {
                    Text("Try Again")
                }
            },
            dismissButton = {
                Button(onClick = { showRationale = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AnimatedRecordButton(
    state: RecordStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Different animations based on the current state
    when (state) {
        RecordStatus.Idle -> {
            // Normal button when idle
            IconButton(
                onClick = onClick,
                modifier = modifier.size(140.dp),
                enabled = true
            ) {
                Icon(
                    Icons.Filled.PlayCircleFilled,
                    contentDescription = "Record",
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        RecordStatus.Recording -> {
            // Pulsating animation while recording
            val infiniteTransition = rememberInfiniteTransition(
                label = "recording_pulse"
            )
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            val color by infiniteTransition.animateColor(
                initialValue = MaterialTheme.colorScheme.secondary,
                targetValue = MaterialTheme.colorScheme.secondaryContainer,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "color"
            )

            IconButton(
                onClick = onClick,
                modifier = modifier.size(140.dp),
                enabled = false
            ) {
                Icon(
                    Icons.Filled.PlayCircleFilled,
                    contentDescription = "Recording",
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale),
                    tint = color
                )
            }
        }

        RecordStatus.Sending -> {
            val infiniteTransition = rememberInfiniteTransition(
                label = "sending_pulse"
            )
            val color by infiniteTransition.animateColor(
                initialValue = MaterialTheme.colorScheme.tertiary,
                targetValue = MaterialTheme.colorScheme.tertiaryContainer,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "color"
            )
            IconButton(
                onClick = onClick,
                modifier = modifier.size(140.dp),
                enabled = false
            ) {
                Icon(
                    Icons.Filled.PlayCircleFilled,
                    contentDescription = "Sending",
                    modifier = Modifier
                        .size(120.dp),
                    tint = color
                )
            }
        }

        RecordStatus.RecordError -> {
            // Shaking animation for error
            val infiniteTransition = rememberInfiniteTransition(
                label = "error_shake"
            )
            val translateX by infiniteTransition.animateFloat(
                initialValue = -10f,
                targetValue = 10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(100),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "shake"
            )

            IconButton(
                onClick = onClick,
                modifier = modifier.size(140.dp),
                enabled = true
            ) {
                Icon(
                    Icons.Filled.PlayCircleFilled,
                    contentDescription = "Error",
                    modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer {
                            translationX = translateX
                        },
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        else -> {
            // Fallback for any other states
            IconButton(
                onClick = onClick,
                modifier = modifier.size(140.dp),
                enabled = state != RecordStatus.Recording && state != RecordStatus.Sending
            ) {
                Icon(
                    Icons.Filled.PlayCircleFilled,
                    contentDescription = "Record",
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
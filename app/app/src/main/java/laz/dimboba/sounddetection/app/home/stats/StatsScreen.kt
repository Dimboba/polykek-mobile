package laz.dimboba.sounddetection.app.home.stats

import BaseButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatsPage(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = viewModel()
) {
    val cardPaddingHorizontal = 8.dp
    val cardPaddingVertical = 8.dp
    val cardTextContainerPadding = 8.dp
    val state = viewModel.state.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditUserDialog by remember { mutableStateOf(false) }

    val headersStyle = MaterialTheme.typography.titleMedium
    val valueStyle = MaterialTheme.typography.titleLarge.copy(
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
    val scope = rememberCoroutineScope()

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxHeight()
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            state.value.map.forEach { (k, v) ->
                val keyWidth = measureTextWidth(k, textStyle = headersStyle)
                val valueWidth = measureTextWidth(v, textStyle = valueStyle)
                val maxTextWidth = maxOf(keyWidth, valueWidth)
                val minWidth =
                    maxTextWidth + cardPaddingHorizontal * 2 + cardTextContainerPadding * 2
                val cardModifier = Modifier
                    .weight(minWidth.value)
                    .widthIn(min = minWidth)
                    .padding(vertical = cardPaddingVertical, horizontal = cardPaddingHorizontal)
                    .fillMaxRowHeight()
                StatsCard(
                    key = k,
                    value = v,
                    modifier = cardModifier,
                    cardTextContainerPadding = cardTextContainerPadding,
                    keyStyle = headersStyle,
                    valueStyle = valueStyle
                )
            }
        }
        Buttons(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onLogoutClick = { showLogoutDialog = true },
            onSettingsClick = { showEditUserDialog = true }
        )
    }

    if (showLogoutDialog) {
        LogoutAlertDialog(
            onConfirmClick = { viewModel.logOut() },
            onDismissClick = { showLogoutDialog = false }
        )
    }

    if (showEditUserDialog) {
        EditUserAlertDialog({ showEditUserDialog = false },
            { username, password ->
                viewModel.updateUser(username, password)
                showEditUserDialog = false
            }
        )
    }
}

@Composable
fun EditUserAlertDialog(
    onDismissClick: () -> Unit,
    onConfirmClick: (username: String, password: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismissClick
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {

            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                ) {
                    Text(
                        text = "Account Details",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    IconButton(
                        onClick = onDismissClick,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close Dialog"
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .padding(bottom = 20.dp)
                ) {
                    TextField(
                        label = { Text("Username") },
                        value = username,
                        modifier = Modifier.padding(bottom = 20.dp),
                        onValueChange = { username = it },
                        supportingText = @Composable {
                            val text = if (username == "") "Username will not be changed" else ""
                            Text(text, color = MaterialTheme.colorScheme.secondary)
                        }
                    )
                    TextField(
                        label = { Text("Password") },
                        value = password,
                        visualTransformation = PasswordVisualTransformation(),
                        onValueChange = { password = it },
                        supportingText = @Composable {
                            val text = if (password == "") "Passwords will not be changed" else ""
                            Text(text, color = MaterialTheme.colorScheme.secondary)
                        }
                    )

                }


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismissClick,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { onConfirmClick(username, password) },
                        enabled = true
                    ) {
                        Text("Update")
                    }
                }
            }
        }
    }
}

@Composable
fun LogoutAlertDialog(
    onDismissClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismissClick() },
        title = { Text("Are you sure you want to log out?") },
        confirmButton = {
            TextButton(onClick = { onConfirmClick() }) {
                Text("Log Out")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        dismissButton = {
            TextButton(onClick = { onDismissClick() }) {
                Text("Stay")
            }
        }
    )
}

@Composable
fun Buttons(
    modifier: Modifier = Modifier,
    onLogoutClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BaseButton(
            text = "Settings",
            onClick = onSettingsClick
        )
        BaseButton(
            text = "Log Out",
            onClick = onLogoutClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            modifier = Modifier.padding(bottom = 50.dp, top = 20.dp)
        )
    }
}

@Composable
fun StatsCard(
    modifier: Modifier = Modifier,
    cardTextContainerPadding: Dp,
    key: String,
    value: String,
    keyStyle: TextStyle,
    valueStyle: TextStyle
) {
    Card(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .padding(cardTextContainerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                key,
                maxLines = 1,
                style = keyStyle,
                modifier = Modifier.align(Alignment.Start),
            )
            Text(
                value,
                overflow = TextOverflow.Ellipsis,
                style = valueStyle.copy(
                    textAlign = TextAlign.End
                ),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun measureTextWidth(text: String, textStyle: TextStyle = TextStyle.Default): Dp {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val textLayoutResult = textMeasurer.measure(
        text = AnnotatedString(text),
        style = textStyle
    )

    return with(density) { textLayoutResult.size.width.toDp() }
}

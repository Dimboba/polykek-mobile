package laz.dimboba.sounddetection.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun RecordPage(
    modifier: Modifier = Modifier,
    viewModel: RecordViewModel = viewModel()
) {
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
        IconButton(
            onClick = {},
            modifier = Modifier.size(140.dp)
        ) {
            Icon(
                Icons.Filled.PlayCircleFilled, "Record",
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary)
        }

    }
}

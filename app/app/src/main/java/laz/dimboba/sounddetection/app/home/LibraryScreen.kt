package laz.dimboba.sounddetection.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import laz.dimboba.sounddetection.app.FileState
import laz.dimboba.sounddetection.app.Record
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Composable
fun LibraryPage(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = viewModel()
) {
    val state = viewModel.libraryState.collectAsState()
    val records = viewModel.recordsState

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn {
            items(records) {
                val isNowPlayed = it.id == state.value.currentPlayingId
                RecordCard(
                    modifier = Modifier.padding(10.dp),
                    record = it,
                    viewModel = viewModel,
                    isNowPlayed = isNowPlayed,
                    isPaused = state.value.isPaused && isNowPlayed
                )
            }
        }
    }
}

@Composable
fun RecordCard(
    modifier: Modifier = Modifier,
    record: Record,
    viewModel: LibraryViewModel,
    isNowPlayed: Boolean,
    isPaused: Boolean
) {
    Card(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    DateTimeFormatter
                        .ofPattern("yyyy LLL dd")
                        .withZone(ZoneId.systemDefault())
                        .format(record.createdAt)
                )
                Text(record.note)
            }
            RecordCardButton(viewModel, record, isNowPlayed, isPaused)
        }
    }
}

@Composable
fun RecordCardButton(
    viewModel: LibraryViewModel,
    record: Record,
    isNowPlayed: Boolean,
    isPaused: Boolean
) {
    if (record.fileState == FileState.LOADING) {
        LoadingIcon()
        return
    }
    if (record.fileState == FileState.NOT_LOADED) {
        LoadButton(viewModel, record)
        return
    }
    if (record.fileState == FileState.ERROR) {
        ReloadButton(viewModel, record)
        return
    }
    if (isNowPlayed && !isPaused) {
        PauseButton(viewModel, record)
    }
    PlayButton(viewModel, record)
}

@Composable
fun ReloadButton(viewModel: LibraryViewModel, record: Record) {
    IconButton(
        onClick = { viewModel.loadSound(record) }
    ) {
        Icon(
            Icons.Filled.Add, "Stop sound",
            modifier = Modifier.rotate(45f)
        )
    }
}

@Composable
fun LoadingIcon() {
    Icon(Icons.Filled.Pending, "Loading sound")
}

@Composable
fun PauseButton(viewModel: LibraryViewModel, record: Record) {
    IconButton(
        onClick = { viewModel.stopSound(record) }
    ) {
        Icon(Icons.Filled.FileDownload, "Stop sound")
    }
}

@Composable
fun LoadButton(viewModel: LibraryViewModel, record: Record) {
    IconButton(
        onClick = { viewModel.playSound(record) }
    ) {
        Icon(Icons.Filled.FileDownload, "Upload sound")
    }
}

@Composable
fun PlayButton(viewModel: LibraryViewModel, record: Record) {
    IconButton(
        onClick = { viewModel.loadSound(record) }
    ) {
        Icon(Icons.Filled.Pause, "Pause sound")
    }
}

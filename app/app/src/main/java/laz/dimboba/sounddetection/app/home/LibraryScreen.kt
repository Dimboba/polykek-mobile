package laz.dimboba.sounddetection.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import laz.dimboba.sounddetection.app.FileState
import laz.dimboba.sounddetection.app.Record
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryPage(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = viewModel()
) {
    val state = viewModel.libraryState.collectAsState()
    val records = viewModel.records
    val hasMoreData = state.value.hasMoreData
    val isLoading = state.value.isLoading
    val isRefreshing = state.value.isRefreshing
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo }
            .collect { items ->
                val last = items.lastOrNull()
                if (hasMoreData && last != null && last.index >= records.size - 5) {
                    viewModel.loadMoreData()
                }
            }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                viewModel.reloadData()
            }
        },
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            state = lazyListState
        ) {
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

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            } else if (!hasMoreData) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "All records have been loaded",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
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
                .height(80.dp)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    DateTimeFormatter
                        .ofPattern("yyyy LLL dd  H:mm:ss")
                        .withZone(ZoneId.systemDefault())
                        .format(record.createdAt),
                    style = MaterialTheme.typography.titleMedium
                )
                Row(Modifier.fillMaxHeight()) {
                    Text(
                        record.note ?: "Undefined",
                        modifier = Modifier.align(Alignment.Top),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    if(record.octave != null) {
                        Text(
                            record.octave.toString(),
                            modifier = Modifier.align(Alignment.Bottom),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
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
    } else {
        PlayButton(viewModel, record)
    }
}

@Composable
fun ReloadButton(viewModel: LibraryViewModel, record: Record) {
    val context =  LocalContext.current
    IconButton(
        onClick = { viewModel.loadSound(record, context) }
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
        onClick = { viewModel.pauseSound(record) }
    ) {
        Icon(Icons.Filled.Pause, "Stop sound")
    }
}

@Composable
fun LoadButton(viewModel: LibraryViewModel, record: Record) {
    val context = LocalContext.current
    IconButton(
        onClick = { viewModel.loadSound(record, context) }
    ) {
        Icon(Icons.Filled.FileDownload, "Upload sound")
    }
}

@Composable
fun PlayButton(viewModel: LibraryViewModel, record: Record) {
    val context = LocalContext.current
    IconButton(
        onClick = { viewModel.playSound(record, context) }
    ) {
        Icon(Icons.Filled.PlayArrow, "Pause sound")
    }
}


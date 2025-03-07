package laz.dimboba.sounddetection.app.home

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import laz.dimboba.sounddetection.app.FileState
import laz.dimboba.sounddetection.app.Record
import laz.dimboba.sounddetection.app.ui.theme.AppTheme
import java.time.format.DateTimeFormatter

val pages = listOf<@Composable (modifier: Modifier?) -> Unit>(
    { SettingsPage() },
    { RecordPage() },
    { LibraryPage() }
)


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen () {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.secondary,
                    scrolledContainerColor = MaterialTheme.colorScheme.secondary,
                    navigationIconContentColor = MaterialTheme.colorScheme.secondary
                ),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors()
                    ) {
                        Icon(Icons.Filled.AccountBox, "Account")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.pageCount - 1)
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors()
                    ) {
                        Icon(Icons.Filled.LibraryMusic, "History")
                    }
                },
                title = {
                    Row(
                        Modifier
                            .wrapContentHeight()
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        repeat(pagerState.pageCount) { iteration ->
                            val color =
                                if (pagerState.currentPage == iteration)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onPrimary
                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .size(16.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        HorizontalPager(state = pagerState, modifier = Modifier.padding(padding)) { pageIndex ->
            val page = pages[pageIndex]
            page.invoke(Modifier)
        }
    }
}

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
            Icon(Icons.Filled.PlayCircleFilled, "Record",
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary)
        }

    }
}

@Composable
fun SettingsPage(
    modifier: Modifier = Modifier
) {
    Column (
        modifier = modifier.fillMaxSize(),
    ) {

    }
}

@Composable
fun LibraryPage(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = viewModel()
) {
    val state = viewModel.libraryState.collectAsState()
    val records = viewModel.recordsState

    Column (
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
    Card (
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
                Text(record.recordedAt.format(DateTimeFormatter.ofPattern("yyyy LLL d")))
                Text(record.note)
            }
            RecordCardButton(viewModel, record, isNowPlayed, isPaused)
        }
    }
}
@Composable
fun RecordCardButton(viewModel: LibraryViewModel, record: Record, isNowPlayed: Boolean, isPaused: Boolean) {
    if(record.fileState == FileState.LOADING) {
        LoadingIcon()
        return
    }
    if(record.fileState == FileState.NOT_LOADED) {
        LoadButton(viewModel, record)
        return
    }
    if(record.fileState == FileState.ERROR) {
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
        Icon(Icons.Filled.Add, "Stop sound",
            modifier = Modifier.rotate(45f))
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


@Composable
@Preview
fun Preview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen()
        }
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
fun PreviewDark() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen()
        }
    }
}
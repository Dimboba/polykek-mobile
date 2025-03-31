package laz.dimboba.sounddetection.app.screens.home

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import laz.dimboba.sounddetection.app.screens.home.library.LibraryPage
import laz.dimboba.sounddetection.app.screens.home.record.RecordPage
import laz.dimboba.sounddetection.app.screens.home.stats.StatsPage
import laz.dimboba.sounddetection.app.screens.home.stats.StatsViewModel
import laz.dimboba.sounddetection.app.screens.home.stats.UserSettingsState
import laz.dimboba.sounddetection.app.ui.theme.AppTheme

val pages = listOf<@Composable (modifier: Modifier?) -> Unit>(
    { StatsPage() },
    { RecordPage() },
    { LibraryPage() }
)


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen (
    viewModel: StatsViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })
    val snackbarHostState = remember { SnackbarHostState() }
    val settingsState = viewModel.settingsState.collectAsState()

    LaunchedEffect(settingsState.value) {
        val state = settingsState.value
        if (state == UserSettingsState.Success) {
            snackbarHostState.showSnackbar("Successfully updated user")
        } else if(state is UserSettingsState.Error) {
            snackbarHostState.showSnackbar(
                "Could not update user, because: ${state.message}"
            )
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                snackbarHostState
            )
        },
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
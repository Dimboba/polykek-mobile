package laz.dimboba.sounddetection.app.home

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import laz.dimboba.sounddetection.app.FileState
import laz.dimboba.sounddetection.app.Record
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: RecordRepository
) : ViewModel() {

    private val _libraryState = MutableStateFlow(LibraryState())
    val libraryState: StateFlow<LibraryState> = _libraryState
    val records = repository.recordsState

    var mediaPlayer: MediaPlayer? = null

    private final val pageSize = 20;

    init {
        viewModelScope.launch {
            repository.state.collect { repoState ->
                _libraryState.value = libraryState.value.copy(
                    isLoading = repoState.isLoading,
                    hasMoreData = repoState.hasMoreData,
                    errorMessage = repoState.errorMessage
                )
            }
        }

        //todo: FUNNY BUG after log out by timeout does not reload init data
        loadMoreData()
    }

    fun reloadData() {
        if (_libraryState.value.isLoading || _libraryState.value.isRefreshing) return

        _libraryState.value = _libraryState.value.copy(isRefreshing = true)
        viewModelScope.launch {
            repository.clearRecords()
            repository.loadRecords(Instant.now(), pageSize)
            _libraryState.value = _libraryState.value.copy(isRefreshing = false)
        }
    }

    fun loadMoreData() {
        if (_libraryState.value.isLoading || !_libraryState.value.hasMoreData) return

        viewModelScope.launch {
            val lastRecord = records.lastOrNull()
            val afterInstant = lastRecord?.createdAt ?: Instant.now()
            repository.loadRecords(before = afterInstant, limit = pageSize)
        }
    }


    fun loadSound(record: Record, context: Context) {
        if(record.fileState == FileState.LOADING || record.fileState == FileState.LOADED) return
        viewModelScope.launch {
            repository.downloadRecordSound(record, context)
        }
    }

    fun playSound(record: Record, context: Context) {
        if(_libraryState.value.currentPlayingId != null) {
            if(continueSound(record)) return
        }
        _libraryState.value = _libraryState.value.copy(
            isPaused = false,
            currentPlayingId = record.id
        )
        mediaPlayer?.apply {
            this.stop()
            this.reset()
            this.release()
        }
        mediaPlayer = initMediaPlayer(context)
            .apply {
                setDataSource(record.localFilePath)
                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                    _libraryState.value = _libraryState.value.copy(
                        isPaused = false,
                        currentPlayingId = null
                    )
                }
                prepare()
                start()
            }
    }

    private fun continueSound(record: Record): Boolean {
        if(_libraryState.value.currentPlayingId != record.id) return false
        if(!_libraryState.value.isPaused) return false
        mediaPlayer?.apply {
            _libraryState.value = _libraryState.value.copy(
                isPaused = false
            )
            this.start()
        }
        return true
    }

    fun pauseSound(record: Record) {
        if(_libraryState.value.currentPlayingId != record.id) return
        if(_libraryState.value.isPaused) return
        mediaPlayer?.apply {
            this.pause()
            _libraryState.value = _libraryState.value.copy(isPaused = true)
        }
    }

    private fun initMediaPlayer(context: Context): MediaPlayer {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            return MediaPlayer(context)
        return MediaPlayer()
    }
}

data class LibraryState(
    val hasMoreData: Boolean = true,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val currentPlayingId: Long? = null,
    val isPaused: Boolean = false
) {}
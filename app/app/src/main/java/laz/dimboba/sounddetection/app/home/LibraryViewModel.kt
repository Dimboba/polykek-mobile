package laz.dimboba.sounddetection.app.home

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import laz.dimboba.sounddetection.app.Record
import java.time.LocalDateTime

class LibraryViewModel: ViewModel() {
    private val _recordsState = mutableStateListOf<Record>()
    val recordsState: List<Record> = _recordsState

    private val _libraryState = MutableStateFlow(LibraryState())
    val libraryState: StateFlow<LibraryState> = _libraryState

    init {
        loadMoreData()
    }

    fun loadMoreData() {
        val currState = _libraryState.value
        if(currState.isLoading) return

        _libraryState.value = _libraryState.value.copy(
            isLoading = true
        )
        viewModelScope.launch {
            //todo: api call here
            var records = mockLoadMoreData()
            val currentPage = currState.currentPage + 1
            if(currentPage > 3) {
                records = emptyList()
            }
            if (records.isEmpty()) {
                _libraryState.value = currState.copy(
                    isLoading = false,
                    hasMoreData = false
                )
            } else {
               _recordsState.addAll(records)
            }
        }
    }

    fun loadSound(record: Record) {
        viewModelScope.launch {
            //todo: load audio, add states for this, think about implementation
        }
    }

    fun playSound(record: Record) {
        //todo: to be done
    }

    fun stopSound(record: Record) {
        //todo: to be done
    }
}

suspend fun mockLoadMoreData(): List<Record>{
    delay(1000)
    return 1.rangeTo(10)
        .map {
            laz.dimboba.sounddetection.app.Record(
                it.toLong(),
                it.toLong(),
                "D",
                LocalDateTime.now().minusHours(it.toLong()),
                null
            )
        }.toList()
}

data class LibraryState (
    val isLoading: Boolean = false,
    val hasMoreData: Boolean = true,
    val currentPage: Int = 0,
    val errorMessage: String? = null,
    val currentPlayingId: Long? = null,
    val isPaused: Boolean = false
) {}
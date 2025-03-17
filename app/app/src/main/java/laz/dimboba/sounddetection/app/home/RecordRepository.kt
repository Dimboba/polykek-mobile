package laz.dimboba.sounddetection.app.home

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import laz.dimboba.sounddetection.app.FileState
import laz.dimboba.sounddetection.app.Record
import laz.dimboba.sounddetection.app.api.SoundClient
import java.io.File
import java.nio.file.Files
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordRepository @Inject constructor(
    private val client: SoundClient
) {

    private val _recordsState = mutableStateListOf<Record>()
    val recordsState: List<Record> = _recordsState

    private val _state = MutableStateFlow(RepositoryState())
    val state: StateFlow<RepositoryState> = _state

    suspend fun clearRecords() = withContext(Dispatchers.IO) {
        _recordsState.clear()
        _state.value = _state.value.copy(hasMoreData = true)
    }

    suspend fun loadRecords(before: Instant, limit: Int): Unit = withContext(Dispatchers.IO) {
        if(_state.value.isLoading) return@withContext
        _state.value = _state.value.copy(
            isLoading = true,
            errorMessage = null
        )

        client.getRecordsPage(before, limit)
            .onFailure {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = it.message
                )
            }
            .onSuccess {
                _recordsState.addAll(it)
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = null,
                    hasMoreData = it.size >= limit
                )
            }
    }

    suspend fun addRecord(file: File): Result<Record> = withContext(Dispatchers.IO) {
        if(_state.value.isSending)
            return@withContext Result.failure(RuntimeException("We already creating a record"))
        _state.value = _state.value.copy(
            isSending = true
        )
        val result = client.postSound(file)
            .onSuccess { newRecord ->
                val indexOfFirstLater = _recordsState.indexOfFirst { it.createdAt.isBefore(newRecord.createdAt) }
                if(indexOfFirstLater == -1) {
                    _recordsState.add(newRecord)
                    return@onSuccess
                }
                _recordsState.add(indexOfFirstLater, newRecord)
            }
        _state.value = _state.value.copy(
            isSending = false
        )
        return@withContext result
    }

    suspend fun downloadRecordSound(record: Record, context: Context): Unit = withContext(Dispatchers.IO) {
        changeRecord(record.copy(
            fileState = FileState.LOADING
        ))
        client.downloadFile(record.id)
            .onSuccess { stream ->
                val name = UUID.randomUUID().toString()
                val file = File(context.cacheDir, "$name.mp4")
                stream.use {
                    Files.copy(it,file.toPath())
                }
                val updatedRecord = record.copy(
                    fileState = FileState.LOADED,
                    localFilePath = file.absolutePath
                )
                changeRecord(updatedRecord)
            }
            .onFailure {
                changeRecord(
                    record.copy(
                        fileState = FileState.ERROR
                    )
                )
            }
        println()
    }

    private fun changeRecord(updatedRecord: Record) {
        val indexOfRecord = _recordsState.indexOfFirst { it.id == updatedRecord.id }
        if(indexOfRecord == -1) return
        _recordsState[indexOfRecord] = updatedRecord
    }
}

data class RepositoryState(
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val hasMoreData: Boolean = true,
    val errorMessage: String? = null
)
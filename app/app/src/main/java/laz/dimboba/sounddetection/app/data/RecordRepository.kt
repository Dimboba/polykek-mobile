package laz.dimboba.sounddetection.app.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import laz.dimboba.sounddetection.app.api.SoundClient
import laz.dimboba.sounddetection.app.dto.FileState
import laz.dimboba.sounddetection.app.dto.Record
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

    suspend fun clearRecords() = withContext(Dispatchers.IO) {
        _recordsState.clear()
    }

    suspend fun loadRecords(
        before: Instant = Instant.now(),
        limit: Int = 10
    ): Result<List<Record>> = withContext(Dispatchers.IO) {
        client.getRecordsPage(before, limit)
            .onSuccess {
                _recordsState.addAll(it)
            }
    }

    suspend fun addRecord(file: File): Result<Record> = withContext(Dispatchers.IO) {
        val result = client.postSound(file)
            .onSuccess { newRecord ->
                val indexOfFirstLater =
                    _recordsState.indexOfFirst { it.createdAt.isBefore(newRecord.createdAt) }
                if (indexOfFirstLater == -1) {
                    _recordsState.add(newRecord)
                    return@onSuccess
                }
                _recordsState.add(indexOfFirstLater, newRecord)
            }
        return@withContext result
    }

    suspend fun downloadRecordSound(record: Record, context: Context): Unit =
        withContext(Dispatchers.IO) {
            changeRecord(
                record.copy(
                    fileState = FileState.LOADING
                )
            )
            client.downloadFile(record.id)
                .onSuccess { stream ->
                    val name = UUID.randomUUID().toString()
                    val file = File(context.cacheDir, "$name.mp4")
                    stream.use {
                        Files.copy(it, file.toPath())
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
        if (indexOfRecord == -1) return
        _recordsState[indexOfRecord] = updatedRecord
    }
}

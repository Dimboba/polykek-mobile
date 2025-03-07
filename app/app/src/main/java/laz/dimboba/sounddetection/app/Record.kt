package laz.dimboba.sounddetection.app

import java.time.LocalDateTime

data class Record (
    val id: Long,
    val soundId: Long,
    val note: String,
    val recordedAt: LocalDateTime,
    val localFilePath: String?,
    val fileState: FileState = FileState.NOT_LOADED
)

enum class FileState {
    NOT_LOADED,
    ERROR,
    LOADING,
    LOADED
}
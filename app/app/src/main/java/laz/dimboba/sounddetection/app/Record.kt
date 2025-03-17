@file:UseSerializers(InstantSerializer::class)

package laz.dimboba.sounddetection.app

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.Instant

@Serializable
data class Record (
    val id: Long,
    val note: String?,
    val octave: Int?,
    val createdAt: Instant,
    val localFilePath: String? = null,
    val fileState: FileState = FileState.NOT_LOADED
)

enum class FileState {
    NOT_LOADED,
    ERROR,
    LOADING,
    LOADED
}
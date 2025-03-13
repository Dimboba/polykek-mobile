package laz.dimboba.sounddetection.mobileserver.record

import java.time.Instant

data class RecordDto(
    val id: Long,
    val fileName: String,
    val note: String,
    val createdAt: Instant
)
package laz.dimboba.sounddetection.mobileserver.record

import java.time.Instant

data class RecordDto(
    val id: Long,
    val note: String,
    val octave: Int?,
    val createdAt: Instant
)
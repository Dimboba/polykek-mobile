package laz.dimboba.sounddetection.mobileserver.user

import laz.dimboba.sounddetection.mobileserver.record.RecordDto
import java.time.Instant

data class UserDto(
    val id: Long,
    val username: String,
    val createdAt: Instant,
)

data class UserWithRecordsDto(
    val id: Long,
    val username: String,
    val createdAt: Instant,
    val records: List<RecordDto>,
)
package laz.dimboba.sounddetection.mobileserver.stats

import laz.dimboba.sounddetection.mobileserver.record.NoteCountDto
import laz.dimboba.sounddetection.mobileserver.record.OctaveCountDto
import java.time.Instant

data class StatsDto(
    val username: String,
    val since: Instant,
    val recordCount: Long,
    val popularNotes: NoteCountDto,
    val lastRecordTime: Instant?,
    val popularOctaves: OctaveCountDto,
)

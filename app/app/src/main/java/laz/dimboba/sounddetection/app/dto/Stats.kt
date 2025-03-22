@file:UseSerializers(InstantSerializer::class)

package laz.dimboba.sounddetection.app.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.Instant

@Serializable
data class Stats(
    val username: String,
    val since: Instant,
    val recordCount: Long,
    val popularNotes: NoteCount,
    val lastRecordTime: Instant?,
    val popularOctaves: OctaveCount,
)

@Serializable
data class NoteCount(
    val notes: List<String>,
    val count: Long
)

@Serializable
data class OctaveCount(
    val octaves: List<Int>,
    val count: Long
)

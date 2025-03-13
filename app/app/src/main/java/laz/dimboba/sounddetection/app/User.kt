@file:UseSerializers(InstantSerializer::class)

package laz.dimboba.sounddetection.app

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.Instant


@Serializable
data class User(
    val id: Long,
    val username: String,
    val createdAt: Instant,
)

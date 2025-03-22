package laz.dimboba.sounddetection.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRequest(
    val username: String?,
    val password: String?
)
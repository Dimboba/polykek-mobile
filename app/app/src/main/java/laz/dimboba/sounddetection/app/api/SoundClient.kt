package laz.dimboba.sounddetection.app.api

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundClient @Inject constructor(
    private val authClient: AuthClient,
    private val tokenManager: TokenManager
): BaseClient() {
    suspend fun postSound() {

    }
}
package laz.dimboba.sounddetection.app.api

import laz.dimboba.sounddetection.app.data.TokenManager
import laz.dimboba.sounddetection.app.dto.Stats
import laz.dimboba.sounddetection.app.dto.UpdateUserRequest
import laz.dimboba.sounddetection.app.dto.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsClient @Inject constructor(
    private val authClient: AuthClient,
    private val tokenManager: TokenManager
): BaseClient() {
    suspend fun getStats(): Result<Stats> {
        return authClient.sendRequestWithRetry {
            getJsonRequest(
                "/api/stats",
                emptyMap(),
                Stats.serializer(),
                tokenManager.getAccessToken()
            )
        }
    }

    suspend fun updateUser(username: String?, password: String?): Result<User> {
        return authClient.sendRequestWithRetry {
            putJsonRequest(
                endpoint = "/api/user",
                requestBody = UpdateUserRequest(username, password),
                serializer = UpdateUserRequest.serializer(),
                responseSerializer = User.serializer(),
                tokenManager.getAccessToken()
            )
        }
    }
}
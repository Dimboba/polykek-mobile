package laz.dimboba.sounddetection.app.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import laz.dimboba.sounddetection.app.Record
import okhttp3.MediaType.Companion.toMediaType
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundClient @Inject constructor(
    private val authClient: AuthClient,
    private val tokenManager: TokenManager
): BaseClient() {

    suspend fun postSound(file: File): Result<Record> {
        val mediaType = when (file.extension.lowercase()) {
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "m4a" -> "audio/mp4"
            "mp4" -> "audio/mp4"
            else -> "audio/mp4"
        }.toMediaType()

        return sendRequestWithRetry {
            postMultipart(
                "/api/record",
                file,
                mediaType,
                Record.serializer(),
                tokenManager.getAccessToken()
            )
        }
    }

    private suspend fun <T> sendRequestWithRetry(requestFunc: suspend () -> Result<T>): Result<T>
        = withContext(Dispatchers.IO) {
            requestFunc()
                .onSuccess { return@withContext Result.success(it) }
                .onFailure { error ->
                    if(error is ApiException && error.code == 401 ) {
                        authClient.refresh(tokenManager.getRefreshToken())
                            .onFailure {
                                return@withContext Result.failure(it)
                            }
                            .onSuccess {
                                return@withContext requestFunc()
                            }
                    } else {
                        return@withContext Result.failure(error)
                    }
                }
    }
}
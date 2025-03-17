package laz.dimboba.sounddetection.app.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import laz.dimboba.sounddetection.app.Record
import okhttp3.MediaType.Companion.toMediaType
import java.io.File
import java.io.InputStream
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundClient @Inject constructor(
    private val authClient: AuthClient,
    private val tokenManager: TokenManager
) : BaseClient() {
    private final val mediaType = "audio/mp4".toMediaType()

    suspend fun postSound(file: File): Result<Record> {
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

    suspend fun getRecordsPage(before: Instant, limit: Int): Result<List<Record>> {
        return sendRequestWithRetry {
            getJsonRequest(
                "/api/record",
                mapOf(
                    "before" to before,
                    "limit" to limit
                ),
                ListSerializer(Record.serializer()),
                tokenManager.getAccessToken()
            )
        }
    }

    suspend fun downloadFile(recordId: Long): Result<InputStream> {
        return sendRequestWithRetry {
            getFileAsStream("/api/record/$recordId/download", tokenManager.getAccessToken())
        }
    }

    private suspend fun <T> sendRequestWithRetry(requestFunc: suspend () -> Result<T>): Result<T> =
        withContext(Dispatchers.IO) {
            requestFunc()
                .onSuccess { return@withContext Result.success(it) }
                .onFailure { error ->
                    if (error is ApiException && error.code == 403) {
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
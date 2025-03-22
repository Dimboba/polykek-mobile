package laz.dimboba.sounddetection.app.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import laz.dimboba.sounddetection.app.data.TokenManager
import laz.dimboba.sounddetection.app.dto.Record
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
        return authClient.sendRequestWithRetry {
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
        return authClient.sendRequestWithRetry {
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
        return authClient.sendRequestWithRetry {
            getFileAsStream("/api/record/$recordId/download", tokenManager.getAccessToken())
        }
    }
}
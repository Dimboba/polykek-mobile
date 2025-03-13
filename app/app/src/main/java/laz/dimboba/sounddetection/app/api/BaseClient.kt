package laz.dimboba.sounddetection.app.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

open class BaseClient {
    protected val apiBaseUrl = "http://10.0.2.2:8080"
    protected val json = Json { ignoreUnknownKeys = true }
    protected val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    protected val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    protected suspend inline fun <reified T, reified R> postRequest(
        endpoint: String,
        requestBody: T,
        serializer: kotlinx.serialization.KSerializer<T>,
        responseSerializer: kotlinx.serialization.KSerializer<R>,
        authToken: String? = null
    ): Result<R> = withContext(Dispatchers.IO) {
        try {
            val jsonString = json.encodeToString(serializer, requestBody)
            val requestBuilder = Request.Builder()
                .url("$apiBaseUrl$endpoint")
                .post(jsonString.toRequestBody(jsonMediaType))
                .header("Content-Type", "application/json")

            if(authToken != null) {
                requestBuilder.header("Authorization", "Bearer $authToken")
            }
            val request = requestBuilder.build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Request failed with code: ${response.code}"))
                }

                val responseBody = response.body?.string()
                    ?: return@withContext Result.failure(IOException("Empty response"))

                // Deserialize the response
                val parsedResponse = json.decodeFromString(responseSerializer, responseBody)
                Result.success(parsedResponse)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
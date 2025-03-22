package laz.dimboba.sounddetection.app.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.io.InputStream
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

    protected suspend inline fun getFileAsStream(
        endpoint: String,
        authToken: String? = null
    ): Result<InputStream> = withContext(Dispatchers.IO) {
        val requestBuilder = Request.Builder()
            .url("$apiBaseUrl$endpoint")
        if (authToken != null) {
            requestBuilder.header("Authorization", "Bearer $authToken")
        }
        val request = requestBuilder.build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            return@withContext Result.failure(
                ApiException(
                    response.code,
                    "Request failed with code: ${response.code}"
                )
            )
        }
        val responseBody = response.body?.byteStream()
            ?: return@withContext Result.failure(
                ApiException(
                    response.code,
                    "Empty response"
                )
            )
        return@withContext Result.success(responseBody)

    }

    protected suspend inline fun <reified T> getJsonRequest(
        endpoint: String,
        searchParams: Map<String, Any>,
        responseSerializer: KSerializer<T>,
        authToken: String? = null
    ): Result<T> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = "$apiBaseUrl$endpoint".toHttpUrl().newBuilder()
            searchParams.forEach { (key, value) ->
                urlBuilder.addQueryParameter(key, value.toString())
            }

            val requestBuilder = Request.Builder()
                .url(urlBuilder.build())
                .get()
                .header("Content-Type", "application/json")

            if (authToken != null) {
                requestBuilder.header("Authorization", "Bearer $authToken")
            }
            val request = requestBuilder.build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        ApiException(
                            response.code,
                            "Request failed with code: ${response.code}"
                        )
                    )
                }

                val responseBody = response.body?.string()
                    ?: return@withContext Result.failure(
                        ApiException(
                            response.code,
                            "Empty response"
                        )
                    )

                val parsedResponse = json.decodeFromString(responseSerializer, responseBody)
                Result.success(parsedResponse)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    protected suspend inline fun <reified T, reified R> putJsonRequest(
        endpoint: String,
        requestBody: T,
        serializer: KSerializer<T>,
        responseSerializer: KSerializer<R>,
        authToken: String? = null
    ): Result<R>  = sendJsonRequest(
    JsonSendMethod.Put,
    endpoint, requestBody, serializer, responseSerializer, authToken
    )

    protected suspend inline fun <reified T, reified R> postJsonRequest(
        endpoint: String,
        requestBody: T,
        serializer: KSerializer<T>,
        responseSerializer: KSerializer<R>,
        authToken: String? = null
    ): Result<R> = sendJsonRequest(
        JsonSendMethod.Post,
        endpoint, requestBody, serializer, responseSerializer, authToken
    )

    protected suspend inline fun <reified T, reified R> sendJsonRequest(
        method: JsonSendMethod,
        endpoint: String,
        requestBody: T,
        serializer: KSerializer<T>,
        responseSerializer: KSerializer<R>,
        authToken: String? = null
    ): Result<R> = withContext(Dispatchers.IO) {
        try {
            val methodFunc = method.func
            val jsonString = json.encodeToString(serializer, requestBody)
            val requestBuilder = Request.Builder()
                .url("$apiBaseUrl$endpoint")
                .methodFunc(jsonString.toRequestBody(jsonMediaType))
                .header("Content-Type", "application/json")

            if (authToken != null) {
                requestBuilder.header("Authorization", "Bearer $authToken")
            }
            val request = requestBuilder.build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        ApiException(
                            response.code,
                            "Request failed with code: ${response.code}"
                        )
                    )
                }

                val responseBody = response.body?.string()
                    ?: return@withContext Result.failure(
                        ApiException(
                            response.code,
                            "Empty response"
                        )
                    )

                // Deserialize the response
                val parsedResponse = json.decodeFromString(responseSerializer, responseBody)
                Result.success(parsedResponse)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    protected suspend inline fun <reified R> postMultipart(
        endpoint: String,
        file: File,
        mediaType: MediaType,
        responseSerializer: KSerializer<R>,
        authToken: String? = null
    ): Result<R> = withContext(Dispatchers.IO) {
        try {
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file", file.getName(),
                    file.readBytes().toRequestBody(mediaType)
                )
                .build();

            val requestBuilder = Request.Builder()
                .url("$apiBaseUrl$endpoint")
                .post(body)

            if (authToken != null) {
                requestBuilder.header("Authorization", "Bearer $authToken")
            }
            val request = requestBuilder.build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        ApiException(
                            response.code,
                            "Request failed with code: ${response.code}"
                        )
                    )
                }

                val responseBody = response.body?.string()
                    ?: return@withContext Result.failure(
                        ApiException(
                            response.code,
                            "Empty response"
                        )
                    )

                // Deserialize the response
                val parsedResponse = json.decodeFromString(responseSerializer, responseBody)
                Result.success(parsedResponse)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

enum class JsonSendMethod(
    val func: Request.Builder.(RequestBody) -> Request.Builder
) {
    Post( { this.post(it) } ),
    Put( { this.put(it) } )
}

class ApiException(
    val code: Int,
    message: String,
    cause: Throwable? = null
) : IOException(message, cause)
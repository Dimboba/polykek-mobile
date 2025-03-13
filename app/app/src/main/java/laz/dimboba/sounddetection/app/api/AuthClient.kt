package laz.dimboba.sounddetection.app.api

import kotlinx.serialization.Serializable
import laz.dimboba.sounddetection.app.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthClient @Inject constructor(
    private val tokenManager: TokenManager
): BaseClient() {

    private val authBaseUrl = "/auth"

    suspend fun validateTokenAtStartUp(): TokenState {
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken.isBlank()) {
            tokenManager.clearTokens()
            return TokenState.NoActiveTokens
        }

        try {
            val result = refresh(refreshToken)
            return tokenManager.tokenState.value
        } catch (e: Exception) {
            tokenManager.clearTokens()
            return TokenState.NoActiveTokens
        }
    }

    suspend fun logIn(username: String, password: String): Result<LoginSignupResponse> {
        val requestBody = LoginRequest(username, password)
        return postJsonRequest(
            endpoint = "$authBaseUrl/login",
            requestBody = requestBody,
            serializer = LoginRequest.serializer(),
            responseSerializer = LoginSignupResponse.serializer()
        ).onSuccess { result ->
            tokenManager.saveTokens(result.accessToken, result.refreshToken)
        }
    }

    suspend fun signUp(username: String, password: String): Result<LoginSignupResponse> {
        val requestBody = LoginRequest(username, password)
        return postJsonRequest(
            endpoint = "$authBaseUrl/signup",
            requestBody = requestBody,
            serializer = LoginRequest.serializer(),
            responseSerializer = LoginSignupResponse.serializer()
        ).onSuccess { result ->
            tokenManager.saveTokens(result.accessToken, result.refreshToken)
        }
    }

    suspend fun refresh(refreshToken: String): Result<TokenResponse> {
        val requestBody = RefreshRequest(refreshToken)
        return postJsonRequest(
            endpoint = "$authBaseUrl/refresh",
            requestBody = requestBody,
            serializer = RefreshRequest.serializer(),
            responseSerializer = TokenResponse.serializer()
        ).onSuccess { result ->
            tokenManager.saveTokens(result.accessToken, result.refreshToken)
        }.onFailure {
            if(it is ApiException && it.code == 401) {
                tokenManager.clearTokens()
            }
        }
    }

}

@Serializable
data class RefreshRequest(
    val refreshToken: String
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
)

@Serializable
data class LoginSignupResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)

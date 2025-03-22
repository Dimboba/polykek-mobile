package laz.dimboba.sounddetection.app.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import laz.dimboba.sounddetection.app.api.AuthClient
import laz.dimboba.sounddetection.app.api.LoginSignupResponse
import javax.inject.Inject
import javax.inject.Singleton

private val dispatcher: CoroutineDispatcher = Dispatchers.IO

@Singleton
class LogoutUseCase @Inject constructor(
    private val recordRepository: RecordRepository,
    private val tokenManager: TokenManager,
) {
    suspend operator fun invoke(): Result<Unit> = withContext(dispatcher) {
        try {
            recordRepository.clearRecords()
            tokenManager.clearTokens()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Singleton
class LoginUseCase @Inject constructor(
    private val recordRepository: RecordRepository,
    private val authClient: AuthClient
) {
    suspend operator fun invoke(username: String, password: String): Result<LoginSignupResponse>
    = withContext(dispatcher) {
        authClient.logIn(username, password)
            .onSuccess { recordRepository.loadRecords() }
    }
}

@Singleton
class SignupUseCase @Inject constructor(
    private val recordRepository: RecordRepository,
    private val authClient: AuthClient
) {
    suspend operator fun invoke(username: String, password: String): Result<LoginSignupResponse>
            = withContext(dispatcher) {
        authClient.signUp(username, password)
            .onSuccess { recordRepository.loadRecords() }
    }
}

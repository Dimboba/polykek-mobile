package laz.dimboba.sounddetection.app.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import laz.dimboba.sounddetection.app.dto.User
import laz.dimboba.sounddetection.app.data.LoginUseCase
import laz.dimboba.sounddetection.app.data.TokenManager
import laz.dimboba.sounddetection.app.data.TokenState
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val loginUseCase: LoginUseCase
): ViewModel () {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    init {
        viewModelScope.launch {
            tokenManager.tokenState.collect { state ->
                if (state == TokenState.NoActiveTokens) {
                    _authState.value = AuthState.Idle
                }
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = loginUseCase(username, password)
            result.onSuccess {
                _authState.value = AuthState.Success(it.user)
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "UndefinedError")
            }
            //_authState.value = AuthState.Error("Wrong username or password")
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}


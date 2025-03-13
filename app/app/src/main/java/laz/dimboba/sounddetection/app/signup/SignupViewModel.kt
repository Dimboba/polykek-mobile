package laz.dimboba.sounddetection.app.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import laz.dimboba.sounddetection.app.api.AuthClient
import laz.dimboba.sounddetection.app.User
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val httpClient: AuthClient
): ViewModel() {
    private val _state = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val state: StateFlow<RegisterState> = _state

    fun registerUser(username: String, password: String, passwordRepeat: String) {
        viewModelScope.launch {
            if(passwordRepeat != password) {
                _state.value = RegisterState.PasswordsNotEqual
                return@launch
            }
            _state.value = RegisterState.Loading
            val result = httpClient.signUp(username, password)
            result.onSuccess {
                _state.value = RegisterState.Success(it.user)
            }.onFailure {
                _state.value = RegisterState.Error(it.message ?: "Undefined Error")
            }
        }
    }

    fun resetState(password: String, repeatPassword: String) {
        if(repeatPassword != password) {
            _state.value = RegisterState.PasswordsNotEqual
            return
        }
        _state.value = RegisterState.Idle
    }

}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object PasswordsNotEqual: RegisterState()
    data class Success(val user: User) : RegisterState()
    data class Error(val message: String) : RegisterState()
}
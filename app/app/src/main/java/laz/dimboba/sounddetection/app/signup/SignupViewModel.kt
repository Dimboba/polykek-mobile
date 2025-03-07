package laz.dimboba.sounddetection.app.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import laz.dimboba.sounddetection.app.User

class SignupViewModel: ViewModel() {
    private val _state = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val state: StateFlow<RegisterState> = _state

    fun registerUser(username: String, password: String, passwordRepeat: String) {
        viewModelScope.launch {
            if(passwordRepeat != password) {
                _state.value = RegisterState.PasswordsNotEqual
                return@launch
            }
            _state.value = RegisterState.Loading
            delay(1000)
            _state.value = RegisterState.Success(User("test"))
            //_state.value = RegisterState.Error("Something went wrong")
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
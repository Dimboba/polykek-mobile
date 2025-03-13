package laz.dimboba.sounddetection.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import laz.dimboba.sounddetection.app.api.AuthClient
import laz.dimboba.sounddetection.app.api.TokenManager
import laz.dimboba.sounddetection.app.api.TokenState
import javax.inject.Inject

@HiltViewModel
class AppNavigator @Inject constructor(
    tokenManager: TokenManager,
    authClient: AuthClient
): ViewModel() {
    private val currentScreen = MutableStateFlow(Screen.OnBoard)
    private val screenHistory = mutableListOf<Screen>()

    private val _exitEvent = MutableSharedFlow<Unit>()
    val exitEvent = _exitEvent.asSharedFlow()

    val screen: StateFlow<Screen> = currentScreen

    //todo: test it
    init {
        viewModelScope.launch {
            if(authClient.validateTokenAtStartUp() == TokenState.ActiveTokens) {
                changeScreen(Screen.Home)
            }
        }
        viewModelScope.launch {
            tokenManager.tokenState.onEach { state ->
                if (state == TokenState.NoActiveTokens) {
                    changeScreen(Screen.OnBoard)
                }
            }
        }
    }

    fun changeScreen(screen: Screen, clearHistoryAfter: Boolean = false ) {
        if(screen == currentScreen.value) return
        screenHistory.add(currentScreen.value)
        if (clearHistoryAfter) {
            screenHistory.clear()
        }
        currentScreen.value = screen
    }

    fun goBack() {
        val last = screenHistory.lastOrNull() ?: return
        if(last == currentScreen.value) return
//        currentScreen.value = screenHistory.removeLast()
        currentScreen.value = last
        screenHistory.removeAt(screenHistory.size - 1)
    }

    fun isEmpty(): Boolean = screenHistory.isEmpty()

    fun exit() {
        viewModelScope.launch {
            _exitEvent.emit(Unit)
        }
    }
}
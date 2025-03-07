package laz.dimboba.sounddetection.app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AppViewModel(
    private val onExit: () -> Unit
): ViewModel() {
    private val currentScreen = MutableStateFlow(Screen.OnBoard)
    private val screenHistory = mutableListOf<Screen>()

    val screen: StateFlow<Screen> = currentScreen

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
        onExit()
    }
}
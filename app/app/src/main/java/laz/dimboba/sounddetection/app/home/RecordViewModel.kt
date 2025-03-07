package laz.dimboba.sounddetection.app.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RecordViewModel: ViewModel() {
    private val _state = MutableStateFlow(RecordStatus.Idle)
    val state: StateFlow<RecordStatus> = _state


}

sealed class RecordStatus {
    object Idle : RecordStatus()
    object Recording: RecordStatus()
    object Loading: RecordStatus()
    data class ReceiveError(val message: String): RecordStatus()
    //todo: maybe use enum for notes?
    data class Success(val note: String): RecordStatus()
}
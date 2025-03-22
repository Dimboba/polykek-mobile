package laz.dimboba.sounddetection.app.home.stats

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import laz.dimboba.sounddetection.app.api.StatsClient
import laz.dimboba.sounddetection.app.data.LogoutUseCase
import laz.dimboba.sounddetection.app.data.TokenManager
import laz.dimboba.sounddetection.app.dto.Stats
import laz.dimboba.sounddetection.app.data.RecordRepository
import laz.dimboba.sounddetection.app.dto.User
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val client: StatsClient,
    private val recordRepository: RecordRepository,
    private val tokenManager: TokenManager,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    private val toDateFormatter = DateTimeFormatter
        .ofPattern("yyyy LLL dd")
        .withZone(ZoneId.systemDefault())

    private val toDateTimeFormatter = DateTimeFormatter
        .ofPattern("yyyy LLL dd  H:mm:ss")
        .withZone(ZoneId.systemDefault())

    private val _state = MutableStateFlow(StatsState())
    val state: StateFlow<StatsState> = _state

    private val _settingsState = MutableStateFlow<UserSettingsState>(UserSettingsState.Idle)
    val settingsState: StateFlow<UserSettingsState> = _settingsState

    init {
        viewModelScope.launch {
            snapshotFlow { recordRepository.recordsState.toList() }
                .collect { updateStats() }
        }
    }

    fun logOut() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }

    fun updateUser(username: String, password: String) {
        if(_settingsState.value is UserSettingsState.Sending) return
        viewModelScope.launch {
            _settingsState.value = UserSettingsState.Sending
            client.updateUser(
                username.ifEmpty { null },
                password.ifEmpty { null }
            ).onSuccess {
                _settingsState.value = UserSettingsState.Success
            }.onFailure {
                _settingsState.value = UserSettingsState.Error(it.message ?: "Undefined Error")
            }
            updateStats()
        }
    }

    private fun updateStats() {
        viewModelScope.launch {
            client.getStats()
                .onSuccess {
                    _state.value = StatsState(
                        convertStatsToMap(it)
                    )
                }
        }
    }

    private fun convertStatsToMap(stats: Stats): Map<String, String> {
        val map = mutableMapOf(
            "Username" to stats.username,
            "Total sounds recorded" to stats.recordCount.toString(),
            "Since" to toDateFormatter.format(stats.since),
            "Most frequent notes" to
                    "${stats.popularNotes.notes.joinToString(", ")} \n ${stats.popularNotes.count} times",
        )
        if(stats.lastRecordTime != null) {
            map["Last sound record time"] = toDateTimeFormatter.format(stats.lastRecordTime)
        }
        map["Most frequent octaves"] =
            "${stats.popularOctaves.octaves.joinToString(", ")}-th \n ${stats.popularOctaves.count} times"

        return map
    }
}

data class StatsState(
    val map: Map<String, String> = mapOf()
)

open class UserSettingsState {
    object Idle : UserSettingsState()
    object Sending: UserSettingsState()
    object Success: UserSettingsState()
    data class Error(val message: String): UserSettingsState()
}
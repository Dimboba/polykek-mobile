package laz.dimboba.sounddetection.app.home

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val repository: RecordRepository
): ViewModel() {
    private val _state = MutableStateFlow<RecordStatus>(RecordStatus.Idle)
    val state: StateFlow<RecordStatus> = _state

    private var recorder: MediaRecorder? = null

    private fun initializeRecorder(context: Context): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
    }

    private fun startRecording(context: Context): File? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

        val tempFile = File.createTempFile(
            "audio_record_${timestamp}",
            ".mp4",
            context.cacheDir
        )

        recorder = initializeRecorder(context).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(tempFile.path)
            setAudioChannels(1)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(44100)  // 44.1kHz
            setAudioEncodingBitRate(128000)  // 128kbps

            try {
                prepare()
            } catch (e: IOException) {
                _state.value = RecordStatus.RecordError
                return null
            }
            start()
            _state.value = RecordStatus.Recording
        }
        return tempFile
    }

    private fun stopRecording() {
        recorder?.stop()
        _state.value = RecordStatus.Sending
    }

    fun recordAndSendAudio(context: Context) {
        val file = startRecording(context) ?: return
        viewModelScope.launch {
            delay(5_000)
            stopRecording()
            _state.value = RecordStatus.Sending
            repository.addRecord(file)
                .onFailure { _state.value = RecordStatus.ReceiveError(it.message ?: "Unknown error") }
                .onSuccess { _state.value = RecordStatus.Success(it.note ?: "undefined") }
        }
    }
}

sealed class RecordStatus {
    object Idle : RecordStatus()
    object Recording: RecordStatus()
    object Sending: RecordStatus()
    object RecordError: RecordStatus()
    data class ReceiveError(val message: String): RecordStatus()
    //todo: maybe use enum for notes?
    data class Success(val note: String): RecordStatus()
}
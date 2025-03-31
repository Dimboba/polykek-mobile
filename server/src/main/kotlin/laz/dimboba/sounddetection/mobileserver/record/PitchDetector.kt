package laz.dimboba.sounddetection.mobileserver.record

import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.jvm.JVMAudioInputStream
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm
import org.springframework.stereotype.Component
import ws.schild.jave.Encoder
import ws.schild.jave.MultimediaObject
import ws.schild.jave.encode.AudioAttributes
import ws.schild.jave.encode.EncodingAttributes
import java.io.File
import javax.sound.sampled.AudioSystem
import kotlin.math.ln
import kotlin.math.sqrt

@Component
class PitchDetector {

    private fun convertAudioToWav(inputAudio: File, outputWav: File) {
        val audio = AudioAttributes()
        audio.setCodec("pcm_s16le")
        audio.setBitRate(128_000)
        audio.setChannels(1)
        audio.setSamplingRate(44100)

        val attrs = EncodingAttributes()
        attrs.setAudioAttributes(audio)
        attrs.setOutputFormat("wav")

        Encoder().encode(MultimediaObject(inputAudio), outputWav, attrs)
    }

    fun detectPitch(inputM4AFile: File): Pair<String, Int>? {
        val wavFile = File.createTempFile("wav-audio", ".wav")
        println(wavFile.absolutePath)
        convertAudioToWav(inputM4AFile, wavFile)

        val loudestPitchResult = findLoudestPitch(wavFile)
        wavFile.delete() // Clean up temporary file

        return if (loudestPitchResult != null) {
            frequencyToNoteOctave(loudestPitchResult.pitch)
        } else {
            null
        }
    }

    fun getAllPitchesWithVolumes(audioFile: File): List<PitchResult> {
        val pitchResults = mutableListOf<PitchResult>()

        try {
            val audioInputStream = AudioSystem.getAudioInputStream(audioFile)
            val format = audioInputStream.format
            val sampleRate = format.sampleRate

            val bufferSize = 1024
            val overlap = 0

            val jvmAudioStream = JVMAudioInputStream(audioInputStream)
            val dispatcher = AudioDispatcher(jvmAudioStream, bufferSize, overlap)

            val pitchHandler = PitchDetectionHandler { result, event ->
                val pitchInHz = result.pitch
                if (pitchInHz > 0) {
                    val rms = calculateRMS(event.floatBuffer)

                    pitchResults.add(PitchResult(
                        pitch = pitchInHz,
                        volume = rms,
                        timestamp = event.timeStamp
                    ))
                }
            }

            val pitchProcessor = PitchProcessor(
                PitchEstimationAlgorithm.YIN,
                sampleRate,
                bufferSize,
                pitchHandler
            )

            dispatcher.addAudioProcessor(pitchProcessor)
            dispatcher.run()

            return pitchResults

        } catch (e: Exception) {
            throw RuntimeException("Error detecting pitches: ${e.message}", e)
        }
    }

    private fun findLoudestPitch(audioFile: File): PitchResult? {
        val allPitches = getAllPitchesWithVolumes(audioFile)
        return allPitches.maxByOrNull { it.volume }
    }

    private fun calculateRMS(buffer: FloatArray): Double {
        var sum = 0.0
        for (sample in buffer) {
            sum += sample * sample
        }
        return sqrt(sum / buffer.size)
    }

    private fun frequencyToNoteOctave(frequency: Float): Pair<String, Int> {
        val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

        val a = 440.0 // A4 frequency
        val halfStepFromA4 = 12 * ln(frequency / a) / ln(2.0)
        val halfStepsRounded = Math.round(halfStepFromA4).toInt()

        var noteIndex = (halfStepsRounded + 9) % 12
        if (noteIndex < 0) noteIndex += 12

        val octave = (halfStepsRounded + 9) / 12 + 4

        return Pair(noteNames[noteIndex], octave)
    }

    data class PitchResult(
        val pitch: Float,      // Frequency in Hz
        val volume: Double,    // Volume as RMS value
        val timestamp: Double  // Time in seconds when detected
    )
}
package laz.dimboba.sounddetection.mobileserver.record

import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.ln

class PitchDetector {
//    fun detectPitch(): String {}

    fun detectPitch(audioData: FloatArray, sampleRate: Int): String {
        // Создаем AudioEvent из массива данных
        val format = TarsosDSPAudioFormat(
            sampleRate.toFloat(),
            16,
            1,
            true,
            false
        )
        val audioEvent: AudioEvent = AudioEvent(format)
        audioEvent.setFloatBuffer(audioData)


        // Используем CountDownLatch для ожидания результата обработки
        val latch = CountDownLatch(1)
        val detectedPitch = AtomicReference(-1f)
        val detectedProbability = AtomicReference(0f)


        // Создаем детектор высоты тона (используем алгоритм YIN)
        val pitchProcessor: PitchProcessor = PitchProcessor(
            PitchEstimationAlgorithm.YIN,
            sampleRate.toFloat(),
            audioData.size,
            PitchDetectionHandler { result, event ->
                // Сохраняем результат определения высоты тона
                detectedPitch.set(result.getPitch())
                detectedProbability.set(result.getProbability())
                // Уведомляем о завершении обработки
                latch.countDown()
            }
        )


        // Выполняем обработку аудиоданных
        pitchProcessor.process(audioEvent)

        try {
            // Ждем завершения обработки
            latch.await()
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            return "Обработка была прервана"
        }

        val frequency = detectedPitch.get()
        val probability = detectedProbability.get()

        return if (frequency != -1f && probability > 0.85) {
            // Преобразуем частоту в название ноты только если уверенность выше порога
            convertFrequencyToNote(frequency) + " (уверенность: " + String.format("%.2f", probability * 100) + "%)"
        } else {
            "Нота не распознана или сигнал слишком слабый"
        }
    }

    private fun convertFrequencyToNote(frequency: Float): String {
        val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

        val a = 440.0 // Частота ноты A4
        val halfStepFromA4 = 12 * ln(frequency / a) / ln(2.0)
        val halfStepsRounded = Math.round(halfStepFromA4).toInt()

        var noteIndex = (halfStepsRounded + 9) % 12
        if (noteIndex < 0) noteIndex += 12

        val octave = (halfStepsRounded + 9) / 12 + 4

        return noteNames[noteIndex] + octave
    }
}
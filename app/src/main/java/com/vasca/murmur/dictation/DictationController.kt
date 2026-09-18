package com.vasca.murmur.dictation

import com.vasca.murmur.audio.AudioCapture
import com.vasca.murmur.text.TextFormatter
import java.io.File
import java.util.concurrent.Executors

class DictationController(
    private val modelDirectory: File,
    private val formatter: TextFormatter,
    private val onPartial: (String) -> Unit,
    private val onLevel: (Float) -> Unit,
    private val onFinal: (String, Long) -> Unit,
    private val onError: (String) -> Unit
) {
    private val worker = Executors.newSingleThreadExecutor()
    private val capture = AudioCapture()
    private var engine: SpeechEngine? = null
    private var startedAt = 0L

    fun start() {
        startedAt = System.currentTimeMillis()
        worker.execute {
            try {
                engine = VoskSpeechEngine(modelDirectory).also { it.start() }
                capture.start({ audio, length -> worker.execute { engine?.accept(audio, length)?.let(onPartial) } }, onLevel)
            } catch (e: Exception) { onError(e.message ?: "Could not start dictation") }
        }
    }
    fun stop() {
        capture.stop()
        worker.execute {
            val raw = engine?.finish().orEmpty()
            engine?.close(); engine = null
            val final = formatter.format(raw)
            if (final.isNotBlank()) onFinal(final, System.currentTimeMillis() - startedAt)
        }
    }
    fun close() { capture.stop(); worker.shutdownNow(); engine?.close() }
}

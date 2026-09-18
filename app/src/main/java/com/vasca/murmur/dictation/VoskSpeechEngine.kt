package com.vasca.murmur.dictation

import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File

class VoskSpeechEngine(modelDirectory: File) : SpeechEngine {
    private val model = Model(modelDirectory.absolutePath)
    private var recognizer: Recognizer? = null
    override fun start() { recognizer = Recognizer(model, 16_000f) }
    override fun accept(audio: ByteArray, length: Int): String? {
        val recognizer = recognizer ?: return null
        return if (recognizer.acceptWaveForm(audio, length)) text(recognizer.result) else text(recognizer.partialResult)
    }
    override fun finish(): String = recognizer?.let { text(it.finalResult) } ?: ""
    override fun close() { recognizer?.close(); model.close() }
    private fun text(json: String): String = runCatching { JSONObject(json).optString("text").ifBlank { JSONObject(json).optString("partial") } }.getOrDefault("")
}

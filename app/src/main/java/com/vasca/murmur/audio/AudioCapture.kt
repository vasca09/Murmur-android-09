package com.vasca.murmur.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.util.concurrent.atomic.AtomicBoolean

class AudioCapture {
    private val recording = AtomicBoolean(false)
    private var recorder: AudioRecord? = null

    fun start(onAudio: (ByteArray, Int) -> Unit, onLevel: (Float) -> Unit) {
        if (!recording.compareAndSet(false, true)) return
        val minBuffer = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, FORMAT)
        require(minBuffer > 0) { "This device cannot record 16 kHz mono audio" }
        recorder = AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, SAMPLE_RATE, CHANNEL, FORMAT, minBuffer * 4)
        val activeRecorder = requireNotNull(recorder)
        activeRecorder.startRecording()
        Thread {
            val buffer = ByteArray(minBuffer)
            while (recording.get()) {
                val count = activeRecorder.read(buffer, 0, buffer.size)
                if (count > 0) {
                    var energy = 0.0
                    var i = 0
                    while (i + 1 < count) { val sample = ((buffer[i + 1].toInt() shl 8) or (buffer[i].toInt() and 0xff)); energy += sample * sample.toDouble(); i += 2 }
                    onLevel((kotlin.math.sqrt(energy / (count / 2).coerceAtLeast(1)) / 32768.0).toFloat())
                    onAudio(buffer.copyOf(count), count)
                }
            }
        }.apply { name = "MurmurAudio"; start() }
    }
    fun stop() { recording.set(false); recorder?.runCatching { stop(); release() }; recorder = null }
    companion object { const val SAMPLE_RATE = 16_000; const val CHANNEL = AudioFormat.CHANNEL_IN_MONO; const val FORMAT = AudioFormat.ENCODING_PCM_16BIT }
}

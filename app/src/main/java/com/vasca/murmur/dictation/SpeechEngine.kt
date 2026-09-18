package com.vasca.murmur.dictation

interface SpeechEngine : AutoCloseable {
    fun start()
    fun accept(audio: ByteArray, length: Int): String?
    fun finish(): String
    override fun close()
}

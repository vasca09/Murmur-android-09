package com.vasca.murmur.model

/** The only ASR model this application can install. */
object ModelSpec {
    const val directoryName = "vosk-model-small-en-us-0.15"
    const val zipUrl = "https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip"
    const val expectedCompressedBytes = 40L * 1024L * 1024L
}

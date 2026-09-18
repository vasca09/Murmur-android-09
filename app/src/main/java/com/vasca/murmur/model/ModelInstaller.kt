package com.vasca.murmur.model

import android.content.Context
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

class ModelInstaller(private val context: Context) {
    val modelDirectory get() = File(context.filesDir, "models/${ModelSpec.directoryName}")
    fun isInstalled() = File(modelDirectory, "am/final.mdl").isFile

    fun install(onProgress: (Int) -> Unit) {
        if (isInstalled()) return
        val modelsRoot = File(context.filesDir, "models").apply { mkdirs() }
        val staging = File(modelsRoot, ".${ModelSpec.directoryName}.staging")
        staging.deleteRecursively(); staging.mkdirs()
        val connection = (URL(ModelSpec.zipUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 20_000; readTimeout = 30_000; instanceFollowRedirects = true
        }
        try {
            connection.inputStream.use { input ->
                val length = connection.contentLengthLong.coerceAtLeast(ModelSpec.expectedCompressedBytes)
                ZipInputStream(BufferedInputStream(input)).use { zip ->
                    var entry = zip.nextEntry
                    var readTotal = 0L
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (entry != null) {
                        val destination = File(staging, entry.name)
                        if (!destination.canonicalPath.startsWith(staging.canonicalPath + File.separator)) {
                            throw SecurityException("Invalid model archive entry")
                        }
                        if (entry.isDirectory) destination.mkdirs() else {
                            destination.parentFile?.mkdirs()
                            FileOutputStream(destination).use { output ->
                                while (true) {
                                    val count = zip.read(buffer)
                                    if (count < 0) break
                                    output.write(buffer, 0, count)
                                    readTotal += count
                                    onProgress(((readTotal * 100) / length).toInt().coerceIn(0, 99))
                                }
                            }
                        }
                        zip.closeEntry(); entry = zip.nextEntry
                    }
                }
            }
            val extracted = File(staging, ModelSpec.directoryName)
            if (!File(extracted, "am/final.mdl").isFile) error("Downloaded model is incomplete")
            modelDirectory.deleteRecursively()
            if (!extracted.renameTo(modelDirectory)) error("Could not install speech model")
            staging.deleteRecursively(); onProgress(100)
        } catch (error: Exception) {
            staging.deleteRecursively(); throw error
        } finally { connection.disconnect() }
    }
}

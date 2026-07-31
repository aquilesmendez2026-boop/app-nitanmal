package com.nitanmal.app.domain.util

import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.nitanmal.app.core.config.AppContextHolder
import com.nitanmal.app.core.config.PermissionBridge
import java.io.File

private class AndroidAudioRecorder : AudioRecorder {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    override fun requestPermission(onResult: (Boolean) -> Unit) {
        PermissionBridge.requestAudioPermission(onResult)
    }

    override fun start(): Boolean {
        cancel()
        return try {
            val context = AppContextHolder.context
            val file = File.createTempFile("idea-", ".m4a", context.cacheDir)
            @Suppress("DEPRECATION")
            val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioEncodingBitRate(96_000)
            r.setAudioSamplingRate(44_100)
            r.setOutputFile(file.absolutePath)
            r.prepare()
            r.start()
            recorder = r
            outputFile = file
            true
        } catch (e: Exception) {
            Log.e("NitanmalAudio", "No se pudo iniciar la grabación", e)
            cancel()
            false
        }
    }

    override fun stop(): ByteArray? {
        val r = recorder ?: return null
        val file = outputFile
        recorder = null
        outputFile = null
        return try {
            r.stop()
            r.release()
            file?.readBytes()?.also { file.delete() }
        } catch (e: Exception) {
            Log.e("NitanmalAudio", "Error al detener la grabación", e)
            runCatching { r.release() }
            file?.delete()
            null
        }
    }

    override fun cancel() {
        recorder?.let { r ->
            runCatching { r.stop() }
            runCatching { r.release() }
        }
        recorder = null
        outputFile?.delete()
        outputFile = null
    }
}

actual fun createAudioRecorder(): AudioRecorder = AndroidAudioRecorder()

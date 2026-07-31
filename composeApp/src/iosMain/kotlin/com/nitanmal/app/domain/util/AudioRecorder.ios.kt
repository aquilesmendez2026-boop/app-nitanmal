package com.nitanmal.app.domain.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.dataWithContentsOfURL
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
private class IosAudioRecorder : AudioRecorder {
    private var recorder: AVAudioRecorder? = null
    private var fileUrl: NSURL? = null

    override fun requestPermission(onResult: (Boolean) -> Unit) {
        AVAudioSession.sharedInstance().requestRecordPermission { granted ->
            dispatch_async(dispatch_get_main_queue()) { onResult(granted) }
        }
    }

    override fun start(): Boolean {
        cancel()
        return try {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(AVAudioSessionCategoryPlayAndRecord, error = null)
            // Nota: AVAudioRecorder activa la sesión al grabar; no llamamos
            // setActive explícito (la firma del interop varía entre SDKs).

            val path = NSTemporaryDirectory() + "idea-${NSUUID().UUIDString}.m4a"
            val url = NSURL.fileURLWithPath(path)
            val settings = mapOf<Any?, Any?>(
                AVFormatIDKey to kAudioFormatMPEG4AAC,
                AVSampleRateKey to 44100.0,
                AVNumberOfChannelsKey to 1
            )
            val r = AVAudioRecorder(uRL = url, settings = settings, error = null)
            if (r.record()) {
                recorder = r
                fileUrl = url
                true
            } else {
                false
            }
        } catch (e: Exception) {
            cancel()
            false
        }
    }

    override fun stop(): ByteArray? {
        val r = recorder ?: return null
        val url = fileUrl
        recorder = null
        fileUrl = null
        r.stop()
        val data = url?.let { NSData.dataWithContentsOfURL(it) } ?: return null
        val length = data.length.toInt()
        if (length == 0) return null
        val bytes = ByteArray(length)
        bytes.usePinned { pinned ->
            memcpy(pinned.addressOf(0), data.bytes, data.length)
        }
        return bytes
    }

    override fun cancel() {
        recorder?.stop()
        recorder = null
        fileUrl = null
    }
}

actual fun createAudioRecorder(): AudioRecorder = IosAudioRecorder()

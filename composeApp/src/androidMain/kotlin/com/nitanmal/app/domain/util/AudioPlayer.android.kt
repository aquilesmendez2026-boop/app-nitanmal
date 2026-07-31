package com.nitanmal.app.domain.util

import android.media.AudioAttributes
import android.media.MediaPlayer

private class AndroidAudioPlayer : AudioPlayer {
    private var player: MediaPlayer? = null

    override fun play(url: String, onCompletion: () -> Unit) {
        stop()
        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(url)
            setOnPreparedListener { it.start() }
            setOnCompletionListener { onCompletion() }
            setOnErrorListener { _, _, _ ->
                onCompletion()
                true
            }
            prepareAsync()
        }
    }

    override fun stop() {
        player?.release()
        player = null
    }

    override fun release() = stop()
}

actual fun createAudioPlayer(): AudioPlayer = AndroidAudioPlayer()

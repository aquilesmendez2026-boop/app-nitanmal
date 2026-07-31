package com.nitanmal.app.domain.util

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.nitanmal.app.core.config.AppContextHolder

/**
 * Implementación con ExoPlayer (media3): reproduce los .webm/opus que graba
 * el web con MediaRecorder, que MediaPlayer no soporta bien en streaming.
 */
private class AndroidAudioPlayer : AudioPlayer {
    private var player: ExoPlayer? = null

    override fun play(url: String, onCompletion: () -> Unit) {
        stop()
        player = ExoPlayer.Builder(AppContextHolder.context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        onCompletion()
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.e("NitanmalAudio", "Error reproduciendo audio: ${error.errorCodeName}", error)
                    onCompletion()
                }
            })
            prepare()
            play()
        }
    }

    override fun stop() {
        player?.release()
        player = null
    }

    override fun release() = stop()
}

actual fun createAudioPlayer(): AudioPlayer = AndroidAudioPlayer()

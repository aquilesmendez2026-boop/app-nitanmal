package com.nitanmal.app.domain.util

/**
 * Reproductor simple de audio por URL (streams los audios firmados de S3).
 * Una sola pista a la vez: play() detiene lo que estuviera sonando.
 */
interface AudioPlayer {
    fun play(url: String, onCompletion: () -> Unit)
    fun stop()
    fun release()
}

expect fun createAudioPlayer(): AudioPlayer

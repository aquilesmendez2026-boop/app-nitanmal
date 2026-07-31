package com.nitanmal.app.domain.util

import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.play
import platform.AVFoundation.pause
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL

private class IosAudioPlayer : AudioPlayer {
    private var player: AVPlayer? = null
    private var observer: Any? = null

    override fun play(url: String, onCompletion: () -> Unit) {
        stop()
        val nsUrl = NSURL.URLWithString(url) ?: run {
            onCompletion()
            return
        }
        val item = AVPlayerItem(uRL = nsUrl)
        observer = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = item,
            queue = NSOperationQueue.mainQueue
        ) { _ -> onCompletion() }
        player = AVPlayer(playerItem = item).also { it.play() }
    }

    override fun stop() {
        player?.pause()
        observer?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        observer = null
        player = null
    }

    override fun release() = stop()
}

actual fun createAudioPlayer(): AudioPlayer = IosAudioPlayer()

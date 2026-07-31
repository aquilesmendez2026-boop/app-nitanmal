package com.nitanmal.app.domain.util

/**
 * Grabador de audio del micrófono. Graba AAC en contenedor .m4a
 * (reproducible en Android, iOS y el navegador).
 */
interface AudioRecorder {
    /** Pide el permiso de micrófono si hace falta. onResult(true) si quedó concedido. */
    fun requestPermission(onResult: (Boolean) -> Unit)

    /** Inicia la grabación. false si no se pudo iniciar. */
    fun start(): Boolean

    /** Detiene y devuelve los bytes grabados (null si falló o no había grabación). */
    fun stop(): ByteArray?

    /** Descarta la grabación en curso sin conservar datos. */
    fun cancel()

    val fileExtension: String get() = "m4a"
    val mimeType: String get() = "audio/mp4"
}

expect fun createAudioRecorder(): AudioRecorder

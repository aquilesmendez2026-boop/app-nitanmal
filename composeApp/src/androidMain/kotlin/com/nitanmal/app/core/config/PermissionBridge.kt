package com.nitanmal.app.core.config

/**
 * Puente para pedir el permiso de micrófono desde código común.
 * MainActivity registra el launcher y setea [requestAudioPermission].
 */
object PermissionBridge {
    var requestAudioPermission: ((Boolean) -> Unit) -> Unit = { it(false) }
}

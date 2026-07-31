package com.nitanmal.app.core.util

/** "2026-07-21T04:53:17.152Z" → "21/07/26". Sin parseo estricto: solo recorte. */
fun formatFecha(iso: String?): String {
    if (iso == null || iso.length < 10) return ""
    val y = iso.substring(2, 4)
    val m = iso.substring(5, 7)
    val d = iso.substring(8, 10)
    return "$d/$m/$y"
}

package com.nitanmal.app.core.util

import java.util.Calendar
import java.util.Locale

actual fun todayIsoDate(): String {
    val c = Calendar.getInstance()
    return String.format(
        Locale.US,
        "%04d-%02d-%02d",
        c.get(Calendar.YEAR),
        c.get(Calendar.MONTH) + 1,
        c.get(Calendar.DAY_OF_MONTH)
    )
}

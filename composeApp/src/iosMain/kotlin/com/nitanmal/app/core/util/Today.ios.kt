package com.nitanmal.app.core.util

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

actual fun todayIsoDate(): String {
    val formatter = NSDateFormatter().apply { dateFormat = "yyyy-MM-dd" }
    return formatter.stringFromDate(NSDate())
}

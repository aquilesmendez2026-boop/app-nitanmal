package com.nitanmal.app.core.util

import android.os.Build
import com.nitanmal.app.core.config.AppContextHolder

private fun info() = runCatching {
    val ctx = AppContextHolder.context
    ctx.packageManager.getPackageInfo(ctx.packageName, 0)
}.getOrNull()

actual fun versionCodeApp(): Int = info()?.let {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) it.longVersionCode.toInt()
    else @Suppress("DEPRECATION") it.versionCode
} ?: 0

actual fun versionNameApp(): String = info()?.versionName ?: ""

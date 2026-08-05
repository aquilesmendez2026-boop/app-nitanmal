package com.nitanmal.app.core.util

import platform.Foundation.NSBundle

actual fun versionCodeApp(): Int =
    (NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleVersion") as? String)?.toIntOrNull() ?: 0

actual fun versionNameApp(): String =
    (NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String) ?: ""

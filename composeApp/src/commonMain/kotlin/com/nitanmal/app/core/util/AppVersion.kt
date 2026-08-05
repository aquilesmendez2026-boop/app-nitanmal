package com.nitanmal.app.core.util

/** Código de versión instalado (versionCode en Android, CFBundleVersion en iOS). */
expect fun versionCodeApp(): Int

/** Versión visible para el usuario (1.3, 2.0…). */
expect fun versionNameApp(): String

package com.nitanmal.app.core.config

import android.annotation.SuppressLint
import android.content.Context

/** Contexto de aplicación para componentes que lo requieren (ExoPlayer). */
@SuppressLint("StaticFieldLeak")
object AppContextHolder {
    lateinit var context: Context
}

package com.nitanmal.app.core.logger

actual object Logger {
    actual fun d(tag: String, message: String) {
        println("[$tag] D: $message")
    }

    actual fun e(tag: String, message: String, throwable: Throwable?) {
        println("[$tag] E: $message${throwable?.let { " | ${it.message}" } ?: ""}")
    }

    actual fun w(tag: String, message: String) {
        println("[$tag] W: $message")
    }
}

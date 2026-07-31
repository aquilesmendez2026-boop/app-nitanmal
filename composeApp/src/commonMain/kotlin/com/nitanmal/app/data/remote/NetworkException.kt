package com.nitanmal.app.data.remote

sealed class NetworkException(message: String) : Exception(message) {
    class Unauthorized(message: String = "No autorizado") : NetworkException(message)
    class NotFound(message: String = "Recurso no encontrado") : NetworkException(message)
    class ServerError(message: String = "Error del servidor") : NetworkException(message)
    class NetworkError(message: String = "Error de conexión") : NetworkException(message)
    class Timeout(message: String = "Tiempo de espera agotado") : NetworkException(message)
    class Unknown(message: String = "Error desconocido") : NetworkException(message)
}

fun Exception.toNetworkException(): NetworkException {
    return when (this) {
        is NetworkException -> this
        else -> NetworkException.Unknown(this.message ?: "Error desconocido")
    }
}

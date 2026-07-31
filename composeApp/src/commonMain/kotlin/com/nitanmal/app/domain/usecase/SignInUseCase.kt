package com.nitanmal.app.domain.usecase

import com.nitanmal.app.domain.model.User
import com.nitanmal.app.domain.repository.AuthRepository

class SignInWithGoogleUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Result<User> {
        return repository.signInWithGoogle()
    }
}

class SelectClientUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(clientKey: String): Result<User> {
        return repository.selectClient(clientKey)
    }
}

class SignOutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.signOut()
    }
}

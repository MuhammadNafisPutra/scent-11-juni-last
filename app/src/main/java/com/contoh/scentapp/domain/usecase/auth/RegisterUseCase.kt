package com.contoh.scentapp.domain.usecase.auth

import com.contoh.scentapp.domain.AuthRepository
import com.contoh.scentapp.domain.model.User

class RegisterUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, fullName: String): Result<User> {
        return authRepository.register(email, password, fullName)
    }
}

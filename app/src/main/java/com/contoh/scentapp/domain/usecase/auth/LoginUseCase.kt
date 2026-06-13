package com.contoh.scentapp.domain.usecase.auth

import com.contoh.scentapp.domain.AuthRepository
import com.contoh.scentapp.domain.model.User

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return authRepository.login(email, password)
    }
}

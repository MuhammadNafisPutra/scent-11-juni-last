package com.contoh.scentapp.domain.usecase.auth

import com.contoh.scentapp.domain.AuthRepository

class LogoutUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}

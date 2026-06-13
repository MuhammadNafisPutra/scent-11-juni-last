package com.contoh.scentapp.domain.usecase.auth

import com.contoh.scentapp.domain.AuthRepository

class UpdatePasswordUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(currentPassword: String, newPassword: String): Result<Unit> {
        return authRepository.updatePassword(currentPassword, newPassword)
    }
}

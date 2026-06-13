package com.contoh.scentapp.domain.usecase.auth

import com.contoh.scentapp.domain.AuthRepository
import com.contoh.scentapp.domain.model.User

class GetCurrentUserUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): User? {
        return authRepository.getCurrentUser()
    }

    fun getCurrentUserId(): String? {
        return authRepository.currentUserId
    }
}

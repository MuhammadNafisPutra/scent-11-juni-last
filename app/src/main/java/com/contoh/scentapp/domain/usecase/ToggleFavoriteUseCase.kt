package com.contoh.scentapp.domain.usecase

import com.contoh.scentapp.data.repository.FavoriteRepository

class ToggleFavoriteUseCase(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(parfumId: String, currentlyFavorited: Boolean) {
        favoriteRepository.toggleFavorite(parfumId, currentlyFavorited)
    }
}

package com.contoh.scentapp.domain.usecase

import com.contoh.scentapp.domain.model.HeroBanner
import com.contoh.scentapp.domain.model.Product
import com.contoh.scentapp.data.repository.ProductRepositoryImpl
import com.contoh.scentapp.data.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetHomeProductsUseCase(
    private val productRepository: ProductRepositoryImpl,
    private val favoriteRepository: FavoriteRepository
) {
    operator fun invoke(): Flow<List<Product>> {
        return combine(
            productRepository.getAllParfums(),
            favoriteRepository.getFavoriteIds()
        ) { parfumList, favoriteIds ->
            parfumList.map { parfum ->
                Product(
                    id           = parfum.id.hashCode(),
                    firestoreId  = parfum.id,
                    brand        = parfum.brand,
                    name         = parfum.name,
                    price        = "Rp${"%,d".format(parfum.price).replace(',', '.')}",
                    volume       = "${parfum.sizes.firstOrNull() ?: 50}ml",
                    imageUrl     = parfum.imageUrl,
                    cardColor    = 0xFF1A1A1A,
                    accentColor  = 0xFFD4A853,
                    isFavorite   = parfum.id in favoriteIds,
                    description  = parfum.description,
                    aromaProfile = listOf(parfum.olfactoryFamily),
                    rating       = parfum.avgLongevity,
                    reviewCount  = parfum.reviewCount
                )
            }
        }
    }
}

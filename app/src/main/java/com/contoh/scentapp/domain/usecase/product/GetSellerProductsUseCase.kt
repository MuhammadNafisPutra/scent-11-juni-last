package com.contoh.scentapp.domain.usecase.product

import com.contoh.scentapp.domain.model.Parfum
import com.contoh.scentapp.data.repository.ProductRepositoryImpl
import kotlinx.coroutines.flow.Flow

class GetSellerProductsUseCase(private val productRepository: ProductRepositoryImpl) {
    operator fun invoke(): Flow<List<Parfum>> {
        return productRepository.getMyParfums()
    }
}

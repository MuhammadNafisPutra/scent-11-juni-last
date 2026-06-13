package com.contoh.scentapp.domain.usecase.product

import com.contoh.scentapp.data.repository.ProductRepositoryImpl

class DeleteProductUseCase(private val productRepository: ProductRepositoryImpl) {
    suspend operator fun invoke(firestoreId: String): Result<Unit> {
        return productRepository.deleteParfum(firestoreId)
    }
}

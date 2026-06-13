package com.contoh.scentapp.domain.usecase.product

import com.contoh.scentapp.domain.model.Parfum
import com.contoh.scentapp.data.repository.ProductRepositoryImpl

class GetProductDetailUseCase(private val productRepository: ProductRepositoryImpl) {
    suspend operator fun invoke(firestoreId: String): Result<Parfum> {
        return productRepository.getParfumById(firestoreId)
    }
}

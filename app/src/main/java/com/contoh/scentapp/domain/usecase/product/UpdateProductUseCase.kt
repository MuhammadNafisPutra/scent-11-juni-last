package com.contoh.scentapp.domain.usecase.product

import android.content.Context
import android.net.Uri
import com.contoh.scentapp.domain.model.Parfum
import com.contoh.scentapp.data.repository.ProductRepositoryImpl

class UpdateProductUseCase(private val productRepository: ProductRepositoryImpl) {
    suspend operator fun invoke(context: Context, imageUri: Uri?, parfum: Parfum, existingImageUrl: String): Result<Unit> {
        val imageUrl = if (imageUri != null) {
            val uploadResult = productRepository.uploadProductImage(context, imageUri)
            if (uploadResult.isFailure) return Result.failure(uploadResult.exceptionOrNull()!!)
            uploadResult.getOrDefault(existingImageUrl)
        } else {
            existingImageUrl
        }
        return productRepository.updateParfum(parfum.copy(imageUrl = imageUrl))
    }
}

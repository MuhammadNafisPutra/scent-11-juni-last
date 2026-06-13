package com.contoh.scentapp.domain.usecase.product

import com.contoh.scentapp.domain.model.Review
import com.contoh.scentapp.data.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow

class GetProductReviewsUseCase(private val reviewRepository: ReviewRepository) {
    operator fun invoke(firestoreId: String): Flow<List<Review>> {
        return reviewRepository.getReviews(firestoreId)
    }
}

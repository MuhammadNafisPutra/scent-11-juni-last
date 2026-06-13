package com.contoh.scentapp.domain.usecase.cart

import com.contoh.scentapp.data.repository.CartRepository

class UpdateCartQuantityUseCase(private val cartRepository: CartRepository) {
    operator fun invoke(productId: Int, change: Int) {
        if (change > 0) {
            cartRepository.increaseQuantity(productId)
        } else if (change < 0) {
            cartRepository.decreaseQuantity(productId)
        }
    }
}

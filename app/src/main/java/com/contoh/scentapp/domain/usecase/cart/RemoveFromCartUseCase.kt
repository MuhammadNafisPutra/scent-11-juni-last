package com.contoh.scentapp.domain.usecase.cart

import com.contoh.scentapp.data.repository.CartRepository

class RemoveFromCartUseCase(private val cartRepository: CartRepository) {
    operator fun invoke(productId: Int) {
        cartRepository.removeFromCart(productId)
    }
}

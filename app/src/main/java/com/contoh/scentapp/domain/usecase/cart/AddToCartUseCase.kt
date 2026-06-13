package com.contoh.scentapp.domain.usecase.cart

import com.contoh.scentapp.domain.model.CartItem
import com.contoh.scentapp.data.repository.CartRepository

class AddToCartUseCase(private val cartRepository: CartRepository) {
    operator fun invoke(cartItem: CartItem) {
        cartRepository.addToCart(cartItem)
    }
}

package com.contoh.scentapp.domain.usecase.cart

import com.contoh.scentapp.data.repository.CartRepository

class ClearCartUseCase(private val cartRepository: CartRepository) {
    operator fun invoke() {
        cartRepository.clearCart()
    }
}

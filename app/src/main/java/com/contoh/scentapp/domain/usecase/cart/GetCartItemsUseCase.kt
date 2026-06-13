package com.contoh.scentapp.domain.usecase.cart

import com.contoh.scentapp.domain.model.CartItem
import com.contoh.scentapp.data.repository.CartRepository
import kotlinx.coroutines.flow.Flow

class GetCartItemsUseCase(private val cartRepository: CartRepository) {
    operator fun invoke(): Flow<List<CartItem>> {
        return cartRepository.cartItems
    }
}

package com.contoh.scentapp.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.contoh.scentapp.data.model.CartItem
import com.contoh.scentapp.data.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartUiState(
    val items     : List<CartItem> = emptyList(),
    val isLoading : Boolean        = true
) {
    val isEmpty       : Boolean get() = items.isEmpty()
    val totalItems    : Int     get() = items.sumOf { it.quantity }
    val subtotal      : Int     get() = items.sumOf { it.totalPrice }
    val shippingCost  : Int     get() = 0
    val estimasiTotal : Int     get() = subtotal + shippingCost

    val formattedSubtotal: String
        get() = "Rp${"%,.0f".format(subtotal.toDouble()).replace(",", ".")}"
    val formattedTotal: String
        get() = "Rp${"%,.0f".format(estimasiTotal.toDouble()).replace(",", ".")}"
    val headerSubtitle: String
        get() = "$totalItems PRODUK DI ATELIER"
}

class CartViewModel(
    private val repository: CartRepository = CartRepository.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init { observeCart() }

    private fun observeCart() {
        viewModelScope.launch {
            repository.cartItems.collect { items ->
                _uiState.update { it.copy(items = items, isLoading = false) }
            }
        }
    }

    fun increaseQty(productId: Int) { repository.increaseQuantity(productId) }
    fun decreaseQty(productId: Int) { repository.decreaseQuantity(productId) }
    fun removeItem(productId: Int)  { repository.removeFromCart(productId) }
    fun clearCart()                 { repository.clearCart() }
}

class CartViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            return CartViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

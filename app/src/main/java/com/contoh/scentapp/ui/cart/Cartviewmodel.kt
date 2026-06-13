package com.contoh.scentapp.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contoh.scentapp.domain.model.CartItem
import com.contoh.scentapp.domain.usecase.cart.GetCartItemsUseCase
import com.contoh.scentapp.domain.usecase.cart.UpdateCartQuantityUseCase
import com.contoh.scentapp.domain.usecase.cart.RemoveFromCartUseCase
import com.contoh.scentapp.domain.usecase.cart.ClearCartUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartUiState(
    val items     : List<CartItem> = emptyList(),
    val isLoading : Boolean        = true,
    val totalPrice: String         = "Rp0",
    val totalPriceLong: Long       = 0L
) {
    val isEmpty       : Boolean get() = items.isEmpty()
    val totalItems    : Int     get() = items.sumOf { it.quantity }
    val subtotal      : Int     get() = items.sumOf { it.pricePerItem * it.quantity }
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
    private val getCartItemsUseCase: GetCartItemsUseCase,
    private val updateCartQuantityUseCase: UpdateCartQuantityUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase,
    private val clearCartUseCase: ClearCartUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getCartItemsUseCase().collect { items ->
                val total = items.sumOf { it.pricePerItem * it.quantity }
                _uiState.update {
                    it.copy(
                        items      = items,
                        isLoading  = false,
                        totalPrice = "Rp${"%,d".format(total).replace(',', '.')}",
                        totalPriceLong = total.toLong()
                    )
                }
            }
        }
    }

    fun increaseQty(productId: Int) {
        updateCartQuantityUseCase(productId, 1)
    }

    fun decreaseQty(productId: Int) {
        updateCartQuantityUseCase(productId, -1)
    }

    fun removeItem(productId: Int) {
        removeFromCartUseCase(productId)
    }

    fun clearCart() {
        clearCartUseCase()
    }
}

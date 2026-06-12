package com.contoh.scentapp.data.repository

import com.contoh.scentapp.data.model.CartItem
import com.contoh.scentapp.data.model.ShippingOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CartRepository private constructor() {
    companion object {
        @Volatile
        private var INSTANCE: CartRepository? = null

        fun getInstance(): CartRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: CartRepository().also { INSTANCE = it }
            }
    }

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: Flow<List<CartItem>> = _cartItems.asStateFlow()

    /**
     * Ringkasan total pesanan (subtotal produk + biaya pengiriman) yang
     * dihitung di halaman Pengiriman saat user menekan "Lanjutkan Pesanan".
     * Dipakai oleh halaman setelah pemesanan agar totalnya sinkron dengan
     * yang ditampilkan saat checkout.
     */
    private val _checkoutSummary = MutableStateFlow(CheckoutSummary())
    val checkoutSummary: Flow<CheckoutSummary> = _checkoutSummary.asStateFlow()

    fun setCheckoutSummary(subtotal: Int, shippingFee: Int) {
        _checkoutSummary.value = CheckoutSummary(subtotal = subtotal, shippingFee = shippingFee)
    }

    fun addToCart(item: CartItem) {
        _cartItems.update { list ->
            // FIX: compare productId to productId (was comparing id to productId)
            val existing = list.find { it.productId == item.productId }
            if (existing != null) {
                list.map {
                    if (it.productId == item.productId)
                        it.copy(quantity = it.quantity + 1)
                    else it
                }
            } else {
                list + item
            }
        }
    }

    fun removeFromCart(productId: Int) {
        _cartItems.update { list -> list.filter { it.productId != productId } }
    }

    fun increaseQuantity(productId: Int) {
        _cartItems.update { list ->
            list.map {
                if (it.productId == productId) it.copy(quantity = it.quantity + 1)
                else it
            }
        }
    }

    fun decreaseQuantity(productId: Int) {
        _cartItems.update { list ->
            list.mapNotNull {
                if (it.productId == productId) {
                    if (it.quantity > 1) it.copy(quantity = it.quantity - 1)
                    else null
                } else it
            }
        }
    }

    fun clearCart() { _cartItems.update { emptyList() } }

    val shippingOptions: List<ShippingOption> = listOf(
        ShippingOption(id="jnt",     name="J&T Express",  badge="REGULAR", estimasi="Estimasi tiba: 2-3 hari", price=15_000, iconType="truck"),
        ShippingOption(id="sicepat", name="SiCepat",      badge="BEST",    estimasi="Estimasi tiba: 1-2 hari", price=22_000, iconType="lightning"),
        ShippingOption(id="jne",     name="JNE",          badge="YES",     estimasi="Estimasi tiba: 1 hari",   price=35_000, iconType="plane"),
        ShippingOption(id="grab",    name="Grab/Gojek",   badge="INSTANT", estimasi="Estimasi tiba: 3 jam",    price=50_000, iconType="bike")
    )
}

data class CheckoutSummary(
    val subtotal    : Int = 0,
    val shippingFee : Int = 0
) {
    val total: Int get() = subtotal + shippingFee
}

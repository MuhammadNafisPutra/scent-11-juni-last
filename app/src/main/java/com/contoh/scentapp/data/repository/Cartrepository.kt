package com.contoh.scentapp.data.repository

import com.contoh.scentapp.domain.model.CartItem
import com.contoh.scentapp.domain.model.ShippingOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import com.contoh.scentapp.data.local.dao.CartDao
import com.contoh.scentapp.data.local.entity.toEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CartRepository private constructor(private val cartDao: CartDao) {
    companion object {
        @Volatile
        private var INSTANCE: CartRepository? = null

        fun getInstance(cartDao: CartDao): CartRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: CartRepository(cartDao).also { INSTANCE = it }
            }
        
        // Throw error if not initialized
        fun getInstance(): CartRepository = INSTANCE ?: throw IllegalStateException("CartRepository not initialized")
    }

    val cartItems: Flow<List<CartItem>> = cartDao.getAllCartItems().map { list ->
        list.map { it.toDomainModel() }
    }
    
    private val scope = CoroutineScope(Dispatchers.IO)

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
        scope.launch {
            val existing = cartDao.getCartItemByProductId(item.productId)
            if (existing != null) {
                cartDao.updateCartItem(existing.copy(quantity = existing.quantity + 1))
            } else {
                cartDao.insertCartItem(item.toEntity())
            }
        }
    }

    fun removeFromCart(productId: Int) {
        scope.launch {
            cartDao.deleteCartItemByProductId(productId)
        }
    }

    fun increaseQuantity(productId: Int) {
        scope.launch {
            val existing = cartDao.getCartItemByProductId(productId)
            if (existing != null) {
                cartDao.updateCartItem(existing.copy(quantity = existing.quantity + 1))
            }
        }
    }

    fun decreaseQuantity(productId: Int) {
        scope.launch {
            val existing = cartDao.getCartItemByProductId(productId)
            if (existing != null) {
                if (existing.quantity > 1) {
                    cartDao.updateCartItem(existing.copy(quantity = existing.quantity - 1))
                } else {
                    cartDao.deleteCartItemByProductId(productId)
                }
            }
        }
    }

    fun clearCart() {
        scope.launch {
            cartDao.clearCart()
        }
    }

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

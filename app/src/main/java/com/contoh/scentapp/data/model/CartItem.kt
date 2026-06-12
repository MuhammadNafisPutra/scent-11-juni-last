package com.contoh.scentapp.data.model

data class CartItem(
    val id: Int = 0,
    val productId: Int = 0,
    val name: String = "",
    val brand: String = "",
    val aromaProfile: String = "",
    val imageUrl: String = "",
    val volume: String = "",
    val isDecant: Boolean = false,
    val pricePerItem: Int = 0,

    var quantity: Int = 1,
    val cardColor: Long = 0xFF1A1A1A,
    val accentColor: Long = 0xFFD4A853
) {
    val totalPrice: Int get() = pricePerItem * quantity
    val formattedPrice: String get() =
        "Rp%,.0f".format(totalPrice.toDouble()).replace(",", ".")
}
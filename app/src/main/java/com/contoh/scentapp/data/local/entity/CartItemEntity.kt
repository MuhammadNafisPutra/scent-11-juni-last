package com.contoh.scentapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.contoh.scentapp.domain.model.CartItem

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productId: Int,
    val firestoreId: String,
    val sellerId: String,
    val name: String,
    val brand: String,
    val aromaProfile: String,
    val imageUrl: String,
    val volume: String,
    val isDecant: Boolean,
    val pricePerItem: Int,
    val quantity: Int,
    val cardColor: Long,
    val accentColor: Long
) {
    fun toDomainModel() = CartItem(
        id = id,
        productId = productId,
        firestoreId = firestoreId,
        sellerId = sellerId,
        name = name,
        brand = brand,
        aromaProfile = aromaProfile,
        imageUrl = imageUrl,
        volume = volume,
        isDecant = isDecant,
        pricePerItem = pricePerItem,
        quantity = quantity,
        cardColor = cardColor,
        accentColor = accentColor
    )
}

fun CartItem.toEntity() = CartItemEntity(
    id = id,
    productId = productId,
    firestoreId = firestoreId,
    sellerId = sellerId,
    name = name,
    brand = brand,
    aromaProfile = aromaProfile,
    imageUrl = imageUrl,
    volume = volume,
    isDecant = isDecant,
    pricePerItem = pricePerItem,
    quantity = quantity,
    cardColor = cardColor,
    accentColor = accentColor
)

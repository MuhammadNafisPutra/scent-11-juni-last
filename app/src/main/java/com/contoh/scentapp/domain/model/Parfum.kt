package com.contoh.scentapp.domain.model

data class Parfum(
    val id: String = "",
    val sellerId: String = "",
    val name: String = "",
    val brand: String = "",
    val price: Long = 0L,
    val decantPrice: Long = 0L,
    val stock: Int = 0,
    val imageUrl: String = "",
    val description: String = "",
    val olfactoryFamily: String = "",
    val topNotes: List<String> = emptyList(),
    val middleNotes: List<String> = emptyList(),
    val baseNotes: List<String> = emptyList(),
    val sizes: List<Int> = listOf(30, 50, 100),
    val avgLongevity: Float = 0f,
    val avgSillage: Float = 0f,
    val avgProjection: Float = 0f,
    val reviewCount: Int = 0,
    val isDecantAvailable: Boolean = false,
    val isLimitedDrop: Boolean = false,
    val usage: String = "",          // "SIANG", "MALAM", atau "KEDUANYA"
    val createdAt: Long = 0L
)

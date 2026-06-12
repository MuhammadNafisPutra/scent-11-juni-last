package com.contoh.scentapp.data.model

data class Review(
    val id: Int = 0,
    val parfumId: String = "",
    val reviewerId: String = "",
    val name: String = "",
    val initials: String = "",
    val badge: String = "",
    val avatarColor: Long = 0xFF2A3545,
    val text: String = "",
    val rating: Float = 5f,
    val longevity: Float = 0f,
    val sillage: Float = 0f,
    val projection: Float = 0f,
    val photoUrls: List<String> = emptyList(),
    val imageCount: Int = 0,
    val date: String = "",
    val createdAt: Long = 0L
) {
    val avgRating: Float get() = (longevity + sillage + projection) / 3f
}
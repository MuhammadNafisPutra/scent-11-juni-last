package com.contoh.scentapp.domain.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val defaultAddress: String = "",
    val scentProfile: List<String> = emptyList(),
    @ServerTimestamp  // âœ… ADD THIS
    val createdAt: Date? = null  // âœ… ADD THIS
)

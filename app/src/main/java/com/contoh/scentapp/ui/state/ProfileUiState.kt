package com.contoh.scentapp.ui.state

import com.contoh.scentapp.domain.model.*

data class ProfileUiState(
    // FIX: ganti 'name' jadi 'fullName' dan tambah 'profileImageUrl'
    // agar konsisten dengan data class User dan Firestore
    val fullName        : String  = "",
    val email           : String  = "",
    val profileImageUrl : String  = "",
    val address         : String  = "",
    val isDarkMode      : Boolean = true,
    val language        : String  = "INDONESIA",
    val showDeleteDialog: Boolean = false
)

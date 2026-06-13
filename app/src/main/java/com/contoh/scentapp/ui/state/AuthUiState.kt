package com.contoh.scentapp.ui.state

import com.contoh.scentapp.domain.model.*

data class AuthUiState(
    val loginEmail      : String  = "",
    val loginPassword   : String  = "",
    val showLoginPass   : Boolean = false,

    val registerName    : String  = "",
    val registerEmail   : String  = "",
    val registerPassword: String  = "",
    val showRegisterPass: Boolean = false,

    val isLoading       : Boolean = false,
    val errorMessage    : String? = null,
    val isLoggedIn      : Boolean = false,
    val currentUser     : User?   = null  // âœ… ADD THIS
)

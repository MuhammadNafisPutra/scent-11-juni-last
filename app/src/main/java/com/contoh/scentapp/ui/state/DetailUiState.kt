package com.contoh.scentapp.ui.state

import com.contoh.scentapp.domain.model.*

data class DetailUiState(
    val isLoading      : Boolean          = true,
    val product        : Product?         = null,
    val sizeOptions    : List<SizeOption> = emptyList(),
    val selectedSizeId : String           = "full",
    val reviews        : List<Review>     = emptyList(),
    val errorMessage   : String?          = null
)

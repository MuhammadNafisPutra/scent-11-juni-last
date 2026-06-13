package com.contoh.scentapp.ui.state

import com.contoh.scentapp.domain.model.*

data class HomeUiState(
    val isLoading    : Boolean       = true,
    val heroBanner   : HeroBanner?   = null,
    val products     : List<Product> = emptyList(),
    val errorMessage : String?       = null
) {
    val filteredProducts: List<Product> get() = products
}

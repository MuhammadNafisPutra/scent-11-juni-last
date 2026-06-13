package com.contoh.scentapp.ui.state

import com.contoh.scentapp.domain.model.*

data class SearchUiState(
    val query                : String           = "",
    val allProducts          : List<Product>     = emptyList(),
    val aromaFilters         : List<AromaFilter> = emptyList(),
    val usageFilters         : List<UsageFilter>  = emptyList(),
    val selectedAromaFilters : Set<String>       = emptySet(),
    val selectedUsage        : String?           = null,
    val results              : List<Product>     = emptyList(),
    val isLoading            : Boolean           = true,
    val recentSearches       : List<String>      = emptyList()
) {
    val resultCount     : Int     get() = results.size
    val hasProducts     : Boolean get() = allProducts.isNotEmpty()
    val hasActiveFilters: Boolean get() =
        selectedAromaFilters.isNotEmpty() || selectedUsage != null
}

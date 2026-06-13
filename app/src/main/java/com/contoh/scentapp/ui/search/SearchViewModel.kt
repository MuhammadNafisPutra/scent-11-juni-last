package com.contoh.scentapp.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contoh.scentapp.domain.model.AromaFilter
import com.contoh.scentapp.domain.model.Product
import com.contoh.scentapp.ui.state.SearchUiState
import com.contoh.scentapp.domain.model.UsageFilter
import com.contoh.scentapp.domain.usecase.product.GetAllProductsUseCase
import com.contoh.scentapp.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.contoh.scentapp.domain.usecase.search.AddSearchQueryUseCase
import com.contoh.scentapp.domain.usecase.search.ClearSearchHistoryUseCase
import com.contoh.scentapp.domain.usecase.search.GetRecentSearchesUseCase

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val getAllProductsUseCase: GetAllProductsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getRecentSearchesUseCase: GetRecentSearchesUseCase,
    private val addSearchQueryUseCase: AddSearchQueryUseCase,
    private val clearSearchHistoryUseCase: ClearSearchHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        observeProducts()
        observeRecentSearches()

        viewModelScope.launch {
            _uiState
                .map { Triple(it.query, it.selectedAromaFilters, it.selectedUsage) }
                .distinctUntilChanged()
                .debounce(300L)
                .collect { applyFilters() }
        }
    }

    private fun observeRecentSearches() {
        viewModelScope.launch {
            getRecentSearchesUseCase().collect { history ->
                _uiState.update { it.copy(recentSearches = history) }
            }
        }
    }

    private fun observeProducts() {
        viewModelScope.launch {
            getAllProductsUseCase()
                .catch { _uiState.update { it.copy(isLoading = false) } }
                .collect { products ->
                    _uiState.update { state ->
                        state.copy(
                            allProducts  = products,
                            aromaFilters = buildAromaFilters(products),
                            usageFilters = buildUsageFilters(products),
                            isLoading    = false
                        )
                    }
                    applyFilters()
                }
        }
    }

    private fun buildAromaFilters(products: List<Product>): List<AromaFilter> {
        return products
            .flatMap { it.aromaProfile }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { it.uppercase() }
            .distinct()
            .sorted()
            .map { AromaFilter(id = it, label = it) }
    }

    private fun buildUsageFilters(products: List<Product>): List<UsageFilter> {
        val order = listOf("SIANG", "MALAM", "KEDUANYA")
        val existing = products
            .map { it.usage.trim().uppercase() }
            .filter { it.isNotBlank() }
            .distinct()

        return order
            .filter { it in existing }
            .map { usage ->
                val label = when (usage) {
                    "SIANG"    -> "☀️ SIANG"
                    "MALAM"    -> "🌙 MALAM"
                    "KEDUANYA" -> "✨ KEDUANYA"
                    else       -> usage
                }
                UsageFilter(id = usage, label = label)
            }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun toggleAromaFilter(filterId: String) {
        _uiState.update { state ->
            val current = state.selectedAromaFilters.toMutableSet()
            if (filterId in current) current.remove(filterId) else current.add(filterId)
            state.copy(selectedAromaFilters = current)
        }
    }

    fun toggleUsageFilter(filterId: String) {
        _uiState.update { state ->
            val newUsage = if (state.selectedUsage == filterId) null else filterId
            state.copy(selectedUsage = newUsage)
        }
    }

    fun clearAllFilters() {
        _uiState.update {
            it.copy(
                selectedAromaFilters = emptySet(),
                selectedUsage        = null,
                query                = ""
            )
        }
    }

    fun toggleFavorite(productId: Int) {
        viewModelScope.launch {
            val state    = _uiState.value
            val product  = state.allProducts.find { it.id == productId }
                ?: state.results.find { it.id == productId }
                ?: return@launch
            toggleFavoriteUseCase(
                parfumId           = product.firestoreId,
                currentlyFavorited = product.isFavorite
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            clearSearchHistoryUseCase()
        }
    }

    fun applyFilters() {
        val state = _uiState.value
        val results = state.allProducts.filter { p ->
            val matchQuery = state.query.isBlank() ||
                    p.name.contains(state.query, ignoreCase = true) ||
                    p.brand.contains(state.query, ignoreCase = true)
            val matchAroma = state.selectedAromaFilters.isEmpty() ||
                    p.aromaProfile.any { it.uppercase() in state.selectedAromaFilters }
            val matchUsage = state.selectedUsage == null ||
                    p.usage.uppercase() == state.selectedUsage!!.uppercase() ||
                    p.usage.uppercase() == "KEDUANYA"
            matchQuery && matchAroma && matchUsage
        }
        
        // Simpan riwayat pencarian jika tidak kosong dan hanya ketika mencari
        if (state.query.isNotBlank()) {
            viewModelScope.launch {
                addSearchQueryUseCase(state.query)
            }
        }
        
        _uiState.update { it.copy(results = results) }
    }
}

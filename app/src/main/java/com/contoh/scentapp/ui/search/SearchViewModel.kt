package com.contoh.scentapp.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.contoh.scentapp.data.model.AromaFilter
import com.contoh.scentapp.data.model.Product
import com.contoh.scentapp.data.model.SearchUiState
import com.contoh.scentapp.data.model.UsageFilter
import com.contoh.scentapp.data.repository.FavoriteRepository
import com.contoh.scentapp.data.repository.ProductRepositoryImpl
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val productRepo  : ProductRepositoryImpl = ProductRepositoryImpl(),
    private val favoriteRepo : FavoriteRepository    = FavoriteRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        observeProducts()

        // Re-jalankan filter/pencarian setiap kali query atau filter berubah,
        // dengan debounce supaya tidak terlalu sering saat user mengetik.
        viewModelScope.launch {
            _uiState
                .map { Triple(it.query, it.selectedAromaFilters, it.selectedUsage) }
                .distinctUntilChanged()
                .debounce(300L)
                .collect { applyFilters() }
        }
    }

    /**
     * Sinkronkan daftar produk dari Firestore (sama seperti Home),
     * sehingga pencarian selalu mencocokkan produk yang benar-benar ada/sudah ditambahkan.
     */
    private fun observeProducts() {
        viewModelScope.launch {
            combine(
                productRepo.getAllParfums(),
                favoriteRepo.getFavoriteIds()
            ) { parfumList, favoriteIds ->
                parfumList.map { parfum ->
                    Product(
                        id           = parfum.id.hashCode(),
                        firestoreId  = parfum.id,
                        brand        = parfum.brand,
                        name         = parfum.name,
                        price        = "Rp${"%,d".format(parfum.price).replace(',', '.')}",
                        volume       = "${parfum.sizes.firstOrNull() ?: 50}ml",
                        imageUrl     = parfum.imageUrl,
                        cardColor    = 0xFF1A1A1A,
                        accentColor  = 0xFFD4A853,
                        isFavorite   = parfum.id in favoriteIds,
                        description  = parfum.description,
                        aromaProfile = listOf(parfum.olfactoryFamily).filter { it.isNotBlank() },
                        usage        = parfum.usage,          // ← petakan field usage
                        rating       = parfum.avgLongevity,
                        reviewCount  = parfum.reviewCount
                    )
                }
            }
                .catch { _uiState.update { it.copy(isLoading = false) } }
                .collect { products ->
                    _uiState.update { state ->
                        state.copy(
                            allProducts  = products,
                            aromaFilters = buildAromaFilters(products),
                            usageFilters = buildUsageFilters(products),  // ← populate usageFilters
                            isLoading    = false
                        )
                    }
                    applyFilters()
                }
        }
    }

    /**
     * Bangun daftar chip filter aroma HANYA dari profil aroma produk yang
     * benar-benar ada saat ini.
     */
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

    /**
     * Bangun daftar filter waktu penggunaan dari data produk yang ada.
     * Urutkan: SIANG → MALAM → KEDUANYA sehingga tampil konsisten.
     */
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
                    "SIANG"    -> "☀ SIANG"
                    "MALAM"    -> "🌙 MALAM"
                    "KEDUANYA" -> "✦ KEDUANYA"
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
            favoriteRepo.toggleFavorite(
                parfumId           = product.firestoreId,
                currentlyFavorited = product.isFavorite
            )
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
                    p.usage.uppercase() == "KEDUANYA"   // produk KEDUANYA cocok untuk filter apapun
            matchQuery && matchAroma && matchUsage
        }
        _uiState.update { it.copy(results = results) }
    }
}

class SearchViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

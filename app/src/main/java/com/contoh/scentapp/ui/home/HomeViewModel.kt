package com.contoh.scentapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.contoh.scentapp.data.model.HeroBanner
import com.contoh.scentapp.data.model.HomeUiState
import com.contoh.scentapp.data.model.Product
import com.contoh.scentapp.data.repository.FavoriteRepository
import com.contoh.scentapp.data.repository.ProductRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val productRepo  : ProductRepositoryImpl = ProductRepositoryImpl(),
    private val favoriteRepo : FavoriteRepository    = FavoriteRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { loadProducts() }

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Gabungkan stream produk + stream favorit secara realtime
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
                        aromaProfile = listOf(parfum.olfactoryFamily),
                        rating       = parfum.avgLongevity,
                        reviewCount  = parfum.reviewCount
                    )
                }
            }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collect { products ->
                    _uiState.update {
                        it.copy(
                            isLoading  = false,
                            heroBanner = heroBanner,
                            products   = products
                        )
                    }
                }
        }
    }

    fun toggleFavorite(productId: Int) {
        val product = _uiState.value.products.find { it.id == productId } ?: return
        viewModelScope.launch {
            // Firestore update — UI otomatis reaktif karena combine() di atas
            favoriteRepo.toggleFavorite(
                parfumId           = product.firestoreId,
                currentlyFavorited = product.isFavorite
            )
        }
    }

    private val heroBanner = HeroBanner(
        tag           = "RILIS TERBATAS",
        title         = "NOIR\nABSOLU",
        description   = "Perpaduan etereal dari kayu oud asap, amber beludru, " +
                "dan melati tengah malam. Keahlian dalam setiap tetes.",
        gradientStart = 0xFF2A2A2A,
        gradientEnd   = 0xFF0A0A0A
    )
}

class HomeViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
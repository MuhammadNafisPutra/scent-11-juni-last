package com.contoh.scentapp.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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

data class FavoriteUiState(
    val favorites    : List<Product> = emptyList(),
    val isLoading    : Boolean       = true,
    val errorMessage : String?       = null
) {
    val isEmpty: Boolean get() = favorites.isEmpty()
}

class FavoriteViewModel(
    private val productRepo  : ProductRepositoryImpl = ProductRepositoryImpl(),
    private val favoriteRepo : FavoriteRepository    = FavoriteRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoriteUiState())
    val uiState: StateFlow<FavoriteUiState> = _uiState.asStateFlow()

    init { observeFavorites() }

    private fun observeFavorites() {
        viewModelScope.launch {
            // Gabungkan stream semua produk + stream favorit IDs secara realtime
            combine(
                productRepo.getAllParfums(),
                favoriteRepo.getFavoriteIds()
            ) { parfumList, favoriteIds ->
                parfumList
                    .filter { it.id in favoriteIds }
                    .map { parfum ->
                        Product(
                            id          = parfum.id.hashCode(),
                            firestoreId = parfum.id,
                            brand       = parfum.brand,
                            name        = parfum.name,
                            price       = "Rp${"%,d".format(parfum.price).replace(',', '.')}",
                            volume      = "${parfum.sizes.firstOrNull() ?: 50}ml",
                            imageUrl    = parfum.imageUrl,
                            cardColor   = 0xFF1A1A1A,
                            accentColor = 0xFFD4A853,
                            isFavorite  = true,
                            collection  = parfum.olfactoryFamily,
                            description = parfum.description,
                            rating      = parfum.avgLongevity,
                            reviewCount = parfum.reviewCount
                        )
                    }
            }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collect { favorites ->
                    _uiState.update {
                        it.copy(isLoading = false, favorites = favorites)
                    }
                }
        }
    }

    fun removeFromFavorite(productId: Int) {
        val product = _uiState.value.favorites.find { it.id == productId } ?: return
        viewModelScope.launch {
            favoriteRepo.toggleFavorite(
                parfumId           = product.firestoreId,
                currentlyFavorited = true
            )
        }
    }
}

class FavoriteViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoriteViewModel::class.java)) {
            return FavoriteViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
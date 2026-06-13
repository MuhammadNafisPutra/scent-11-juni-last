package com.contoh.scentapp.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contoh.scentapp.domain.model.Product
import com.contoh.scentapp.domain.usecase.product.GetAllProductsUseCase
import com.contoh.scentapp.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
    private val getAllProductsUseCase: GetAllProductsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoriteUiState())
    val uiState: StateFlow<FavoriteUiState> = _uiState.asStateFlow()

    init { observeFavorites() }

    private fun observeFavorites() {
        viewModelScope.launch {
            getAllProductsUseCase()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collect { products ->
                    val favorites = products.filter { it.isFavorite }
                    _uiState.update {
                        it.copy(isLoading = false, favorites = favorites)
                    }
                }
        }
    }

    fun removeFromFavorite(productId: Int) {
        val product = _uiState.value.favorites.find { it.id == productId } ?: return
        viewModelScope.launch {
            toggleFavoriteUseCase(
                parfumId           = product.firestoreId,
                currentlyFavorited = true
            )
        }
    }
}
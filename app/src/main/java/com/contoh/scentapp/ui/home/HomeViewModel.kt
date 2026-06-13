package com.contoh.scentapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.contoh.scentapp.domain.model.HeroBanner
import com.contoh.scentapp.ui.state.HomeUiState
import com.contoh.scentapp.domain.model.Product
import com.contoh.scentapp.domain.usecase.GetHomeProductsUseCase
import com.contoh.scentapp.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getHomeProductsUseCase: GetHomeProductsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { loadProducts() }

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getHomeProductsUseCase()
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
            toggleFavoriteUseCase(
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

class HomeViewModelFactory(
    private val getHomeProductsUseCase: GetHomeProductsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(getHomeProductsUseCase, toggleFavoriteUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
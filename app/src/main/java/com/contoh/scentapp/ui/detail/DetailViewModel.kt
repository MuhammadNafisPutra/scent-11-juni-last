package com.contoh.scentapp.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contoh.scentapp.domain.model.CartItem
import com.contoh.scentapp.ui.state.DetailUiState
import com.contoh.scentapp.domain.model.Product
import com.contoh.scentapp.domain.model.SizeOption
import com.contoh.scentapp.domain.usecase.product.GetProductDetailUseCase
import com.contoh.scentapp.domain.usecase.product.GetProductReviewsUseCase
import com.contoh.scentapp.domain.usecase.cart.AddToCartUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailViewModel(
    private val firestoreId: String,
    private val getProductDetailUseCase: GetProductDetailUseCase,
    private val getProductReviewsUseCase: GetProductReviewsUseCase,
    private val addToCartUseCase: AddToCartUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private var fullPrice   : Long = 0L
    private var decantPrice : Long = 0L

    init {
        loadDetail()
        loadReviews()
    }

    private fun loadDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getProductDetailUseCase(firestoreId)
                .onSuccess { parfum ->
                    fullPrice   = parfum.price
                    decantPrice = parfum.decantPrice

                    val product = Product(
                        id           = parfum.id.hashCode(),
                        firestoreId  = parfum.id,
                        sellerId     = parfum.sellerId,
                        brand        = parfum.brand,
                        name         = parfum.name,
                        price        = "Rp${"%,d".format(parfum.price).replace(',', '.')}",
                        volume       = "${parfum.sizes.firstOrNull() ?: 50}ml",
                        imageUrl     = parfum.imageUrl,
                        cardColor    = 0xFF1A1A1A,
                        accentColor  = 0xFFD4A853,
                        collection   = parfum.olfactoryFamily.uppercase(),
                        fullBrand    = parfum.brand,
                        description  = parfum.description,
                        aromaProfile = parfum.topNotes.ifEmpty { listOf(parfum.olfactoryFamily) },
                        rating       = if (parfum.avgLongevity > 0f) parfum.avgLongevity else 4.8f,
                        reviewCount  = parfum.reviewCount
                    )

                    val sizeOptions = mutableListOf(
                        SizeOption(
                            id    = "full",
                            label = "UKURAN PENUH",
                            size  = "${parfum.sizes.lastOrNull() ?: 50}ML",
                            price = "/ Rp${"%,d".format(parfum.price).replace(',', '.')}"
                        )
                    ).apply {
                        if (parfum.isDecantAvailable && parfum.decantPrice > 0) {
                            add(
                                SizeOption(
                                    id    = "decant",
                                    label = "DECANT",
                                    size  = "10ML",
                                    price = "/ Rp${"%,d".format(parfum.decantPrice).replace(',', '.')}"
                                )
                            )
                        }
                    }

                    _uiState.update {
                        it.copy(
                            isLoading    = false,
                            product      = product,
                            sizeOptions  = sizeOptions,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading    = false,
                            errorMessage = error.message ?: "Produk tidak ditemukan"
                        )
                    }
                }
        }
    }

    private fun loadReviews() {
        viewModelScope.launch {
            getProductReviewsUseCase(firestoreId)
                .catch { /* abaikan error */ }
                .collect { reviews ->
                    _uiState.update { it.copy(reviews = reviews) }
                }
        }
    }

    fun onSizeSelected(sizeId: String) {
        _uiState.update { it.copy(selectedSizeId = sizeId) }
    }

    fun addToCart() {
        val state   = _uiState.value
        val product = state.product ?: return
        val sizeId  = state.selectedSizeId
        val sizeOption = state.sizeOptions.find { it.id == sizeId }

        val pricePerItem = when (sizeId) {
            "decant" -> decantPrice
            else     -> fullPrice
        }.toInt()

        val volumeLabel = sizeOption?.size?.lowercase() ?: product.volume
        val isDecant     = sizeId == "decant"

        val cartProductId = "${product.firestoreId}_$sizeId".hashCode()

        addToCartUseCase(
            CartItem(
                productId    = cartProductId,
                firestoreId  = product.firestoreId,
                sellerId     = product.sellerId,
                name         = product.name,
                brand        = product.brand,
                aromaProfile = product.aromaProfile.joinToString(", "),
                imageUrl     = product.imageUrl,
                volume       = volumeLabel,
                isDecant     = isDecant,
                pricePerItem = pricePerItem,
                cardColor    = product.cardColor,
                accentColor  = product.accentColor
            )
        )
    }
}

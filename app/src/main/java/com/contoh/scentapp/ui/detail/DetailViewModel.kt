package com.contoh.scentapp.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.contoh.scentapp.data.model.CartItem
import com.contoh.scentapp.data.model.DetailUiState
import com.contoh.scentapp.data.model.Product
import com.contoh.scentapp.data.model.SizeOption
import com.contoh.scentapp.data.repository.CartRepository
import com.contoh.scentapp.data.repository.ProductRepositoryImpl
import com.contoh.scentapp.data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailViewModel(
    private val firestoreId     : String,
    private val repository      : ProductRepositoryImpl = ProductRepositoryImpl(),
    private val cartRepository  : CartRepository         = CartRepository.getInstance(),
    private val reviewRepository: ReviewRepository       = ReviewRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    // Harga mentah (Rupiah) per opsi ukuran — dipakai saat "Tambah ke Keranjang"
    // agar harga di keranjang & subtotal selalu sesuai ukuran yang dipilih.
    private var fullPrice   : Long = 0L
    private var decantPrice : Long = 0L

    init {
        loadDetail()
        loadReviews()
    }

    private fun loadDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.getParfumById(firestoreId)
                .onSuccess { parfum ->
                    fullPrice   = parfum.price
                    decantPrice = parfum.decantPrice

                    // Mapping Parfum (Firestore) → Product (UI model)
                    val product = Product(
                        id           = parfum.id.hashCode(),
                        firestoreId  = parfum.id,
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

                    // Size options: tampilkan Full Size, tambah Decant jika tersedia
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

    /**
     * Ambil ulasan secara realtime dari Firestore (parfums/{id}/reviews),
     * sehingga ulasan yang baru ditulis langsung muncul di halaman detail.
     */
    private fun loadReviews() {
        viewModelScope.launch {
            reviewRepository.getReviews(firestoreId)
                .catch { /* abaikan error stream ulasan, tidak menghalangi detail produk */ }
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

        // Harga per item HARUS mengikuti ukuran yang dipilih (Full / Decant),
        // bukan selalu harga ukuran penuh — ini sumber subtotal yang tidak sesuai.
        val pricePerItem = when (sizeId) {
            "decant" -> decantPrice
            else     -> fullPrice
        }.toInt()

        val volumeLabel = sizeOption?.size?.lowercase() ?: product.volume
        val isDecant     = sizeId == "decant"

        // productId berbeda per varian ukuran agar Full & Decant dari produk
        // yang sama tidak tergabung jadi satu baris keranjang dengan harga keliru.
        val cartProductId = "${product.firestoreId}_$sizeId".hashCode()

        cartRepository.addToCart(
            CartItem(
                productId    = cartProductId,
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

    class DetailViewModelFactory(private val firestoreId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
                return DetailViewModel(firestoreId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

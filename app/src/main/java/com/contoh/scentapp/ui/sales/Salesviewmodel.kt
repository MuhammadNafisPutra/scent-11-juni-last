package com.contoh.scentapp.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contoh.scentapp.domain.model.ActiveOrder
import com.contoh.scentapp.domain.model.OrderStatus
import com.contoh.scentapp.domain.model.SalesProduct
import com.contoh.scentapp.ui.state.SalesUiState
import com.contoh.scentapp.domain.usecase.product.GetSellerProductsUseCase
import com.contoh.scentapp.domain.usecase.product.DeleteProductUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.map

class SalesViewModel(
    private val getSellerProductsUseCase: GetSellerProductsUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val orderRepository: com.contoh.scentapp.domain.OrderRepository,
    private val authRepository: com.contoh.scentapp.domain.AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    private val _resiDialogOrderId = MutableStateFlow<String?>(null)
    val resiDialogOrderId: StateFlow<String?> = _resiDialogOrderId.asStateFlow()

    init { loadData() }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            launch {
                getSellerProductsUseCase()
                    .catch { e ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                    }
                    .collect { parfumList ->
                        val products = parfumList.map { parfum ->
                            SalesProduct(
                                id          = parfum.id.hashCode(),
                                firestoreId = parfum.id,
                                name        = parfum.name.uppercase(),
                                aromaFamily = parfum.olfactoryFamily.uppercase(),
                                volume      = "${parfum.sizes.firstOrNull() ?: 50}ML",
                                stockStatus = when {
                                    parfum.stock <= 0 -> "HABIS"
                                    parfum.stock <= 5 -> "STOK MENIPIS"
                                    else              -> "TERSEDIA"
                                },
                                price       = parfum.price.toInt(),
                                stock       = parfum.stock,
                                imageUrl    = parfum.imageUrl,
                                cardColor   = 0xFF1A1A1A,
                                accentColor = 0xFFD4A853
                            )
                        }
                        _uiState.update { state ->
                            state.copy(
                                isLoading    = false,
                                products     = products
                            )
                        }
                    }
            }
            
            launch {
                val uid = authRepository.currentUserId
                if (uid != null) {
                    orderRepository.getSellerOrders(uid)
                        .catch { /* Handle error silently or update state */ }
                        .collect { orders ->
                            val activeOrders = orders.map { 
                                ActiveOrder(
                                    orderId = it.id, 
                                    buyerName = "Pembeli #${it.buyerId.take(5)}", 
                                    itemCount = it.items.size, 
                                    totalPrice = it.totalPrice, 
                                    status = it.status,
                                    paymentMethod = it.paymentMethod,
                                    noResi = ""
                                )
                            }
                            _uiState.update { it.copy(activeOrders = activeOrders) }
                        }
                }
            }
        }
    }

    fun deleteProduct(productId: Int) {
        val product = _uiState.value.products.find { it.id == productId } ?: return
        viewModelScope.launch {
            val result = deleteProductUseCase(product.firestoreId)
            if (result.isFailure) {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun konfirmasiPembayaran(orderId: String) {
        _uiState.update { state ->
            state.copy(
                activeOrders = state.activeOrders.map {
                    if (it.orderId == orderId) it.copy(status = OrderStatus.PEMBAYARAN_DIKONFIRMASI)
                    else it
                }
            )
        }
    }

    fun markAsPacked(orderId: String) {
        _uiState.update { state ->
            state.copy(
                activeOrders = state.activeOrders.map {
                    if (it.orderId == orderId) it.copy(status = OrderStatus.DIKEMAS) else it
                }
            )
        }
    }

    fun markAsShipped(orderId: String) {
        openResiDialog(orderId)
    }

    fun openResiDialog(orderId: String) {
        _resiDialogOrderId.value = orderId
    }

    fun closeResiDialog() {
        _resiDialogOrderId.value = null
    }

    fun inputResiDanKirim(orderId: String, noResi: String) {
        _uiState.update { state ->
            state.copy(
                activeOrders = state.activeOrders.map {
                    if (it.orderId == orderId) it.copy(status = OrderStatus.DIKIRIM, noResi = noResi)
                    else it
                }
            )
        }
        _resiDialogOrderId.value = null
    }

    fun updateStatus(orderId: String, status: OrderStatus) {
        _uiState.update { state ->
            state.copy(
                activeOrders = state.activeOrders.map {
                    if (it.orderId == orderId) it.copy(status = status) else it
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
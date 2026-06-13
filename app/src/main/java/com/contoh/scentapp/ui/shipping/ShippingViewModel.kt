package com.contoh.scentapp.ui.shipping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contoh.scentapp.data.repository.CartRepository
import com.contoh.scentapp.data.repository.ShippingRepository
import com.contoh.scentapp.domain.model.ShippingOption
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShippingViewModel(
    private val shippingRepository: ShippingRepository = ShippingRepository.getInstance(),
    private val cartRepository: CartRepository = CartRepository.getInstance()
) : ViewModel() {

    private val _shippingOptions = MutableStateFlow<List<ShippingOption>>(cartRepository.shippingOptions)
    val shippingOptions: StateFlow<List<ShippingOption>> = _shippingOptions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchDynamicShippingCosts()
    }

    private fun fetchDynamicShippingCosts() {
        val destinationId = shippingRepository.selectedDestinationCityId ?: return
        // We set Banjarmasin as origin. Banjarmasin ID in BinderByte depends on the API. 
        // As a fallback, we will just pass "Banjarmasin" as string, or if it requires ID, we use a placeholder ID like "43"
        // In this implementation we will try using the destination ID and "Banjarmasin".
        val originId = "Banjarmasin" // You can adjust this if exact ID is known.
        
        viewModelScope.launch {
            _isLoading.value = true
            
            // Fetch for 3 couriers: jnt, sicepat, jne
            val couriers = listOf(
                Pair("jnt", "J&T Express"),
                Pair("sicepat", "SiCepat"),
                Pair("jne", "JNE REG")
            )
            
            try {
                val results = couriers.map { courier ->
                    async {
                        val result = shippingRepository.getShippingCost(
                            courier = courier.first,
                            originCityId = originId,
                            destinationCityId = destinationId
                        )
                        if (result.isSuccess) {
                            val details = result.getOrNull()
                            val firstDetail = details?.firstOrNull()
                            val firstPrice = firstDetail?.cost?.firstOrNull()
                            
                            if (firstPrice != null) {
                                ShippingOption(
                                    id = courier.first,
                                    name = courier.second,
                                    badge = "REGULAR",
                                    estimasi = "Estimasi: ${firstPrice.etd}",
                                    price = firstPrice.value,
                                    iconType = if (courier.first == "jnt") "truck" else if (courier.first == "sicepat") "lightning" else "plane"
                                )
                            } else null
                        } else null
                    }
                }.awaitAll()
                
                val validOptions = results.filterNotNull()
                if (validOptions.isNotEmpty()) {
                    _shippingOptions.value = validOptions
                }
            } catch (e: Exception) {
                // Keep default if error
            } finally {
                _isLoading.value = false
            }
        }
    }
}

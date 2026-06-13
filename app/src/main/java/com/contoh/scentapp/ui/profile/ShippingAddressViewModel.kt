package com.contoh.scentapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contoh.scentapp.data.remote.dto.CityDto
import com.contoh.scentapp.data.remote.dto.ProvinceDto
import com.contoh.scentapp.data.repository.ShippingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShippingAddressViewModel(
    private val shippingRepository: ShippingRepository = ShippingRepository.getInstance()
) : ViewModel() {

    private val _provinces = MutableStateFlow<List<ProvinceDto>>(emptyList())
    val provinces: StateFlow<List<ProvinceDto>> = _provinces.asStateFlow()

    private val _cities = MutableStateFlow<List<CityDto>>(emptyList())
    val cities: StateFlow<List<CityDto>> = _cities.asStateFlow()

    private val _isLoadingProvinces = MutableStateFlow(false)
    val isLoadingProvinces: StateFlow<Boolean> = _isLoadingProvinces.asStateFlow()

    private val _isLoadingCities = MutableStateFlow(false)
    val isLoadingCities: StateFlow<Boolean> = _isLoadingCities.asStateFlow()

    init {
        fetchProvinces()
    }

    private fun fetchProvinces() {
        viewModelScope.launch {
            _isLoadingProvinces.value = true
            shippingRepository.getProvinces().onSuccess {
                _provinces.value = it
            }.onFailure {
                // handle error
            }
            _isLoadingProvinces.value = false
        }
    }

    fun fetchCities(provinceId: String) {
        viewModelScope.launch {
            _isLoadingCities.value = true
            shippingRepository.getCities(provinceId).onSuccess {
                _cities.value = it
            }.onFailure {
                // handle error
            }
            _isLoadingCities.value = false
        }
    }

    fun saveDestinationCity(cityId: String) {
        shippingRepository.selectedDestinationCityId = cityId
    }
}

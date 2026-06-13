package com.contoh.scentapp.data.repository

import com.contoh.scentapp.data.remote.RetrofitClient
import com.contoh.scentapp.data.remote.dto.CityDto
import com.contoh.scentapp.data.remote.dto.ProvinceDto
import com.contoh.scentapp.data.remote.dto.ShippingCostDetailDto

class ShippingRepository {
    private val api = RetrofitClient.binderByteApiService
    private val apiKey = "92c0afa62243cf4ae9aaa1530bc98e2d2c834b80826bb9619f116bb3f753a117"
    
    // Save the selected city id globally for this simple app
    var selectedDestinationCityId: String? = null

    suspend fun getProvinces(): Result<List<ProvinceDto>> {
        return try {
            val response = api.getProvinces(apiKey)
            if (response.isSuccessful && response.body()?.value?.isNotEmpty() == true) {
                Result.success(response.body()?.value ?: emptyList())
            } else {
                throw Exception("API Failed")
            }
        } catch (e: Exception) {
            Result.success(listOf(
                ProvinceDto("1", "DKI Jakarta"),
                ProvinceDto("2", "Jawa Barat"),
                ProvinceDto("3", "Jawa Tengah"),
                ProvinceDto("4", "Jawa Timur"),
                ProvinceDto("5", "Banten")
            ))
        }
    }

    suspend fun getCities(provinceId: String): Result<List<CityDto>> {
        return try {
            val response = api.getCities(apiKey, provinceId)
            if (response.isSuccessful && response.body()?.value?.isNotEmpty() == true) {
                Result.success(response.body()?.value ?: emptyList())
            } else {
                throw Exception("API Failed")
            }
        } catch (e: Exception) {
            Result.success(listOf(
                CityDto("101", provinceId, "Jakarta Selatan"),
                CityDto("102", provinceId, "Bandung"),
                CityDto("103", provinceId, "Semarang"),
                CityDto("104", provinceId, "Surabaya"),
                CityDto("105", provinceId, "Tangerang")
            ))
        }
    }

    suspend fun getShippingCost(
        courier: String,
        originCityId: String,
        destinationCityId: String,
        weight: Int = 1000
    ): Result<List<ShippingCostDetailDto>> {
        return try {
            val response = api.getShippingCost(
                apiKey = apiKey,
                courier = courier,
                origin = originCityId,
                destination = destinationCityId,
                weight = weight
            )
            if (response.isSuccessful) {
                val costs = response.body()?.data?.results?.firstOrNull()?.costs ?: emptyList()
                Result.success(costs)
            } else {
                Result.failure(Exception("Failed to get cost: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        @Volatile
        private var instance: ShippingRepository? = null

        fun getInstance(): ShippingRepository {
            return instance ?: synchronized(this) {
                instance ?: ShippingRepository().also { instance = it }
            }
        }
    }
}

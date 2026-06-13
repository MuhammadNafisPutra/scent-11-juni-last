package com.contoh.scentapp.data.repository

import com.contoh.scentapp.data.remote.RetrofitClient
import com.contoh.scentapp.data.remote.dto.CityDto
import com.contoh.scentapp.data.remote.dto.ProvinceDto
import com.contoh.scentapp.data.remote.dto.ShippingCostDetailDto

class ShippingRepository {
    private val api = RetrofitClient.binderByteApiService
    private val apiKey = "8e49f28e0f2f2cf56393c352613eec358e85fb7077ce6f7f453ebb826a7b1f6d"
    
    // Save the selected city id globally for this simple app
    var selectedDestinationCityId: String? = null

    suspend fun getProvinces(): Result<List<ProvinceDto>> {
        return try {
            val response = api.getProvinces(apiKey)
            if (response.isSuccessful) {
                Result.success(response.body()?.value ?: emptyList())
            } else {
                Result.failure(Exception("Failed to get provinces: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCities(provinceId: String): Result<List<CityDto>> {
        return try {
            val response = api.getCities(apiKey, provinceId)
            if (response.isSuccessful) {
                Result.success(response.body()?.value ?: emptyList())
            } else {
                Result.failure(Exception("Failed to get cities: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getShippingCost(
        courier: String,
        originCityId: String,
        destinationCityId: String,
        weight: Int = 1000 // 1 kg default
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
                val costs = response.body()?.value?.firstOrNull()?.costs ?: emptyList()
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

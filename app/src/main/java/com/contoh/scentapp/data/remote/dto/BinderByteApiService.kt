package com.contoh.scentapp.data.remote.dto

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BinderByteApiService {
    
    @GET("wilayah/provinsi")
    suspend fun getProvinces(
        @Query("api_key") apiKey: String
    ): Response<BinderByteBaseResponse<List<ProvinceDto>>>

    @GET("wilayah/kabupaten")
    suspend fun getCities(
        @Query("api_key") apiKey: String,
        @Query("id_provinsi") idProvinsi: String
    ): Response<BinderByteBaseResponse<List<CityDto>>>

    @GET("v1/cost")
    suspend fun getShippingCost(
        @Query("api_key") apiKey: String,
        @Query("courier") courier: String,
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("weight") weight: Int
    ): Response<ShippingCostResponseDto>
}

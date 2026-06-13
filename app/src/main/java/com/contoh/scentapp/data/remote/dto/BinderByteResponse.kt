package com.contoh.scentapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BinderByteBaseResponse<T>(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("value")
    val value: T?
)

data class ProvinceDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String
)

data class CityDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("id_provinsi")
    val idProvinsi: String,
    @SerializedName("name")
    val name: String
)

data class ShippingCostRequest(
    @SerializedName("origin")
    val origin: String,
    @SerializedName("destination")
    val destination: String,
    @SerializedName("weight")
    val weight: Int,
    @SerializedName("courier")
    val courier: String
)

data class ShippingCostDto(
    @SerializedName("code")
    val code: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("costs")
    val costs: List<ShippingCostDetailDto>
)

data class ShippingCostDetailDto(
    @SerializedName("service")
    val service: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("cost")
    val cost: List<ShippingCostPriceDto>
)

data class ShippingCostPriceDto(
    @SerializedName("value")
    val value: Int,
    @SerializedName("etd")
    val etd: String,
    @SerializedName("note")
    val note: String
)

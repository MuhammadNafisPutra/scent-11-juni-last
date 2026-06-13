package com.contoh.scentapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BinderByteBaseResponse<T>(
    @SerializedName("code")
    val code: String,
    @SerializedName("messages")
    val messages: String,
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
    @SerializedName("type")
    val type: String,
    @SerializedName("price")
    val price: String,
    @SerializedName("estimated")
    val estimated: String
)

data class ShippingCostResponseDto(
    @SerializedName("code")
    val code: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: ShippingCostDataDto?
)

data class ShippingCostDataDto(
    @SerializedName("origin")
    val origin: LocationInfoDto,
    @SerializedName("destination")
    val destination: LocationInfoDto,
    @SerializedName("weight")
    val weight: String,
    @SerializedName("results")
    val results: List<ShippingCostDto>
)

data class LocationInfoDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("label")
    val label: String
)
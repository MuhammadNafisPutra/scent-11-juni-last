package com.contoh.scentapp.data.model

data class ShippingOption(
    val id       : String,
    val name     : String,
    val badge    : String,
    val estimasi : String,
    val price    : Int,
    val iconType : String
) {
    val formattedPrice: String get() =
        if (price == 0) "GRATIS"
        else "Rp ${"%,d".format(price).replace(",", ".")}"
}
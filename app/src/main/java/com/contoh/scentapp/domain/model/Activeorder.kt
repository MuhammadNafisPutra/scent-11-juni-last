package com.contoh.scentapp.domain.model

data class ActiveOrder(
    val orderId       : String      = "",
    val buyerName     : String      = "",
    val itemCount     : Int         = 0,
    val status        : OrderStatus = OrderStatus.MENUNGGU_KONFIRMASI,
    val paymentMethod : String      = "",
    val noResi        : String      = "",
    val totalPrice    : Long        = 0L
)

package com.contoh.scentapp.data.model

data class Order(
    val id              : String          = "",
    val buyerId         : String          = "",
    val sellerId        : String          = "",
    val items           : List<CartItem>  = emptyList(),
    val totalPrice      : Long            = 0L,
    val shippingCost    : Long            = 0L,
    val shippingAddress : String          = "",
    val courier         : String          = "",
    val paymentMethod   : String          = "",
    val status          : OrderStatus     = OrderStatus.WAITING_PAYMENT,
    val createdAt       : Long            = 0L
)


enum class OrderStatus(val label: String) {
    WAITING_PAYMENT("Menunggu Pembayaran"),
    PAID("Dibayar"),
    DALAM_PROSES("Dalam Proses"),
    DIKEMAS("Dikemas"),
    DIKIRIM("Dikirim"),
    DELIVERED("Diterima"),
    CANCELLED("Dibatalkan"),

    MENUNGGU_KONFIRMASI("Menunggu Konfirmasi"),
    BUKTI_DIKIRIM("Bukti Pembayaran Dikirim"),
    PEMBAYARAN_DIKONFIRMASI("Pembayaran Dikonfirmasi"),
    SIAP_DIKIRIM("Siap Dikirim"),
    TIDAK_SAMPAI("Tidak Sampai"),
    SELESAI("Selesai")
}
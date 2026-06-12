package com.contoh.scentapp.data.model

data class SalesUiState(
    val isLoading      : Boolean          = true,
    val products       : List<SalesProduct> = emptyList(),
    val activeOrders   : List<ActiveOrder>  = emptyList(),
    val errorMessage   : String?            = null
) {
    // Total pendapatan dari semua produk × stok terjual (estimasi sederhana)
    val totalPendapatan: Long
        get() = products.sumOf { it.price.toLong() * (it.stock.coerceAtLeast(1)) }

    val formattedPendapatan: String
        get() = "Rp${"%,d".format(totalPendapatan).replace(',', '.')}"

    val totalPenjualan: Int
        get() = products.sumOf { it.stock }

    val growthPercent: String
        get() = "+12.4% dari bulan lalu"
}
package com.contoh.scentapp.ui.order

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.contoh.scentapp.domain.model.OrderStatus
import com.contoh.scentapp.ui.theme.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.contoh.scentapp.domain.model.Order
import com.contoh.scentapp.data.repository.OrderRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Tampilan ringkas satu pesanan, dipetakan dari dokumen [Order] di Firestore. */
private data class OrderHistoryDisplay(
    val id            : String,
    val productName   : String,
    val volume        : String,
    val totalStr      : String,
    val date          : String,
    val status        : OrderStatus,
    val paymentMethod : String
)

private fun Order.toDisplay(): OrderHistoryDisplay {
    val firstItem  = items.firstOrNull()
    val productName = when {
        firstItem == null      -> "Pesanan"
        items.size > 1          -> "${firstItem.name} & ${items.size - 1} lainnya"
        else                     -> firstItem.name
    }
    val volume = firstItem?.volume ?: ""
    val total  = totalPrice + shippingCost
    val totalStr = "Rp${"%,d".format(total).replace(',', '.')}"
    val date = if (createdAt > 0L) {
        SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(createdAt))
    } else ""
    val paymentMethod = if (this.paymentMethod.equals("transfer", ignoreCase = true)) "Transfer" else "COD"

    return OrderHistoryDisplay(
        id            = id,
        productName   = productName,
        volume        = volume,
        totalStr      = totalStr,
        date          = date,
        status        = status,
        paymentMethod = paymentMethod
    )
}

class OrderHistoryViewModel(
    private val repository: OrderRepositoryImpl = OrderRepositoryImpl()
) : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        val buyerId = repository.currentUserId
        if (buyerId == null) {
            _isLoading.value = false
        } else {
            viewModelScope.launch {
                repository.getBuyerOrders(buyerId)
                    .catch { _isLoading.value = false }
                    .collect { list ->
                        _orders.update { list }
                        _isLoading.value = false
                    }
            }
        }
    }
}

class OrderHistoryViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderHistoryViewModel::class.java)) {
            return OrderHistoryViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun OrderHistoryScreen(
    onBack             : () -> Unit,
    onOrderDetailClick : (String) -> Unit,
    viewModel          : OrderHistoryViewModel = viewModel(factory = OrderHistoryViewModelFactory())
) {
    val tabs = listOf("Semua", "Belum Bayar", "Diproses", "Dikirim", "Selesai", "Batal")
    var selectedTab by remember { mutableStateOf("Semua") }

    val orders     by viewModel.orders.collectAsStateWithLifecycle()
    val isLoading  by viewModel.isLoading.collectAsStateWithLifecycle()
    val allOrders  = orders.map { it.toDisplay() }

    val filteredOrders = when (selectedTab) {
        "Belum Bayar" -> allOrders.filter { it.status == OrderStatus.WAITING_PAYMENT || it.status == OrderStatus.MENUNGGU_KONFIRMASI }
        "Diproses"    -> allOrders.filter { it.status in listOf(OrderStatus.DALAM_PROSES, OrderStatus.DIKEMAS) }
        "Dikirim"     -> allOrders.filter { it.status == OrderStatus.DIKIRIM }
        "Selesai"     -> allOrders.filter { it.status in listOf(OrderStatus.DELIVERED, OrderStatus.SELESAI) }
        "Batal"       -> allOrders.filter { it.status == OrderStatus.CANCELLED }
        else          -> allOrders
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top Bar ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp).clickable(onClick = onBack))
                Text("RIWAYAT PESANAN", style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold, letterSpacing = 2.sp), color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.size(24.dp))
            }

            // ── Tab Filter ────────────────────────────────────────────────────
            LazyRow(
                contentPadding         = PaddingValues(horizontal = 20.dp),
                horizontalArrangement  = Arrangement.spacedBy(8.dp),
                modifier               = Modifier.padding(bottom = 16.dp)
            ) {
                items(tabs) { tab ->
                    val isSelected = tab == selectedTab
                    val count = when (tab) {
                        "Semua" -> allOrders.size
                        "Belum Bayar" -> allOrders.count { it.status == OrderStatus.WAITING_PAYMENT || it.status == OrderStatus.MENUNGGU_KONFIRMASI }
                        "Diproses"    -> allOrders.count { it.status in listOf(OrderStatus.DALAM_PROSES, OrderStatus.DIKEMAS) }
                        "Dikirim"     -> allOrders.count { it.status == OrderStatus.DIKIRIM }
                        "Selesai"     -> allOrders.count { it.status in listOf(OrderStatus.DELIVERED, OrderStatus.SELESAI) }
                        "Batal"       -> allOrders.count { it.status == OrderStatus.CANCELLED }
                        else -> 0
                    }
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.outlineVariant else Color.Transparent, RoundedCornerShape(20.dp))
                            .clickable { selectedTab = tab }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(tab, color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal))
                            if (count > 0 && tab != "Semua") {
                                Spacer(Modifier.width(4.dp))
                                Box(modifier = Modifier.size(16.dp).clip(RoundedCornerShape(8.dp)).background(ScentGold.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                    Text("$count", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, color = ScentGold))
                                }
                            }
                        }
                    }
                }
            }

            // ── Order List ────────────────────────────────────────────────────
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ScentGold)
                }
            } else if (filteredOrders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ShoppingBag, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Tidak ada pesanan", style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)))
                    }
                }
            } else {
                LazyColumn(
                    contentPadding        = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement   = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredOrders, key = { it.id }) { order ->
                        OrderHistoryCard(
                            order   = order,
                            onClick = { onOrderDetailClick(order.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderHistoryCard(order: OrderHistoryDisplay, onClick: () -> Unit) {
    val (statusColor, statusBg) = statusStyle(order.status)
    val statusIcon = statusIcon(order.status)

    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface).border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(16.dp)
    ) {
        // Header row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(order.date, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), fontSize = 10.sp))
            Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(statusBg).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(10.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(order.status.label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.5.sp, color = statusColor, fontWeight = FontWeight.Bold))
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Product info
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant).border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.LocalFlorist, null, tint = ScentGold.copy(alpha = 0.6f), modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("#SCNT-${order.id}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)))
                Spacer(Modifier.height(3.dp))
                Text(order.productName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp), color = MaterialTheme.colorScheme.onBackground)
                Text("${order.volume} • ${order.paymentMethod}", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), fontSize = 11.sp))
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Total Pembayaran", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)))
            Text(order.totalStr, style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold))
        }

        // Action button for DIKIRIM orders
        if (order.status == OrderStatus.DIKIRIM) {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.onBackground).padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("LIHAT DETAIL & KONFIRMASI", style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.background))
            }
        }
    }
}

private fun statusStyle(status: OrderStatus): Pair<Color, Color> = when (status) {
    OrderStatus.MENUNGGU_KONFIRMASI, OrderStatus.WAITING_PAYMENT -> Pair(Color(0xFFD4A853), Color(0xFFD4A853).copy(alpha = 0.1f))
    OrderStatus.DALAM_PROSES, OrderStatus.DIKEMAS               -> Pair(Color(0xFF2196F3), Color(0xFF2196F3).copy(alpha = 0.1f))
    OrderStatus.DIKIRIM                                          -> Pair(Color(0xFF9C27B0), Color(0xFF9C27B0).copy(alpha = 0.1f))
    OrderStatus.DELIVERED, OrderStatus.SELESAI                  -> Pair(Color(0xFF4CAF50), Color(0xFF4CAF50).copy(alpha = 0.1f))
    OrderStatus.CANCELLED, OrderStatus.TIDAK_SAMPAI             -> Pair(Color(0xFFCF6679), Color(0xFFCF6679).copy(alpha = 0.1f))
    else -> Pair(Color(0xFFA0A0A0), Color(0xFF1E1E1E))
}

private fun statusIcon(status: OrderStatus): ImageVector = when (status) {
    OrderStatus.MENUNGGU_KONFIRMASI                              -> Icons.Default.HourglassEmpty
    OrderStatus.DALAM_PROSES, OrderStatus.DIKEMAS               -> Icons.Default.Inventory
    OrderStatus.DIKIRIM                                          -> Icons.Default.LocalShipping
    OrderStatus.DELIVERED, OrderStatus.SELESAI                  -> Icons.Default.CheckCircle
    OrderStatus.CANCELLED, OrderStatus.TIDAK_SAMPAI             -> Icons.Default.Cancel
    else                                                         -> Icons.Default.ShoppingBag
}

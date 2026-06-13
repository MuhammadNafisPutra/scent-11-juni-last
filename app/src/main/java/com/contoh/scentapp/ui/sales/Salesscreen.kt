package com.contoh.scentapp.ui.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.contoh.scentapp.data.model.ActiveOrder
import com.contoh.scentapp.data.model.OrderStatus
import com.contoh.scentapp.data.model.SalesProduct
import com.contoh.scentapp.ui.theme.*

@Composable
fun SalesScreen(
    onBack         : () -> Unit = {},
    onAddProduct   : () -> Unit = {},
    onEditProduct  : (String) -> Unit = {},
    onOrderClick   : (String) -> Unit = {},
    viewModel      : SalesViewModel = viewModel(factory = SalesViewModelFactory())
) {
    val uiState      by viewModel.uiState.collectAsStateWithLifecycle()
    val resiDialogId by viewModel.resiDialogOrderId.collectAsStateWithLifecycle()
    val listState     = rememberLazyListState()

    // State untuk dialog konfirmasi hapus
    var deleteConfirmProductId by remember { mutableStateOf<Int?>(null) }
    var deleteConfirmProductName by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            state          = listState,
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            item(key = "topbar") {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, "Kembali",
                        tint     = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp).clickable(onClick = onBack)
                    )
                    Text(
                        "SCENT",
                        style = MaterialTheme.typography.titleLarge.copy(
                            letterSpacing = 6.sp, fontSize = 18.sp, fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Icon(
                        Icons.Default.ShoppingBag, null,
                        tint     = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            item(key = "header") {
                Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 20.dp)) {
                    Text(
                        "PERFORMA ATELIER",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Tinjauan komprehensif kreasi olfaktori dan logistik pesanan Anda.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            lineHeight = 18.sp
                        )
                    )
                    Spacer(Modifier.height(20.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                            .clickable(onClick = onAddProduct).padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "TAMBAH PRODUK BARU",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp, letterSpacing = 2.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                        }
                    }
                }
            }

            item(key = "stats") {
                Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(label = "TOTAL PENDAPATAN", value = uiState.formattedPendapatan, subLabel = uiState.growthPercent, isLarge = true)
                    StatCard(label = "TOTAL PENJUALAN",  value = "%,d".format(uiState.totalPenjualan).replace(",", "."), isLarge = false)
                }
                Spacer(Modifier.height(28.dp))
            }

            item(key = "orders_header") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        "PESANAN MASUK",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "${uiState.activeOrders.size} PESANAN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp, letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                            )
                        )
                    }
                }
            }

            items(items = uiState.activeOrders, key = { "order_${it.orderId}" }) { order ->
                ActiveOrderCard(
                    order                 = order,
                    onCardClick           = { onOrderClick(order.orderId) },
                    onKonfirmasiPembayaran = { viewModel.konfirmasiPembayaran(order.orderId) },
                    onMarkPacked          = { viewModel.markAsPacked(order.orderId) },
                    onMarkShipped         = { viewModel.markAsShipped(order.orderId) },
                    modifier              = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            if (uiState.activeOrders.isEmpty() && !uiState.isLoading) {
                item(key = "orders_empty") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "Belum ada pesanan masuk",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                )
                            )
                        }
                    }
                }
            }

            item(key = "koleksi_header") {
                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        "KOLEKSI",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.FilterList, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), modifier = Modifier.size(22.dp))
                        Icon(Icons.Default.Search,     null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), modifier = Modifier.size(22.dp))
                    }
                }
            }

            if (uiState.products.isEmpty() && !uiState.isLoading) {
                item(key = "koleksi_empty") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Inventory2,
                                contentDescription = null,
                                tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Belum ada produk",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                )
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Tambah produk baru untuk mulai berjualan",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                )
                            )
                        }
                    }
                }
            }

            items(items = uiState.products, key = { "sales_product_${it.id}" }) { product ->
                SalesProductItem(
                    product  = product,
                    onEdit   = { onEditProduct(product.firestoreId) },
                    onDelete = {
                        deleteConfirmProductId   = product.id
                        deleteConfirmProductName = product.name
                    },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                HorizontalDivider(
                    color    = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }

    resiDialogId?.let { orderId ->
        ResiInputDialog(
            orderId   = orderId,
            onConfirm = { noResi -> viewModel.inputResiDanKirim(orderId, noResi) },
            onDismiss = { viewModel.closeResiDialog() }
        )
    }

    // ── Dialog Konfirmasi Hapus ───────────────────────────────────────────────
    deleteConfirmProductId?.let { productId ->
        AlertDialog(
            onDismissRequest = { deleteConfirmProductId = null },
            containerColor   = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    "Hapus Produk",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    "Apakah kamu yakin ingin menghapus \"$deleteConfirmProductName\"? Tindakan ini tidak dapat dibatalkan.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteProduct(productId)
                    deleteConfirmProductId = null
                }) {
                    Text(
                        "HAPUS",
                        color = Color(0xFFE57373),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmProductId = null }) {
                    Text(
                        "BATAL",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                        )
                    )
                }
            }
        )
    }
}

// ── ResiInputDialog ───────────────────────────────────────────────────────────

@Composable
private fun ResiInputDialog(orderId: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var noResi by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "Input Nomor Resi",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column {
                Text(
                    "Pesanan #$orderId",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                )
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    BasicTextField(
                        value         = noResi,
                        onValueChange = { noResi = it },
                        textStyle     = MaterialTheme.typography.bodyMedium.copy(
                            color    = MaterialTheme.colorScheme.onBackground,
                            fontSize = 15.sp
                        ),
                        cursorBrush = SolidColor(ScentGold),
                        singleLine  = true,
                        modifier    = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (noResi.isEmpty()) Text(
                                "mis. JNE123456789",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                                )
                            )
                            inner()
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (noResi.isNotBlank()) onConfirm(noResi) }) {
                Text(
                    "KIRIM",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "BATAL",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                )
            }
        }
    )
}

// ── StatCard ──────────────────────────────────────────────────────────────────

@Composable
private fun StatCard(label: String, value: String, subLabel: String = "", isLarge: Boolean) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp, letterSpacing = 2.sp,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize   = if (isLarge) 34.sp else 28.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subLabel.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingUp, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(subLabel, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)))
                }
            }
        }
    }
}

// ── SalesProductItem — dengan AsyncImage ─────────────────────────────────────

@Composable
private fun SalesProductItem(
    product  : SalesProduct,
    onEdit   : () -> Unit,
    onDelete : () -> Unit,
    modifier : Modifier = Modifier
) {
    Row(
        modifier          = modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail: AsyncImage jika ada imageUrl, fallback ilustrasi botol
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(product.cardColor).copy(alpha = 0.9f),
                            Color(product.cardColor).copy(alpha = 0.5f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (product.imageUrl.isNotBlank()) {
                SubcomposeAsyncImage(
                    model             = product.imageUrl,
                    contentDescription = product.name,
                    contentScale      = ContentScale.Crop,
                    modifier          = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                    loading = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color    = ScentGold,
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = { BottleIllustration(product) }
                )
            } else {
                BottleIllustration(product)
            }
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                product.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${product.aromaFamily} • ${product.volume} • ${product.stockStatus}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (product.stockStatus == "STOK MENIPIS")
                        Color(0xFFD4A853)
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(
                    Icons.Default.Edit, "Edit",
                    tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.size(20.dp).clickable(onClick = onEdit)
                )
                Icon(
                    Icons.Default.Delete, "Hapus",
                    tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.size(20.dp).clickable(onClick = onDelete)
                )
            }
        }
    }
}

// Ilustrasi botol fallback (dipakai saat imageUrl kosong atau error)
@Composable
private fun BottleIllustration(product: SalesProduct) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(8.dp).height(6.dp)
                .background(Color(product.accentColor).copy(alpha = 0.5f))
        )
        Box(
            modifier = Modifier
                .width(28.dp).height(45.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(product.accentColor).copy(alpha = 0.2f))
                .border(0.5.dp, Color(product.accentColor).copy(alpha = 0.4f), RoundedCornerShape(3.dp))
        )
    }
}

// ── ActiveOrderCard ───────────────────────────────────────────────────────────

@Composable
private fun ActiveOrderCard(
    order                  : ActiveOrder,
    onCardClick            : () -> Unit,
    onKonfirmasiPembayaran : () -> Unit,
    onMarkPacked           : () -> Unit,
    onMarkShipped          : () -> Unit,
    modifier               : Modifier = Modifier
) {
    val statusColor = when (order.status) {
        OrderStatus.MENUNGGU_KONFIRMASI     -> Color(0xFFD4A853)
        OrderStatus.PEMBAYARAN_DIKONFIRMASI -> Color(0xFF4CAF50)
        OrderStatus.DIKEMAS                 -> Color(0xFF2196F3)
        OrderStatus.DIKIRIM                 -> Color(0xFF9C27B0)
        else                                -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
    }

    Column(
        modifier = modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .clickable(onClick = onCardClick)
            .padding(16.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "ORDER #${order.orderId}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp, letterSpacing = 1.5.sp,
                        color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${order.buyerName} • ${order.itemCount} Item",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Box(
                modifier = Modifier.clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    order.status.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp, letterSpacing = 1.sp, color = statusColor
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (order.paymentMethod == "Transfer") Icons.Default.AccountBalance else Icons.Default.Payments,
                null,
                tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                if (order.paymentMethod == "Transfer") "Transfer Bank" else "COD",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), fontSize = 11.sp
                )
            )
            if (order.noResi.isNotEmpty()) {
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Default.LocalShipping, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(order.noResi, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), fontSize = 11.sp))
            }
        }

        Spacer(Modifier.height(14.dp))

        when (order.status) {
            OrderStatus.MENUNGGU_KONFIRMASI -> {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                        .background(ScentGold.copy(alpha = 0.15f))
                        .border(1.dp, ScentGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable(onClick = onKonfirmasiPembayaran).padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "KONFIRMASI BAYAR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold, color = ScentGold
                        )
                    )
                }
            }
            OrderStatus.PEMBAYARAN_DIKONFIRMASI, OrderStatus.DALAM_PROSES -> {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            .clickable(onClick = onMarkPacked).padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("TANDAI DIKEMAS", style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold,
                            color    = MaterialTheme.colorScheme.onBackground))
                    }
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            .clickable(onClick = onMarkShipped).padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("INPUT RESI", style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold,
                            color    = MaterialTheme.colorScheme.onBackground))
                    }
                }
            }
            OrderStatus.DIKEMAS -> {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.onBackground)
                        .clickable(onClick = onMarkShipped).padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("INPUT RESI & KIRIM", style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold,
                        color    = MaterialTheme.colorScheme.background))
                }
            }
            OrderStatus.DIKIRIM -> {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer).padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("DALAM PENGIRIMAN", style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold,
                        color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)))
                }
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer).padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(order.status.label, style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold,
                        color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)))
                }
            }
        }
    }
}
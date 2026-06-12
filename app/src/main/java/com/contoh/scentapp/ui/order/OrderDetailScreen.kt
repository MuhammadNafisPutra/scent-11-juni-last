package com.contoh.scentapp.ui.order

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.contoh.scentapp.data.model.OrderStatus
import com.contoh.scentapp.ui.theme.*

@Composable
fun OrderDetailScreen(
    orderId : String,
    onBack  : () -> Unit
) {
    var status         by rememberSaveable { mutableStateOf(OrderStatus.DIKIRIM) }
    var showKonfirmasi by rememberSaveable { mutableStateOf(false) }
    var showLaporan    by rememberSaveable { mutableStateOf(false) }

    val isTransfer    = orderId.endsWith("T")
    val paymentMethod = if (isTransfer) "Transfer Bank" else "COD"
    val noResi        = if (status in listOf(OrderStatus.DIKIRIM, OrderStatus.DELIVERED, OrderStatus.SELESAI)) "JNE123456789" else ""

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            when (status) {
                OrderStatus.DIKIRIM -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .navigationBarsPadding()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.onBackground)
                                .clickable { showKonfirmasi = true }
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "KONFIRMASI TERIMA",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp, letterSpacing = 2.sp,
                                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.background
                                )
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFFCF6679).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .clickable { showLaporan = true }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "LAPORKAN TIDAK SAMPAI",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp, letterSpacing = 2.sp,
                                    fontWeight = FontWeight.Bold, color = Color(0xFFCF6679)
                                )
                            )
                        }
                    }
                }
                OrderStatus.DELIVERED, OrderStatus.SELESAI -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .navigationBarsPadding()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "✓ PESANAN SELESAI",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp, letterSpacing = 2.sp,
                                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                                )
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    ) { innerPadding ->
        // ── Scrollable Content ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp).clickable(onClick = onBack)
                )
                Text(
                    "DETAIL PESANAN",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold, letterSpacing = 2.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.size(24.dp))
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                OrderStatusSection(orderId = orderId, status = status)

                Spacer(Modifier.height(20.dp))

                OrderProgressTracker(status = status)

                Spacer(Modifier.height(20.dp))

                SectionCard {
                    OrderInfoRow(label = "ORDER ID",          value = "#SCNT-$orderId")
                    DividerLine()
                    OrderInfoRow(label = "METODE PEMBAYARAN", value = paymentMethod)
                    if (noResi.isNotEmpty()) {
                        DividerLine()
                        OrderInfoRow(label = "NOMOR RESI",    value = noResi)
                    }
                    DividerLine()
                    OrderInfoRow(label = "ESTIMASI TIBA",     value = "2-3 Hari Kerja")
                }

                Spacer(Modifier.height(16.dp))

                SectionCard {
                    Text(
                        "PRODUK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "NOIR OBSCUR",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold, fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                "BOUTIQUE SERIES • 50ML",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                            )
                        }
                        Text(
                            "Rp 240.000",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                    DividerLine()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Total Pembayaran",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            "Rp 255.000",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // ── Dialog Konfirmasi Terima ──────────────────────────────────────────────
    if (showKonfirmasi) {
        AlertDialog(
            onDismissRequest = { showKonfirmasi = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    "Konfirmasi Penerimaan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    "Apakah pesanan sudah kamu terima dalam kondisi baik?",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    status = OrderStatus.DELIVERED
                    showKonfirmasi = false
                }) {
                    Text(
                        "YA, SUDAH TERIMA", color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showKonfirmasi = false }) {
                    Text(
                        "BATAL", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                        )
                    )
                }
            }
        )
    }

    // ── Dialog Laporan Tidak Sampai ───────────────────────────────────────────
    if (showLaporan) {
        AlertDialog(
            onDismissRequest = { showLaporan = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    "Laporan Pesanan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    "Laporkan bahwa pesanan ini tidak sampai? Tim kami akan menghubungi kamu dalam 1×24 jam.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    status = OrderStatus.TIDAK_SAMPAI
                    showLaporan = false
                }) {
                    Text(
                        "YA, LAPORKAN", color = Color(0xFFCF6679),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLaporan = false }) {
                    Text(
                        "BATAL", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                        )
                    )
                }
            }
        )
    }
}

@Composable
private fun OrderStatusSection(orderId: String, status: OrderStatus) {
    val (statusColor, statusBg) = when (status) {
        OrderStatus.MENUNGGU_KONFIRMASI            -> Pair(Color(0xFFD4A853), Color(0xFFD4A853).copy(alpha = 0.1f))
        OrderStatus.DIKIRIM                        -> Pair(Color(0xFF2196F3), Color(0xFF2196F3).copy(alpha = 0.1f))
        OrderStatus.DELIVERED, OrderStatus.SELESAI -> Pair(Color(0xFF4CAF50), Color(0xFF4CAF50).copy(alpha = 0.1f))
        OrderStatus.TIDAK_SAMPAI                   -> Pair(Color(0xFFCF6679), Color(0xFFCF6679).copy(alpha = 0.1f))
        else -> Pair(Color(0xFFA0A0A0), Color(0xFF1E1E1E))
    }
    val statusIcon = when (status) {
        OrderStatus.MENUNGGU_KONFIRMASI            -> Icons.Default.HourglassEmpty
        OrderStatus.DIKIRIM                        -> Icons.Default.LocalShipping
        OrderStatus.DELIVERED, OrderStatus.SELESAI -> Icons.Default.CheckCircle
        OrderStatus.TIDAK_SAMPAI                   -> Icons.Default.Warning
        else                                       -> Icons.Default.Inventory
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(statusBg)
            .border(0.5.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    status.label,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold, fontSize = 16.sp
                    ),
                    color = statusColor
                )
                Text(
                    "ORDER #SCNT-$orderId",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                )
            }
        }
    }
}

@Composable
private fun OrderProgressTracker(status: OrderStatus) {
    val steps = listOf(
        Triple("Pesanan", Icons.Default.ShoppingBag,   listOf(OrderStatus.DALAM_PROSES, OrderStatus.DIKEMAS, OrderStatus.DIKIRIM, OrderStatus.DELIVERED, OrderStatus.SELESAI)),
        Triple("Dikemas", Icons.Default.Inventory,     listOf(OrderStatus.DIKEMAS, OrderStatus.DIKIRIM, OrderStatus.DELIVERED, OrderStatus.SELESAI)),
        Triple("Dikirim", Icons.Default.LocalShipping, listOf(OrderStatus.DIKIRIM, OrderStatus.DELIVERED, OrderStatus.SELESAI)),
        Triple("Diterima",Icons.Default.CheckCircle,   listOf(OrderStatus.DELIVERED, OrderStatus.SELESAI))
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            steps.forEachIndexed { index, (label, icon, activeStatuses) ->
                val isDone = status in activeStatuses
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDone) ScentGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.secondaryContainer)
                            .border(1.dp, if (isDone) ScentGold else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, tint = if (isDone) ScentGold else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp, letterSpacing = 0.5.sp,
                            color = if (isDone) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }
                if (index < steps.size - 1) {
                    val lineIsDone = status in steps[index + 1].third
                    Box(
                        modifier = Modifier
                            .weight(0.3f)
                            .height(1.dp)
                            .background(if (lineIsDone) ScentGold.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun OrderInfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        )
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium))
    }
}

@Composable
private fun DividerLine() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 14.dp))
}
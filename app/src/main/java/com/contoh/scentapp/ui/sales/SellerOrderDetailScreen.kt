package com.contoh.scentapp.ui.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.contoh.scentapp.domain.model.OrderStatus
import com.contoh.scentapp.ui.theme.*

@Composable
fun SellerOrderDetailScreen(
    orderId : String,
    onBack  : () -> Unit
) {
    var status         by rememberSaveable { mutableStateOf(OrderStatus.MENUNGGU_KONFIRMASI) }
    var noResi         by rememberSaveable { mutableStateOf("") }
    var showResiDialog by rememberSaveable { mutableStateOf(false) }
    var showStatusMenu by rememberSaveable { mutableStateOf(false) }

    // Demo data
    val paymentMethod  = "Transfer Bank"
    val buyerName      = "Julianne V."
    val buyerAddress   = "Jl. Melati No. 12, Banjarmasin Selatan, Kalsel 70113"

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 120.dp)) {

            // â”€â”€ Top Bar â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp).clickable(onClick = onBack))
                Text("DETAIL PESANAN MASUK", style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold, letterSpacing = 2.sp), color = MaterialTheme.colorScheme.onBackground )
                Box {
                    Icon(Icons.Default.MoreVert, "Opsi", tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp).clickable { showStatusMenu = true })
                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        listOf(
                            OrderStatus.MENUNGGU_KONFIRMASI to "Menunggu Konfirmasi",
                            OrderStatus.PEMBAYARAN_DIKONFIRMASI to "Konfirmasi Pembayaran",
                            OrderStatus.DALAM_PROSES to "Dalam Proses",
                            OrderStatus.DIKEMAS to "Dikemas",
                            OrderStatus.DIKIRIM to "Dikirim",
                            OrderStatus.DELIVERED to "Selesai"
                        ).forEach { (s, label) ->
                            DropdownMenuItem(
                                text = { Text(label, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground)) },
                                onClick = { status = s; showStatusMenu = false }
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                // â”€â”€ Status Banner â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
                val statusColor = when (status) {
                    OrderStatus.MENUNGGU_KONFIRMASI       -> Color(0xFFD4A853)
                    OrderStatus.PEMBAYARAN_DIKONFIRMASI   -> Color(0xFF4CAF50)
                    OrderStatus.DIKEMAS                   -> Color(0xFF2196F3)
                    OrderStatus.DIKIRIM                   -> Color(0xFF9C27B0)
                    OrderStatus.DELIVERED                 -> Color(0xFF4CAF50)
                    else                                  -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                }
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(statusColor.copy(alpha = 0.1f))
                        .border(0.5.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Circle, null, tint = statusColor, modifier = Modifier.size(10.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Status Pesanan", style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)))
                            Text(status.label, style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold, fontSize = 16.sp), color = statusColor)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // â”€â”€ Order Info â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
                SellerSectionCard {
                    SellerInfoRow(label = "ORDER ID",       value = "#SCNT-$orderId")
                    SDivider()
                    SellerInfoRow(label = "PEMBELI",        value = buyerName)
                    SDivider()
                    SellerInfoRow(label = "METODE BAYAR",   value = paymentMethod)
                    SDivider()
                    SellerInfoRow(label = "TANGGAL PESAN",  value = "10 Juni 2026, 14:32")
                }

                Spacer(Modifier.height(16.dp))

                // â”€â”€ Alamat Pengiriman â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
                SellerSectionCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("ALAMAT PENGIRIMAN", style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)))
                        Icon(Icons.Default.ContentCopy, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(buyerAddress, style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground, lineHeight = 22.sp))
                }

                Spacer(Modifier.height(16.dp))

                // â”€â”€ Produk Pesanan â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
                SellerSectionCard {
                    Text("PRODUK DIPESAN", style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)))
                    Spacer(Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("NOIR OBSCUR", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp), color = MaterialTheme.colorScheme.onBackground)
                            Text("BOUTIQUE SERIES â€¢ 50ML â€¢ 1 pcs", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)))
                        }
                        Text("Rp 240.000", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground))
                    }
                    SDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal Produk",   style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)))
                        Text("Rp 240.000", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onBackground))
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Biaya Pengiriman",  style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)))
                        Text("Rp 15.000",  style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onBackground))
                    }
                    SDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Total", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground))
                        Text("Rp 255.000", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground))
                    }
                }

                if (noResi.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    SellerSectionCard {
                        SellerInfoRow(label = "NOMOR RESI", value = noResi)
                    }
                }
            }
        }

        // â”€â”€ Action Buttons â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(MaterialTheme.colorScheme.background).padding(horizontal = 20.dp, vertical = 16.dp).navigationBarsPadding()
        ) {
            when (status) {
                OrderStatus.MENUNGGU_KONFIRMASI -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(ScentGold.copy(alpha = 0.15f))
                            .border(1.dp, ScentGold.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { status = OrderStatus.PEMBAYARAN_DIKONFIRMASI }.padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("KONFIRMASI PEMBAYARAN", style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold, color = ScentGold))
                    }
                }
                OrderStatus.PEMBAYARAN_DIKONFIRMASI, OrderStatus.DALAM_PROSES -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                .clickable { status = OrderStatus.DIKEMAS }.padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("TANDAI DIKEMAS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground )) }
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                .clickable { showResiDialog = true }.padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("INPUT RESI", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground )) }
                    }
                }
                OrderStatus.DIKEMAS -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.onBackground).clickable { showResiDialog = true }.padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("INPUT RESI & KIRIM", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.background)) }
                }
                OrderStatus.DIKIRIM -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer).padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("DALAM PENGIRIMAN", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))) }
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer).padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) { Text(status.label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))) }
                }
            }
        }
    }

    // â”€â”€ Resi Dialog â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    if (showResiDialog) {
        var resiInput by remember { mutableStateOf(noResi) }
        AlertDialog(
            onDismissRequest = { showResiDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Input Nomor Resi", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground) },
            text = {
                Column {
                    Text("Order #SCNT-$orderId", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)))
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        BasicTextField(
                            value         = resiInput,
                            onValueChange = { resiInput = it },
                            textStyle     = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp),
                            cursorBrush   = SolidColor(ScentGold),
                            singleLine    = true,
                            modifier      = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                if (resiInput.isEmpty()) Text("mis. JNE123456789", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)))
                                inner()
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (resiInput.isNotBlank()) {
                        noResi = resiInput
                        status = OrderStatus.DIKIRIM
                        showResiDialog = false
                    }
                }) { Text("KIRIM", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)) }
            },
            dismissButton = {
                TextButton(onClick = { showResiDialog = false }) {
                    Text("BATAL", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp))
                }
            }
        )
    }
}

@Composable
private fun SellerSectionCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface).border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(16.dp),
        content = content
    )
}

@Composable
private fun SellerInfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)))
        Spacer(Modifier.height(5.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium))
    }
}

@Composable
private fun SDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))
}

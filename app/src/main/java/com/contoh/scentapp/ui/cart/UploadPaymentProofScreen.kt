package com.contoh.scentapp.ui.cart

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.contoh.scentapp.ui.theme.*

@Composable
fun UploadPaymentProofScreen(
    onBack   : () -> Unit = {},
    onSubmit : () -> Unit = {}
) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { imageUri = it } }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(bottom = 110.dp)
        ) {
            // ── Top Bar ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp).clickable(onClick = onBack))
                Text("SCENT", style = MaterialTheme.typography.titleLarge.copy(
                    letterSpacing = 6.sp, fontSize = 18.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.size(24.dp))
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text("Upload Bukti Transfer", style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold, fontSize = 28.sp), color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(8.dp))
                Text("Pastikan bukti pembayaran terlihat jelas dan nominal sesuai.", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), lineHeight = 22.sp))

                Spacer(Modifier.height(28.dp))

                // ── Rekening Tujuan ───────────────────────────────────────────
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface).border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text("REKENING TUJUAN TRANSFER", style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)))
                    Spacer(Modifier.height(16.dp))

                    BankInfoRow(bank = "BCA", accountNo = "1234567890", holder = "Scent Official")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))
                    BankInfoRow(bank = "GoPay / OVO", accountNo = "0812-3456-7890", holder = "Scent Official")
                }

                Spacer(Modifier.height(24.dp))

                // ── Upload Area ───────────────────────────────────────────────
                Text("FOTO BUKTI PEMBAYARAN", style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)))
                Spacer(Modifier.height(10.dp))

                if (imageUri != null) {
                    // Preview image
                    Box(
                        modifier = Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model             = imageUri,
                            contentDescription= "Bukti Transfer",
                            contentScale      = ContentScale.Crop,
                            modifier          = Modifier.fillMaxSize()
                        )
                        // Re-upload button overlay
                        Box(
                            modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
                                .size(36.dp).clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f))
                                .clickable { galleryLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Edit, "Ganti foto", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(18.dp))
                        }
                    }
                } else {
                    // Empty upload area
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .clickable { galleryLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CloudUpload, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), modifier = Modifier.size(28.dp))
                            }
                            Spacer(Modifier.height(14.dp))
                            Text("Tap untuk pilih foto", style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium))
                            Spacer(Modifier.height(4.dp))
                            Text("JPG, PNG — maks 5MB", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Gallery button
                if (imageUri == null) {
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                            .clickable { galleryLauncher.launch("image/*") }.padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Photo, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("BUKA GALERI", style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)))
                        }
                    }
                }
            }
        }

        // ── Bottom Submit Button ──────────────────────────────────────────────
        Box(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(MaterialTheme.colorScheme.background).padding(horizontal = 20.dp, vertical = 16.dp).navigationBarsPadding()
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(if (imageUri != null) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outline)
                    .clickable(enabled = imageUri != null, onClick = onSubmit)
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (imageUri != null) {
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.background, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text  = "KONFIRMASI PEMBAYARAN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold,
                            color    = if (imageUri != null) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun BankInfoRow(bank: String, accountNo: String, holder: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Column(modifier = Modifier.weight(1f)) {
            Text(bank, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp), color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(2.dp))
            Text(holder, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(accountNo, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp), color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

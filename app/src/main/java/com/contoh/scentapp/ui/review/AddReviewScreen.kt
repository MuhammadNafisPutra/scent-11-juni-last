package com.contoh.scentapp.ui.review

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.contoh.scentapp.data.model.Review
import com.contoh.scentapp.data.remote.CloudinaryUploader
import com.contoh.scentapp.data.repository.ProductRepositoryImpl
import com.contoh.scentapp.data.repository.ReviewRepository
import com.contoh.scentapp.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ─── Slider Metrik Wewangian ──────────────────────────────────────────────────

@Composable
private fun ScentMetricSlider(
    label         : String,
    description   : String,
    value         : Float,
    onValueChange : (Float) -> Unit
) {
    animateFloatAsState(targetValue = value, animationSpec = tween(150), label = "slider_$label")

    val levelLabel = when {
        value < 1.5f -> "Sangat Lemah"
        value < 2.5f -> "Lemah"
        value < 3.5f -> "Sedang"
        value < 4.5f -> "Kuat"
        else         -> "Sangat Kuat"
    }
    val levelColor = when {
        value < 2.5f -> Color(0xFFCF6679)
        value < 3.5f -> Color(0xFFD4A853)
        else         -> Color(0xFF4CAF50)
    }

    // Warna teks label & divider mengikuti tema
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize     = 10.sp,
                        letterSpacing = 1.5.sp,
                        fontWeight   = FontWeight.Bold,
                        color        = onSurface.copy(alpha = 0.5f)
                    )
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color    = onSurface.copy(alpha = 0.45f)
                    )
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(levelColor.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    levelLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize      = 10.sp,
                        letterSpacing = 0.5.sp,
                        fontWeight    = FontWeight.Bold,
                        color         = levelColor
                    )
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "1",
                style = MaterialTheme.typography.labelSmall.copy(
                    color    = onSurface.copy(alpha = 0.4f),
                    fontSize = 10.sp
                )
            )
            Slider(
                value         = value,
                onValueChange = onValueChange,
                valueRange    = 1f..5f,
                steps         = 3,
                modifier      = Modifier.weight(1f),
                colors        = SliderDefaults.colors(
                    thumbColor          = ScentGold,
                    activeTrackColor    = ScentGold,
                    inactiveTrackColor  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                    activeTickColor     = Color.Transparent,
                    inactiveTickColor   = Color.Transparent
                )
            )
            Text(
                "5",
                style = MaterialTheme.typography.labelSmall.copy(
                    color    = onSurface.copy(alpha = 0.4f),
                    fontSize = 10.sp
                )
            )
        }
    }
}

// ─── Bintang Rating ───────────────────────────────────────────────────────────

@Composable
private fun StarRating(
    rating         : Int,
    onRatingChange : (Int) -> Unit
) {
    val emptyStarColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (i in 1..5) {
            Icon(
                imageVector        = if (i <= rating) Icons.Default.Star else Icons.Default.StarOutline,
                contentDescription = "$i bintang",
                tint               = if (i <= rating) ScentGold else emptyStarColor,
                modifier           = Modifier
                    .size(36.dp)
                    .clickable { onRatingChange(i) }
            )
        }
    }
}

// ─── Chip Foto ────────────────────────────────────────────────────────────────

@Composable
private fun PhotoChip(
    uri      : Uri,
    onRemove : () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
    ) {
        AsyncImage(
            model              = uri,
            contentDescription = "Foto ulasan",
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(18.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Hapus foto",
                tint               = Color.White,
                modifier           = Modifier.size(12.dp)
            )
        }
    }
}

// ─── Screen Utama ─────────────────────────────────────────────────────────────

@Composable
fun AddReviewScreen(
    orderId      : String,                 // = firestoreId produk yang diulas
    onBack       : () -> Unit,
    productRepository : ProductRepositoryImpl = ProductRepositoryImpl(),
    reviewRepository  : ReviewRepository      = ReviewRepository(),
    onSubmit     : suspend (Review) -> Result<Unit> = { review ->
        reviewRepository.addReview(parfumId = orderId, review = review)
    }
) {
    var starRating     by rememberSaveable { mutableIntStateOf(0) }
    var longevity      by rememberSaveable { mutableFloatStateOf(3f) }
    var sillage        by rememberSaveable { mutableFloatStateOf(3f) }
    var projection     by rememberSaveable { mutableFloatStateOf(3f) }
    var reviewText     by rememberSaveable { mutableStateOf("") }
    val photoUris      = remember { mutableStateListOf<Uri>() }
    var showSubmitDone by rememberSaveable { mutableStateOf(false) }
    var showError      by rememberSaveable { mutableStateOf(false) }
    var isSubmitting   by rememberSaveable { mutableStateOf(false) }
    var submitError    by rememberSaveable { mutableStateOf<String?>(null) }

    // Ambil nama & brand produk yang sebenarnya dari Firestore, supaya
    // halaman ulasan menampilkan produk yang sama dengan yang dilihat user.
    var productName  by remember { mutableStateOf("") }
    var productBrand by remember { mutableStateOf("") }
    LaunchedEffect(orderId) {
        productRepository.getParfumById(orderId)
            .onSuccess { parfum ->
                productName  = parfum.name
                productBrand = parfum.brand
            }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> if (uri != null && photoUris.size < 5) photoUris.add(uri) }

    // Token warna adaptif — otomatis gelap/terang
    val surface       = MaterialTheme.colorScheme.surfaceVariant
    val onSurface     = MaterialTheme.colorScheme.onSurface
    val outline       = MaterialTheme.colorScheme.outlineVariant
    val topBarBg      = MaterialTheme.colorScheme.background
    val topBarContent = MaterialTheme.colorScheme.onBackground

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .navigationBarsPadding()
            ) {
                if (showError && starRating == 0) {
                    Text(
                        "Pilih rating bintang terlebih dahulu",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color    = Color(0xFFCF6679),
                            fontSize = 12.sp
                        ),
                        modifier    = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        textAlign   = TextAlign.Center
                    )
                }
                if (submitError != null) {
                    Text(
                        submitError ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color    = Color(0xFFCF6679),
                            fontSize = 12.sp
                        ),
                        modifier    = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        textAlign   = TextAlign.Center
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (starRating > 0 && !isSubmitting) ScentGold else outline)
                        .clickable(enabled = !isSubmitting) {
                            if (starRating == 0) {
                                showError = true
                            } else {
                                showError   = false
                                submitError = null
                                isSubmitting = true
                                scope.launch {
                                    // Upload semua foto ke Cloudinary terlebih dahulu
                                    val uploadedUrls = mutableListOf<String>()
                                    if (photoUris.isNotEmpty()) {
                                        val uploadResults = withContext(Dispatchers.IO) {
                                            photoUris.map { uri ->
                                                CloudinaryUploader.upload(context, uri)
                                            }
                                        }
                                        for (result in uploadResults) {
                                            result.onSuccess { url ->
                                                uploadedUrls.add(url)
                                            }.onFailure {
                                                // Jika upload gagal, skip foto tersebut
                                            }
                                        }
                                    }

                                    val review = Review(
                                        id         = System.currentTimeMillis().toInt(),
                                        parfumId   = orderId,
                                        text       = reviewText,
                                        rating     = starRating.toFloat(),
                                        longevity  = longevity,
                                        sillage    = sillage,
                                        projection = projection,
                                        photoUrls  = uploadedUrls,
                                        imageCount = uploadedUrls.size,
                                        date       = "Baru saja"
                                    )
                                    val result = onSubmit(review)
                                    isSubmitting = false
                                    result
                                        .onSuccess { showSubmitDone = true }
                                        .onFailure { e ->
                                            submitError = e.message ?: "Gagal mengirim ulasan, coba lagi"
                                        }
                                }
                            }
                        }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(18.dp),
                            color       = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "KIRIM ULASAN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize      = 12.sp,
                                letterSpacing = 2.sp,
                                fontWeight    = FontWeight.Bold,
                                color         = if (starRating > 0) Color.Black
                                                else onSurface.copy(alpha = 0.38f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            // ── Top Bar ───────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(topBarBg)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint     = topBarContent,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(24.dp)
                        .clickable(onClick = onBack)
                )
                Text(
                    "TULIS ULASAN",
                    style    = MaterialTheme.typography.titleMedium.copy(
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color         = topBarContent
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(16.dp))

                // ── Info Produk ───────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(surface)
                        .border(0.5.dp, outline, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment    = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ScentGold.copy(alpha = 0.12f))
                            .border(0.5.dp, ScentGold.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "✦",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color    = ScentGold,
                                fontSize = 20.sp
                            )
                        )
                    }
                    Column {
                        Text(
                            productName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize   = 15.sp,
                                color      = onSurface
                            )
                        )
                        Text(
                            productBrand,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = onSurface.copy(alpha = 0.55f)
                            )
                        )
                        Text(
                            "ORDER #SCNT-$orderId",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize      = 10.sp,
                                letterSpacing = 0.5.sp,
                                color         = ScentGold.copy(alpha = 0.8f)
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Rating Bintang ────────────────────────────────────────────
                SectionCard(surface = surface, outline = outline) {
                    Text(
                        "PENILAIAN KESELURUHAN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize      = 10.sp,
                            letterSpacing = 1.5.sp,
                            color         = onSurface.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Bagaimana kesan umum wewangian ini?",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = onSurface.copy(alpha = 0.45f)
                        )
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        StarRating(
                            rating         = starRating,
                            onRatingChange = { starRating = it; showError = false }
                        )
                    }
                    if (starRating > 0) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            when (starRating) {
                                1    -> "😞  Mengecewakan"
                                2    -> "😐  Biasa saja"
                                3    -> "🙂  Cukup baik"
                                4    -> "😊  Bagus"
                                else -> "🤩  Luar biasa!"
                            },
                            style     = MaterialTheme.typography.bodySmall.copy(
                                color      = ScentGold,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier  = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Metrik Wewangian ──────────────────────────────────────────
                SectionCard(surface = surface, outline = outline) {
                    Text(
                        "METRIK WEWANGIAN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize      = 10.sp,
                            letterSpacing = 1.5.sp,
                            color         = onSurface.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Nilai berdasarkan pengalaman pemakaian kamu",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = onSurface.copy(alpha = 0.45f)
                        )
                    )
                    Spacer(Modifier.height(20.dp))

                    ScentMetricSlider(
                        label         = "Ketahanan",
                        description   = "Seberapa lama aroma bertahan di kulit",
                        value         = longevity,
                        onValueChange = { longevity = it }
                    )
                    HorizontalDivider(
                        color     = outline,
                        thickness = 0.5.dp,
                        modifier  = Modifier.padding(vertical = 18.dp)
                    )
                    ScentMetricSlider(
                        label         = "Jejak (Sillage)",
                        description   = "Seberapa jauh aroma tercium orang lain",
                        value         = sillage,
                        onValueChange = { sillage = it }
                    )
                    HorizontalDivider(
                        color     = outline,
                        thickness = 0.5.dp,
                        modifier  = Modifier.padding(vertical = 18.dp)
                    )
                    ScentMetricSlider(
                        label         = "Pancaran (Projection)",
                        description   = "Seberapa kuat aura wewangian di sekitar kamu",
                        value         = projection,
                        onValueChange = { projection = it }
                    )

                    Spacer(Modifier.height(16.dp))

                    // Ringkasan rata-rata
                    val avgMetric = (longevity + sillage + projection) / 3f
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ScentGold.copy(alpha = 0.08f))
                            .border(0.5.dp, ScentGold.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            "RATA-RATA METRIK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize      = 10.sp,
                                letterSpacing = 1.sp,
                                color         = onSurface.copy(alpha = 0.5f)
                            )
                        )
                        Text(
                            "%.1f / 5".format(avgMetric),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize   = 16.sp,
                                color      = ScentGold
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Teks Ulasan ───────────────────────────────────────────────
                SectionCard(surface = surface, outline = outline) {
                    Text(
                        "CERITAKAN PENGALAMANMU",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize      = 10.sp,
                            letterSpacing = 1.5.sp,
                            color         = onSurface.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Opsional — bantu pembeli lain dengan ulasan kamu",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = onSurface.copy(alpha = 0.45f)
                        )
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value         = reviewText,
                        onValueChange = { if (it.length <= 500) reviewText = it },
                        modifier      = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 110.dp),
                        placeholder   = {
                            Text(
                                "Contoh: Aroma dibuka dengan citrus segar, lalu mengering ke base kayu yang hangat...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color    = onSurface.copy(alpha = 0.35f),
                                    fontSize = 13.sp
                                )
                            )
                        },
                        colors    = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = ScentGold,
                            unfocusedBorderColor = outline,
                            cursorColor          = ScentGold,
                            focusedTextColor     = onSurface,
                            unfocusedTextColor   = onSurface
                        ),
                        shape     = RoundedCornerShape(10.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        maxLines  = 8
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${reviewText.length}/500",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color    = if (reviewText.length > 450) Color(0xFFCF6679)
                                       else onSurface.copy(alpha = 0.4f)
                        ),
                        modifier  = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── Tambah Foto ───────────────────────────────────────────────
                SectionCard(surface = surface, outline = outline) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "TAMBAH FOTO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize      = 10.sp,
                                    letterSpacing = 1.5.sp,
                                    color         = onSurface.copy(alpha = 0.5f)
                                )
                            )
                            Text(
                                "Opsional — maks. 5 foto",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = onSurface.copy(alpha = 0.45f)
                                )
                            )
                        }
                        Text(
                            "${photoUris.size}/5",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                color    = onSurface.copy(alpha = 0.45f)
                            )
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier              = Modifier.fillMaxWidth()
                    ) {
                        photoUris.forEachIndexed { index, uri ->
                            PhotoChip(uri = uri, onRemove = { photoUris.removeAt(index) })
                        }
                        if (photoUris.size < 5) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .border(1.dp, outline, RoundedCornerShape(8.dp))
                                    .clickable { photoPicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Tambah foto",
                                        tint               = onSurface.copy(alpha = 0.45f),
                                        modifier           = Modifier.size(22.dp)
                                    )
                                    Text(
                                        "Foto",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            color    = onSurface.copy(alpha = 0.45f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }

    // ── Dialog Berhasil Kirim ─────────────────────────────────────────────────
    if (showSubmitDone) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    "Ulasan Terkirim! ✦",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    "Terima kasih sudah berbagi pengalamanmu. Ulasanmu akan membantu pembeli lain menemukan wewangian yang tepat.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showSubmitDone = false; onBack() }) {
                    Text(
                        "KEMBALI KE PESANAN",
                        color = ScentGold,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight    = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        )
    }
}

// ─── Helper composable ────────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    surface : androidx.compose.ui.graphics.Color,
    outline : androidx.compose.ui.graphics.Color,
    content : @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(surface)
            .border(0.5.dp, outline, RoundedCornerShape(12.dp))
            .padding(16.dp),
        content = content
    )
}

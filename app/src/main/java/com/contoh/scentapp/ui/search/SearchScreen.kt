package com.contoh.scentapp.ui.search

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.contoh.scentapp.domain.model.AromaFilter
import com.contoh.scentapp.domain.model.Product
import com.contoh.scentapp.domain.model.UsageFilter
import com.contoh.scentapp.ui.theme.ScentGold
import com.contoh.scentapp.ui.theme.ScentTextPrimary

private enum class SearchPhase { FILTER, RESULTS }

@Composable
fun SearchScreen(
    initialQuery  : String = "",
    onBack        : () -> Unit,
    onProductClick: (String) -> Unit = {},  // <- String
    viewModel     : SearchViewModel = viewModel(
        factory = com.contoh.scentapp.di.ViewModelFactory.searchFactory(
            androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application
        )
    )
) {
    val uiState    by viewModel.uiState.collectAsStateWithLifecycle()
    var localQuery by rememberSaveable { mutableStateOf(initialQuery) }
    var phase      by rememberSaveable { mutableStateOf(SearchPhase.FILTER) }

    LaunchedEffect(localQuery) { viewModel.onQueryChange(localQuery) }

    val listState = rememberLazyListState()
    val focusReq  = remember { FocusRequester() }
    LaunchedEffect(phase) {
        if (phase == SearchPhase.FILTER) focusReq.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (phase) {
            SearchPhase.FILTER -> FilterPhase(
                uiState       = uiState,
                localQuery    = localQuery,
                focusReq      = focusReq,
                listState     = listState,
                onBack        = onBack,
                onQueryChange = { localQuery = it },
                onToggleAroma = { viewModel.toggleAromaFilter(it) },
                onToggleUsage = { viewModel.toggleUsageFilter(it) },
                onClear       = { viewModel.clearAllFilters(); localQuery = "" },
                onApply       = {
                    viewModel.applyFilters()
                    phase = SearchPhase.RESULTS
                }
            )
            SearchPhase.RESULTS -> ResultsPhase(
                uiState        = uiState,
                onBack         = { phase = SearchPhase.FILTER },
                onProductClick = onProductClick,
                onFavToggle    = { viewModel.toggleFavorite(it) }
            )
        }
    }
}

@Composable
private fun FilterPhase(
    uiState       : com.contoh.scentapp.ui.state.SearchUiState,
    localQuery    : String,
    focusReq      : FocusRequester,
    listState     : androidx.compose.foundation.lazy.LazyListState,
    onBack        : () -> Unit,
    onQueryChange : (String) -> Unit,
    onToggleAroma : (String) -> Unit,
    onToggleUsage : (String) -> Unit,
    onClear       : () -> Unit,
    onApply       : () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SearchTopBar(title = "Cari & Filter", onBack = onBack)

        if (uiState.isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ScentGold)
            }
            return@Column
        }

        if (!uiState.hasProducts) {
            Box(
                modifier         = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text  = "✦",
                        style = MaterialTheme.typography.displayLarge.copy(color = MaterialTheme.colorScheme.outlineVariant)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text      = "Belum ada produk untuk dicari",
                        style     = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text      = "Filter pencarian akan muncul setelah ada produk yang ditambahkan",
                        style     = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            return@Column
        }

        LazyColumn(
            state          = listState,
            modifier       = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item(key = "search_input") {
                SearchInputField(
                    query          = localQuery,
                    onChange       = onQueryChange,
                    focusRequester = focusReq,
                    modifier       = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }
            if (uiState.aromaFilters.isNotEmpty()) {
                item(key = "aroma_header") {
                    FilterSectionLabel(
                        text     = "PROFIL AROMA",
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp)
                    )
                }
                item(key = "aroma_chips") {
                    AromaChipGroup(
                        filters         = uiState.aromaFilters,
                        selectedFilters = uiState.selectedAromaFilters,
                        onToggle        = onToggleAroma,
                        modifier        = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
            if (uiState.usageFilters.isNotEmpty()) {
                item(key = "usage_header") {
                    FilterSectionLabel(
                        text     = "PENGGUNAAN",
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 12.dp)
                    )
                }
                item(key = "usage_buttons") {
                    UsageButtonGroup(
                        filters       = uiState.usageFilters,
                        selectedUsage = uiState.selectedUsage,
                        onToggle      = onToggleUsage,
                        modifier      = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
            item(key = "results_summary") {
                Spacer(Modifier.height(32.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                ResultsSummary(
                    count     = uiState.resultCount,
                    hasActive = uiState.hasActiveFilters || localQuery.isNotBlank(),
                    onClear   = onClear,
                    modifier  = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding()
        ) {
            ApplyFilterButton(onClick = onApply)
        }
    }
}

@Composable
private fun ResultsPhase(
    uiState        : com.contoh.scentapp.ui.state.SearchUiState,
    onBack         : () -> Unit,
    onProductClick : (String) -> Unit,  // ← String
    onFavToggle    : (Int) -> Unit
) {
    val listState = rememberLazyListState()
    Column(modifier = Modifier.fillMaxSize()) {
        SearchTopBar(title = "Hasil Pencarian", onBack = onBack)
        LazyColumn(
            state          = listState,
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item(key = "results_header") {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text(
                        text  = "HASIL FILTER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text  = "${uiState.resultCount} Produk",
                            style = MaterialTheme.typography.displayMedium.copy(fontSize = 28.sp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (uiState.query.isNotBlank()) {
                            Text(
                                text  = "\"${uiState.query}\"",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color     = ScentGold,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            )
                        }
                    }
                    if (uiState.selectedAromaFilters.isNotEmpty() || uiState.selectedUsage != null) {
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            uiState.selectedAromaFilters.forEach { aroma -> FilterBadge(label = aroma) }
                            uiState.selectedUsage?.let { FilterBadge(label = it) }
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                Spacer(Modifier.height(8.dp))
            }
            if (uiState.results.isEmpty()) {
                item(key = "empty") {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(top = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text  = "✦",
                                style = MaterialTheme.typography.displayLarge.copy(color = MaterialTheme.colorScheme.outlineVariant)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text      = "Tidak ada parfum\nyang cocok",
                                style     = MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text  = "Coba ubah filter Anda",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            )
                        }
                    }
                }
            } else {
                val rows = uiState.results.chunked(2)
                items(count = rows.size, key = { "row_$it" }) { rowIndex ->
                    SearchProductRow(
                        products       = rows[rowIndex],
                        onProductClick = onProductClick,
                        onFavToggle    = onFavToggle,
                        modifier       = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterBadge(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(ScentGold.copy(alpha = 0.15f))
            .border(0.5.dp, ScentGold.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp, letterSpacing = 1.sp, color = ScentGold
            )
        )
    }
}

@Composable
private fun SearchProductRow(
    products       : List<Product>,
    onProductClick : (String) -> Unit,  // ← String
    onFavToggle    : (Int) -> Unit,
    modifier       : Modifier = Modifier
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        products.forEach { product ->
            SearchProductCard(
                product     = product,
                onClick     = { onProductClick(product.firestoreId) },  // ← firestoreId
                onFavToggle = { onFavToggle(product.id) },
                modifier    = Modifier.weight(1f)
            )
        }
        if (products.size == 1) Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun SearchProductCard(
    product     : Product,
    onClick     : () -> Unit,
    onFavToggle : () -> Unit,
    modifier    : Modifier = Modifier
) {
    val heartTint by animateColorAsState(
        targetValue   = if (product.isFavorite) ScentGold else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        animationSpec = tween(300),
        label         = "heartColor_${product.id}"
    )
    Column(modifier = modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .fillMaxWidth().height(170.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(product.cardColor).copy(alpha = 0.9f),
                            Color(product.cardColor).copy(alpha = 0.5f)
                        )
                    )
                )
        ) {
            // Tampilkan foto produk asli jika ada, fallback ke placeholder botol
            if (product.imageUrl.isNotBlank()) {
                AsyncImage(
                    model              = product.imageUrl,
                    contentDescription = product.name,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.align(Alignment.Center).width(44.dp).height(80.dp)) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter).width(40.dp).height(66.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Brush.verticalGradient(listOf(
                                Color(product.accentColor).copy(alpha = 0.5f),
                                Color(product.accentColor).copy(alpha = 0.1f)
                            )))
                            .border(0.5.dp, Color(product.accentColor).copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter).width(16.dp).height(12.dp)
                            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                            .background(Color(product.accentColor).copy(alpha = 0.4f))
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd).padding(10.dp)
                    .size(32.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                    .clickable(onClick = onFavToggle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = if (product.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorit",
                    tint               = heartTint,
                    modifier           = Modifier.size(16.dp)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text  = product.brand,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text     = product.name,
            style    = MaterialTheme.typography.titleMedium,
            color    = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text  = product.price,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text  = product.volume,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
            )
        }
    }
}

@Composable
private fun SearchTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Kembali",
            tint               = MaterialTheme.colorScheme.onBackground,
            modifier           = Modifier.size(24.dp).clickable(onClick = onBack)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text  = "SCENT",
            style = MaterialTheme.typography.titleLarge.copy(
                letterSpacing = 6.sp, fontSize = 18.sp, fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun SearchInputField(
    query          : String,
    onChange       : (String) -> Unit,
    focusRequester : FocusRequester,
    modifier       : Modifier = Modifier
) {
    val onBg = MaterialTheme.colorScheme.onBackground
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier              = Modifier.fillMaxWidth()
        ) {
            BasicTextField(
                value         = query,
                onValueChange = onChange,
                singleLine    = true,
                cursorBrush   = SolidColor(ScentGold),
                textStyle     = MaterialTheme.typography.titleMedium.copy(
                    color         = onBg,
                    fontSize      = 20.sp,
                    fontWeight    = FontWeight.Normal,
                    letterSpacing = 2.sp
                ),
                modifier      = Modifier.weight(1f).focusRequester(focusRequester),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            text  = "Cari esens Anda...",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                fontSize = 20.sp
                            )
                        )
                    }
                    inner()
                }
            )
            Icon(
                imageVector        = Icons.Default.Search,
                contentDescription = "Cari",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                modifier           = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun FilterSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text     = text,
        modifier = modifier,
        style    = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    )
}

@Composable
private fun AromaChipGroup(
    filters         : List<AromaFilter>,
    selectedFilters : Set<String>,
    onToggle        : (String) -> Unit,
    modifier        : Modifier = Modifier
) {
    val rows = filters.chunked(2)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { filter ->
                    AromaFilterChip(
                        label      = filter.label,
                        isSelected = filter.id in selectedFilters,
                        onClick    = { onToggle(filter.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AromaFilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val onBg        = MaterialTheme.colorScheme.onBackground
    val bgColor     by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.secondaryContainer    else Color.Transparent, tween(200), "chipBg_$label")
    val borderColor by animateColorAsState(if (isSelected) onBg             else MaterialTheme.colorScheme.outlineVariant,      tween(200), "chipBorder_$label")
    val textColor   by animateColorAsState(if (isSelected) onBg             else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),    tween(200), "chipText_$label")
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(50.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize   = 10.sp,
                letterSpacing = 1.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color      = textColor
            )
        )
    }
}

@Composable
private fun UsageButtonGroup(
    filters       : List<UsageFilter>,
    selectedUsage : String?,
    onToggle      : (String) -> Unit,
    modifier      : Modifier = Modifier
) {
    val onBg = MaterialTheme.colorScheme.onBackground
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        filters.forEach { filter ->
            val isSelected  = filter.id == selectedUsage
            val bgColor     by animateColorAsState(if (isSelected) ScentGold.copy(alpha = 0.15f) else Color.Transparent, tween(200), "usageBg_${filter.id}")
            val borderColor by animateColorAsState(if (isSelected) ScentGold  else MaterialTheme.colorScheme.outlineVariant,  tween(200), "usageBorder_${filter.id}")
            val textColor   by animateColorAsState(if (isSelected) onBg       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), tween(200), "usageText_${filter.id}")
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                    .clickable { onToggle(filter.id) }
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = filter.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize      = 11.sp,
                        letterSpacing = 2.sp,
                        fontWeight    = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color         = textColor
                    )
                )
            }
        }
    }
}

@Composable
private fun ResultsSummary(
    count     : Int,
    hasActive : Boolean,
    onClear   : () -> Unit,
    modifier  : Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text  = "HASIL DITEMUKAN",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 2.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 10.sp
            )
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text  = "$count Produk",
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 28.sp),
                color = MaterialTheme.colorScheme.onBackground
            )
            if (hasActive) {
                Row(
                    modifier          = Modifier.clickable(onClick = onClear),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text  = "HAPUS SEMUA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        )
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector        = Icons.Default.Close,
                        contentDescription = "Hapus filter",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        modifier           = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ApplyFilterButton(onClick: () -> Unit) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val bg   = MaterialTheme.colorScheme.background
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(onBg)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = "TERAPKAN FILTER",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize      = 12.sp,
                letterSpacing = 2.sp,
                fontWeight    = FontWeight.Bold,
                color         = bg
            )
        )
    }
}
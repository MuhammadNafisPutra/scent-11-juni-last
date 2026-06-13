    package com.contoh.scentapp.ui.home

    import androidx.compose.animation.animateColorAsState
    import androidx.compose.animation.core.tween
    import androidx.compose.foundation.background
    import androidx.compose.foundation.border
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.LazyListState
    import androidx.compose.foundation.lazy.rememberLazyListState
    import androidx.compose.foundation.shape.CircleShape
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.Favorite
    import androidx.compose.material.icons.filled.Search
    import androidx.compose.material.icons.outlined.FavoriteBorder
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.graphics.Brush
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.ui.res.stringResource
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.style.TextOverflow
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.lifecycle.compose.collectAsStateWithLifecycle
    import androidx.lifecycle.viewmodel.compose.viewModel
    import coil.compose.SubcomposeAsyncImage
    import com.contoh.scentapp.R
    import com.contoh.scentapp.domain.model.HeroBanner
    import com.contoh.scentapp.ui.state.HomeUiState
    import com.contoh.scentapp.domain.model.Product
    import com.contoh.scentapp.ui.theme.*

    @Composable
    fun HomeScreen(
        onProductClick : (String) -> Unit = {},  // ← String (firestoreId)
        onSearchClick  : () -> Unit       = {},
        viewModel      : HomeViewModel    = viewModel(factory = com.contoh.scentapp.di.ViewModelFactory.homeFactory())
    ) {
        val uiState   by viewModel.uiState.collectAsStateWithLifecycle()
        val listState : LazyListState = rememberLazyListState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color    = ScentGold
                )
            } else if (uiState.errorMessage != null) {
                Column(
                    modifier            = Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Gagal memuat produk",
                        style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onBackground)
                    )
                    Text(
                        uiState.errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                    )
                }
            } else {
                HomeContent(
                    uiState          = uiState,
                    listState        = listState,
                    onSearchClick    = onSearchClick,
                    onProductClick   = onProductClick,
                    onFavoriteToggle = { viewModel.toggleFavorite(it) }
                )
            }
        }
    }

    @Composable
    private fun HomeContent(
        uiState          : HomeUiState,
        listState        : LazyListState,
        onSearchClick    : () -> Unit,
        onProductClick   : (String) -> Unit,  // ← String
        onFavoriteToggle : (Int) -> Unit
    ) {
        LazyColumn(
            state          = listState,
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item(key = "topbar") { ScentTopBar() }
            item(key = "search") {
                ScentSearchBarButton(
                    onClick  = onSearchClick,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
            uiState.heroBanner?.let { banner ->
                item(key = "hero") {
                    HeroBannerCard(
                        banner   = banner,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }

            if (uiState.filteredProducts.isEmpty()) {
                item(key = "empty") {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(vertical = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Belum ada produk tersedia",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            } else {
                item(key = "section_header") {
                    CollectionHeader(
                        modifier = Modifier.padding(
                            start = 20.dp, end = 20.dp, top = 28.dp, bottom = 16.dp
                        )
                    )
                }
                val rows = uiState.filteredProducts.chunked(2)
                items(count = rows.size, key = { "row_$it" }) { rowIndex ->
                    ProductRow(
                        products         = rows[rowIndex],
                        onProductClick   = onProductClick,
                        onFavoriteToggle = onFavoriteToggle,
                        modifier         = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }

    @Composable
    private fun ScentTopBar() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text  = "SCENT",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 6.sp,
                    fontSize      = 18.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }

    @Composable
    private fun ScentSearchBarButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search),
                    tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text  = stringResource(R.string.search_hint),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                )
            }
        }
    }

    @Composable
    private fun HeroBannerCard(banner: HeroBanner, modifier: Modifier = Modifier) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(banner.gradientStart), Color(banner.gradientEnd))
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0x00000000), Color(0xCC000000))))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 32.dp)
                    .width(80.dp)
                    .height(160.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .width(70.dp).height(130.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Brush.verticalGradient(listOf(Color(0x884A4A4A), Color(0xCC121212))))
                        .border(0.5.dp, Color(0xFF666666), RoundedCornerShape(6.dp))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .width(30.dp).height(28.dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 22.dp)
                        .width(14.dp).height(18.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Text(
                    text  = banner.tag,
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, color = ScentGold)
                )
                Spacer(Modifier.height(8.dp))
                Text(text = banner.title, style = MaterialTheme.typography.displayMedium)
                Spacer(Modifier.height(10.dp))
                Text(
                    text     = banner.description,
                    style    = MaterialTheme.typography.bodySmall.copy(color = ScentTextPrimary, lineHeight = 18.sp),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    @Composable
    private fun CollectionHeader(modifier: Modifier = Modifier) {
        Column(modifier = modifier) {
            Text(
                text  = stringResource(R.string.home_collection_label),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.sp,
                    color         = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = stringResource(R.string.home_collection_title),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }

    @Composable
    private fun ProductRow(
        products         : List<Product>,
        onProductClick   : (String) -> Unit,  // ← String
        onFavoriteToggle : (Int) -> Unit,
        modifier         : Modifier = Modifier
    ) {
        Row(
            modifier              = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            products.forEach { product ->
                ProductCard(
                    product          = product,
                    onClick          = { onProductClick(product.firestoreId) },  // ← firestoreId
                    onFavoriteToggle = { onFavoriteToggle(product.id) },
                    modifier         = Modifier.weight(1f)
                )
            }
            if (products.size == 1) Spacer(modifier = Modifier.weight(1f))
        }
    }

    @Composable
    private fun ProductCard(
        product          : Product,
        onClick          : () -> Unit,
        onFavoriteToggle : () -> Unit,
        modifier         : Modifier = Modifier
    ) {
        val heartTint by animateColorAsState(
            targetValue   = if (product.isFavorite) ScentGold
            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            animationSpec = tween(300),
            label         = "heartColor_${product.id}"
        )

        Column(modifier = modifier.clickable(onClick = onClick)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
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
                if (product.imageUrl.isNotBlank()) {
                    SubcomposeAsyncImage(
                        model              = product.imageUrl,
                        contentDescription = product.name,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                        loading = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(24.dp),
                                    color       = ScentGold,
                                    strokeWidth = 2.dp
                                )
                            }
                        },
                        error = { BottleIllustration(accentColor = product.accentColor) }
                    )
                } else {
                    BottleIllustration(accentColor = product.accentColor)
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                        .clickable(onClick = onFavoriteToggle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = if (product.isFavorite) Icons.Filled.Favorite
                        else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(R.string.favorite),
                        tint               = heartTint,
                        modifier           = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text  = product.brand,
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    color         = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text     = product.name,
                style    = MaterialTheme.typography.titleMedium,
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
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                )
            }
        }
    }

    @Composable
    private fun BottleIllustration(accentColor: Long) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.width(44.dp).height(80.dp)) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .width(40.dp).height(66.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(accentColor).copy(alpha = 0.5f),
                                    Color(accentColor).copy(alpha = 0.1f)
                                )
                            )
                        )
                        .border(0.5.dp, Color(accentColor).copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .width(16.dp).height(12.dp)
                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                        .background(Color(accentColor).copy(alpha = 0.4f))
                )
            }
        }
    }
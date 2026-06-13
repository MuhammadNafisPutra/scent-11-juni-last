package com.contoh.scentapp.ui.profile

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.contoh.scentapp.R
import com.contoh.scentapp.domain.model.LanguageOption
import com.contoh.scentapp.ui.theme.*

private val languageOptions = listOf(
    LanguageOption("id", "Bahasa Indonesia", "INDONESIAN"),
    LanguageOption("en", "English", "UNITED KINGDOM")
)

@Composable
fun LanguageScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: LanguageViewModel = viewModel(
        factory = com.contoh.scentapp.di.ViewModelFactory.languageFactory(context)
    )
    val selectedLang by viewModel.selectedLang.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier           = Modifier
                            .size(24.dp)
                            .clickable(onClick = onBack)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text  = stringResource(R.string.language_title), // ✅
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight    = FontWeight.Bold,
                            fontSize      = 14.sp,
                            letterSpacing = 3.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text  = "SCENT", // Nama brand, tidak perlu diterjemahkan
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 6.sp,
                        fontSize      = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // ── Subtitle ──────────────────────────────────────────────────────
            Spacer(Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text  = stringResource(R.string.interface_preferences), // ✅
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize      = 10.sp,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = stringResource(R.string.select_language), // ✅
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize   = 28.sp,
                        lineHeight = 36.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // ── Daftar Pilihan Bahasa ─────────────────────────────────────────
            Spacer(Modifier.height(32.dp))
            languageOptions.forEach { lang ->
                val isSelected = lang.id == selectedLang

                val bgColor by animateColorAsState(
                    targetValue   = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                    animationSpec = tween(200),
                    label         = "langBg_${lang.id}"
                )
                val borderColor by animateColorAsState(
                    targetValue   = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outlineVariant,
                    animationSpec = tween(200),
                    label         = "langBorder_${lang.id}"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable { viewModel.setLanguage(lang.id) }
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text  = lang.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text  = lang.subtitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize      = 10.sp,
                                letterSpacing = 1.5.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                            )
                        )
                    }
                    RadioButton(
                        selected = isSelected,
                        onClick  = { viewModel.setLanguage(lang.id) },
                        colors   = RadioButtonDefaults.colors(
                            selectedColor   = MaterialTheme.colorScheme.onBackground,
                            unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        )
                    )
                }
            }
        }
    }
}
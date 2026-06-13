package com.contoh.scentapp.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector

private data class NavItem(
    val route:   String,
    val label:   String,
    val iconOn: ImageVector,
    val iconOff: ImageVector
)

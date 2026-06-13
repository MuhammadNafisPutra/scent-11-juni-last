package com.contoh.scentapp.ui.theme.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.contoh.scentapp.R

private data class NavItem(
    val route    : String,
    val labelRes : Int,          // âœ… Ganti dari String ke resource ID
    val iconOn   : ImageVector,
    val iconOff  : ImageVector
)

private val navItems = listOf(
    NavItem("home",     R.string.nav_home,     Icons.Filled.Home,        Icons.Outlined.Home),
    NavItem("favorite", R.string.nav_favorite, Icons.Filled.Favorite,    Icons.Outlined.FavoriteBorder),
    NavItem("cart",     R.string.nav_cart,     Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag),
    NavItem("profile",  R.string.nav_profile,  Icons.Filled.Person,      Icons.Outlined.Person)
)

@Composable
fun ScentBottomNavBar(
    currentRoute: String,
    onNavigate  : (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            navItems.forEach { item ->
                NavBarItem(
                    item       = item,
                    isSelected = currentRoute == item.route,
                    onClick    = { onNavigate(item.route) },
                    modifier   = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    item      : NavItem,
    isSelected: Boolean,
    onClick   : () -> Unit,
    modifier  : Modifier = Modifier
) {
    val activeColor   = MaterialTheme.colorScheme.onSurface
    val inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)

    val iconTint by animateColorAsState(
        targetValue   = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 200),
        label         = "navIconColor_${item.route}"
    )
    val labelTint by animateColorAsState(
        targetValue   = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 200),
        label         = "navLabelColor_${item.route}"
    )

    val label = stringResource(item.labelRes) // âœ… Ambil string dari resource

    Column(
        modifier            = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector        = if (isSelected) item.iconOn else item.iconOff,
            contentDescription = label,
            tint               = iconTint,
            modifier           = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text  = label, // âœ… Pakai label dari stringResource
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize      = 9.sp,
                letterSpacing = 0.8.sp,
                fontWeight    = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color         = labelTint
            )
        )
    }
}
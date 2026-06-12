package com.contoh.scentapp.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.contoh.scentapp.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess : () -> Unit = {},
    onRegister     : () -> Unit = {},
    viewModel      : AuthViewModel = viewModel(factory = AuthViewModelFactory(LocalContext.current.applicationContext as android.app.Application))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var email    by rememberSaveable { mutableStateOf(uiState.loginEmail) }
    var password by rememberSaveable { mutableStateOf(uiState.loginPassword) }

    LaunchedEffect(email)    { viewModel.onLoginEmailChange(email) }
    LaunchedEffect(password) { viewModel.onLoginPasswordChange(password) }

    // ── Adaptive color tokens ──────────────────────────────────────────────
    val bg       = MaterialTheme.colorScheme.background
    val onBg     = MaterialTheme.colorScheme.onBackground
    val muted    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
    val label    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.38f)
    val divider  = MaterialTheme.colorScheme.outline
    val btnBg    = MaterialTheme.colorScheme.onBackground   // putih di dark, hitam di light
    val btnText  = MaterialTheme.colorScheme.background     // kebalikannya

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(48.dp))
            Text(
                text  = "SCENT",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 6.sp,
                    fontSize      = 20.sp
                ),
                color = onBg
            )
            Spacer(Modifier.height(40.dp))
            Text(
                text  = "Selamat Datang\nKembali",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize   = 36.sp,
                    lineHeight = 44.sp
                ),
                color = onBg
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text  = "Masukkan kredensial Anda untuk mengakses koleksi Anda.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color      = muted,
                    lineHeight = 22.sp
                )
            )
            Spacer(Modifier.height(40.dp))
            Text(
                text  = "ALAMAT EMAIL",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize      = 10.sp,
                    letterSpacing = 2.sp,
                    color         = label
                )
            )
            Spacer(Modifier.height(10.dp))
            BasicTextField(
                value         = email,
                onValueChange = { email = it },
                textStyle     = MaterialTheme.typography.bodyMedium.copy(
                    color    = onBg,
                    fontSize = 16.sp
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                cursorBrush   = SolidColor(ScentGold),
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (email.isEmpty()) {
                        Text(
                            text  = "atelier@scent.com",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = muted, fontSize = 16.sp
                            )
                        )
                    }
                    inner()
                }
            )
            HorizontalDivider(
                color     = divider,
                thickness = 0.5.dp,
                modifier  = Modifier.padding(top = 10.dp)
            )

            Spacer(Modifier.height(28.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = "KATA SANDI",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize      = 10.sp,
                        letterSpacing = 2.sp,
                        color         = label
                    )
                )
                Text(
                    text  = "LUPA KATA SANDI",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize      = 10.sp,
                        letterSpacing = 1.5.sp,
                        color         = muted
                    ),
                    modifier = Modifier.clickable { }
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value                = password,
                    onValueChange        = { password = it },
                    textStyle            = MaterialTheme.typography.bodyMedium.copy(
                        color = onBg, fontSize = 16.sp
                    ),
                    visualTransformation = if (uiState.showLoginPass)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                    cursorBrush          = SolidColor(ScentGold),
                    singleLine           = true,
                    modifier             = Modifier.weight(1f),
                    decorationBox        = { inner ->
                        if (password.isEmpty()) {
                            Text(
                                text  = "••••••••",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = muted, fontSize = 16.sp
                                )
                            )
                        }
                        inner()
                    }
                )
                Icon(
                    imageVector        = if (uiState.showLoginPass) Icons.Default.VisibilityOff
                    else Icons.Default.Visibility,
                    contentDescription = "Toggle password",
                    tint               = muted,
                    modifier           = Modifier
                        .size(20.dp)
                        .clickable { viewModel.toggleLoginPasswordVisibility() }
                )
            }
            HorizontalDivider(
                color     = divider,
                thickness = 0.5.dp,
                modifier  = Modifier.padding(top = 10.dp)
            )
            uiState.errorMessage?.let { error ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text  = error,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.error
                    )
                )
            }

            Spacer(Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(btnBg)
                    .clickable(enabled = !uiState.isLoading) {
                        viewModel.login(onSuccess = onLoginSuccess)
                    }
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color    = btnText,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text  = "MASUK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize      = 12.sp,
                            letterSpacing = 3.sp,
                            fontWeight    = FontWeight.Bold,
                            color         = btnText
                        )
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text  = "Baru di ASCENT? ",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = muted
                )
            )
            Text(
                text  = "Daftar Sekarang",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color      = onBg
                ),
                modifier = Modifier.clickable(onClick = onRegister)
            )
        }
    }
}

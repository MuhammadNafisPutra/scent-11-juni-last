package com.contoh.scentapp.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun RegisterScreen(
    onRegisterSuccess : () -> Unit = {},
    onLogin           : () -> Unit = {},
    viewModel         : AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            LocalContext.current.applicationContext as android.app.Application
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // State lokal form — sync ke ViewModel via LaunchedEffect
    var name     by rememberSaveable { mutableStateOf(uiState.registerName) }
    var email    by rememberSaveable { mutableStateOf(uiState.registerEmail) }
    var password by rememberSaveable { mutableStateOf(uiState.registerPassword) }

    LaunchedEffect(name)     { viewModel.onRegisterNameChange(name) }
    LaunchedEffect(email)    { viewModel.onRegisterEmailChange(email) }
    LaunchedEffect(password) { viewModel.onRegisterPasswordChange(password) }

    // ── Adaptive color tokens ──────────────────────────────────────────────
    val bg      = MaterialTheme.colorScheme.background
    val onBg    = MaterialTheme.colorScheme.onBackground
    val muted   = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
    val label   = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.38f)
    val inputBg = MaterialTheme.colorScheme.secondaryContainer
    val btnBg   = MaterialTheme.colorScheme.onBackground
    val btnText = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(48.dp))

            // ── Header ────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = "SCENT",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 6.sp,
                        fontSize      = 20.sp
                    ),
                    color = onBg
                )
                Row(
                    modifier          = Modifier.clickable(onClick = onLogin),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali ke Login",
                        tint               = muted,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text  = "MASUK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize      = 11.sp,
                            letterSpacing = 2.sp,
                            color         = muted
                        )
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            // ── Judul ─────────────────────────────────────────────────────
            Text(
                text  = "Buat Akun",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize   = 36.sp
                ),
                color = onBg
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text  = "Masukkan detail Anda untuk bergabung dengan atelier digital kami dan jelajahi koleksi eksklusif.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color      = muted,
                    lineHeight = 22.sp
                )
            )

            Spacer(Modifier.height(40.dp))

            // ── Field Nama ────────────────────────────────────────────────
            AuthFormField(
                label       = "NAMA LENGKAP",
                value       = name,
                onChange    = { name = it },
                placeholder = "ALEXANDER VOGUE",
                isUpperCase = true,
                inputBg     = inputBg,
                textColor   = onBg,
                mutedColor  = muted,
                labelColor  = label
            )

            Spacer(Modifier.height(24.dp))

            // ── Field Email ───────────────────────────────────────────────
            AuthFormField(
                label        = "ALAMAT EMAIL",
                value        = email,
                onChange     = { email = it },
                placeholder  = "EMAIL@ATELIER.COM",
                keyboardType = KeyboardType.Email,
                isUpperCase  = true,
                inputBg      = inputBg,
                textColor    = onBg,
                mutedColor   = muted,
                labelColor   = label
            )

            Spacer(Modifier.height(24.dp))

            // ── Field Password ────────────────────────────────────────────
            Text(
                text  = "KATA SANDI",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize      = 10.sp,
                    letterSpacing = 2.sp,
                    color         = label
                )
            )
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(inputBg)
                    .padding(horizontal = 14.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value                = password,
                        onValueChange        = { password = it },
                        textStyle            = MaterialTheme.typography.bodyMedium.copy(
                            color    = onBg,
                            fontSize = 16.sp
                        ),
                        // FIX: gunakan uiState.showRegisterPass untuk toggle visibility
                        visualTransformation = if (uiState.showRegisterPass)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        cursorBrush     = SolidColor(ScentGold),
                        singleLine      = true,
                        modifier        = Modifier.weight(1f),
                        decorationBox   = { inner ->
                            if (password.isEmpty()) {
                                Text(
                                    text  = "••••••••",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color    = muted,
                                        fontSize = 16.sp
                                    )
                                )
                            }
                            inner()
                        }
                    )
                    // FIX: langsung panggil viewModel.toggleRegisterPasswordVisibility()
                    Icon(
                        imageVector        = if (uiState.showRegisterPass)
                            Icons.Default.VisibilityOff
                        else
                            Icons.Default.Visibility,
                        contentDescription = "Toggle visibilitas password",
                        tint               = muted,
                        modifier           = Modifier
                            .size(20.dp)
                            .clickable { viewModel.toggleRegisterPasswordVisibility() }
                    )
                }
            }

            // ── Error Message ─────────────────────────────────────────────
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

            // ── Tombol Daftar ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(btnBg)
                    // FIX: panggil viewModel.register() langsung, bukan extension function kosong
                    .clickable(enabled = !uiState.isLoading) {
                        viewModel.register(onSuccess = onRegisterSuccess)
                    }
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color       = btnText,
                        modifier    = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text  = "DAFTAR",
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
    }
}

// ── Reusable Form Field ────────────────────────────────────────────────────────
@Composable
private fun AuthFormField(
    label        : String,
    value        : String,
    onChange     : (String) -> Unit,
    placeholder  : String      = "",
    keyboardType : KeyboardType = KeyboardType.Text,
    isUpperCase  : Boolean      = false,
    inputBg      : androidx.compose.ui.graphics.Color,
    textColor    : androidx.compose.ui.graphics.Color,
    mutedColor   : androidx.compose.ui.graphics.Color,
    labelColor   : androidx.compose.ui.graphics.Color
) {
    Column {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize      = 10.sp,
                letterSpacing = 2.sp,
                color         = labelColor
            )
        )
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(inputBg)
                .padding(horizontal = 14.dp, vertical = 16.dp)
        ) {
            BasicTextField(
                value           = value,
                onValueChange   = onChange,
                textStyle       = MaterialTheme.typography.bodyMedium.copy(
                    color         = textColor,
                    fontSize      = 16.sp,
                    letterSpacing = if (isUpperCase) 1.sp else 0.sp
                ),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                cursorBrush     = SolidColor(ScentGold),
                singleLine      = true,
                modifier        = Modifier.fillMaxWidth(),
                decorationBox   = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text  = placeholder,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color         = mutedColor,
                                fontSize      = 16.sp,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                    inner()
                }
            )
        }
    }
}
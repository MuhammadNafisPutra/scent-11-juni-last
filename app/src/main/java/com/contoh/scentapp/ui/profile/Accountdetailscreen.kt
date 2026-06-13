package com.contoh.scentapp.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.contoh.scentapp.ui.theme.*

@Composable
fun AccountDetailScreen(
    onBack    : () -> Unit = {},
    viewModel: ProfileViewModel = viewModel(
        factory = com.contoh.scentapp.di.ViewModelFactory.profileFactory(
            LocalContext.current.applicationContext as android.app.Application
        )
    )
) {
    val uiState             by viewModel.uiState.collectAsStateWithLifecycle()
    val updatePasswordState by viewModel.updatePasswordState.collectAsStateWithLifecycle()

    var name            by rememberSaveable { mutableStateOf("") }
    var email           by rememberSaveable { mutableStateOf("") }
    var address         by rememberSaveable { mutableStateOf("") }
    var photoUri        by rememberSaveable { mutableStateOf<Uri?>(null) }
    var showPhotoDialog by remember { mutableStateOf(false) }
    var showUpdatePasswordDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Tutup dialog otomatis saat password berhasil diubah
    LaunchedEffect(updatePasswordState) {
        if (updatePasswordState is UpdatePasswordState.Success) {
            showUpdatePasswordDialog = false
            viewModel.resetUpdatePasswordState()
            snackbarHostState.showSnackbar("Password berhasil diubah")
        }
    }

    // Sync dari Firestore saat data loaded
    LaunchedEffect(key1 = uiState.fullName, key2 = uiState.email, key3 = uiState.address) {
        if (uiState.fullName.isNotBlank()) name  = uiState.fullName
        if (uiState.email.isNotBlank())    email = uiState.email
        if (uiState.address.isNotBlank())  address = uiState.address
    }

    // ✅ FIX: Navigasi balik HANYA setelah upload + Firestore selesai
    val profileUpdateSuccess by viewModel.profileUpdateSuccess.collectAsStateWithLifecycle()
    LaunchedEffect(profileUpdateSuccess) {
        if (profileUpdateSuccess) {
            viewModel.resetProfileUpdateSuccess()
            onBack()
        }
    }

    val listState = rememberLazyListState()
    val context   = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { photoUri = it } }

    // ✅ FIX: rememberSaveable agar URI tidak hilang saat recomposition
    var cameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) cameraUri?.let { photoUri = it }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                state          = listState,
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // ── Top Bar ───────────────────────────────────────────────────
                item(key = "topbar") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint               = MaterialTheme.colorScheme.onBackground,
                            modifier           = Modifier
                                .size(24.dp)
                                .clickable(onClick = onBack)
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text  = "Detail Akun",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize   = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // ── Avatar ────────────────────────────────────────────────────
                item(key = "avatar") {
                    Column(
                        modifier            = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box {
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .clickable { showPhotoDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                val imageData = photoUri
                                    ?: uiState.profileImageUrl.takeIf { it.isNotBlank() }

                                if (imageData != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(imageData)
                                            .crossfade(true)
                                            // ✅ FIX: Nonaktifkan cache agar foto baru selalu tampil
                                            .diskCachePolicy(CachePolicy.DISABLED)
                                            .memoryCachePolicy(CachePolicy.DISABLED)
                                            .build(),
                                        contentDescription = "Foto profil",
                                        contentScale       = ContentScale.Crop,
                                        modifier           = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector        = Icons.Default.Person,
                                        contentDescription = null,
                                        tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                        modifier           = Modifier.size(56.dp)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.onBackground)
                                    .align(Alignment.BottomEnd)
                                    .clickable { showPhotoDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector        = Icons.Default.Edit,
                                    contentDescription = "Ganti foto",
                                    tint               = MaterialTheme.colorScheme.background,
                                    modifier           = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text  = "GANTI FOTO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize      = 10.sp,
                                letterSpacing = 2.sp,
                                color         = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                            )
                        )
                    }
                }

                // ── Nama ──────────────────────────────────────────────────────
                item(key = "nama") {
                    AccountFormField(
                        label    = "NAMA LENGKAP",
                        value    = name,
                        onChange = { name = it },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    HorizontalDivider(
                        color     = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.5.dp,
                        modifier  = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // ── Email ─────────────────────────────────────────────────────
                item(key = "email") {
                    AccountFormField(
                        label        = "EMAIL",
                        value        = email,
                        onChange     = { email = it },
                        keyboardType = KeyboardType.Email,
                        modifier     = Modifier.padding(horizontal = 20.dp)
                    )
                    HorizontalDivider(
                        color     = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.5.dp,
                        modifier  = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // ── Password ──────────────────────────────────────────────────
                item(key = "password") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text  = "PASSWORD",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize      = 10.sp,
                                    letterSpacing = 1.5.sp,
                                    color         = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text  = "••••••••••••",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color      = MaterialTheme.colorScheme.onBackground,
                                    fontSize   = 18.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .clickable { showUpdatePasswordDialog = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text  = "GANTI",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize      = 10.sp,
                                    letterSpacing = 1.5.sp,
                                    fontWeight    = FontWeight.Bold,
                                    color         = MaterialTheme.colorScheme.onBackground
                                )
                            )
                        }
                    }
                    HorizontalDivider(
                        color     = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.5.dp,
                        modifier  = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // ── Alamat ────────────────────────────────────────────────────
                item(key = "alamat") {
                    AddressFormField(
                        value    = address,
                        onChange = { address = it },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    HorizontalDivider(
                        color     = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.5.dp,
                        modifier  = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // ── Security note ─────────────────────────────────────────────
                item(key = "security") {
                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                0.5.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Security,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            modifier           = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text  = "Keamanan Akun",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text  = "Informasi pribadi Anda dienkripsi dengan standar industri atelier yang ketat.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // ── Tombol Simpan ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .navigationBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.onBackground)
                        // ✅ FIX: onBack() DIHAPUS — navigasi dihandle LaunchedEffect
                        .clickable {
                            viewModel.updateProfileWithPhoto(
                                fullName = name,
                                email    = email,
                                address  = address,
                                photoUri = photoUri
                            )
                        }
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = "SIMPAN PERUBAHAN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize      = 12.sp,
                            letterSpacing = 2.sp,
                            fontWeight    = FontWeight.Bold,
                            color         = MaterialTheme.colorScheme.background
                        )
                    )
                }
            }
        }
    }

    // ── Dialog Update Password ────────────────────────────────────────────
    if (showUpdatePasswordDialog) {
        UpdatePasswordDialog(
            updatePasswordState = updatePasswordState,
            onDismiss = {
                showUpdatePasswordDialog = false
                viewModel.resetUpdatePasswordState()
            },
            onConfirm = { currentPwd, newPwd, confirmPwd ->
                viewModel.updatePassword(currentPwd, newPwd, confirmPwd)
            }
        )
    }

    // ── Dialog Pilih Foto ──────────────────────────────────────────────────
    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            containerColor   = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    "Ganti Foto Profil",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                showPhotoDialog = false
                                galleryLauncher.launch("image/*")
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null, tint = ScentGold, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Pilih dari Galeri", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                showPhotoDialog = false
                                val file = java.io.File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
                                val uri = androidx.core.content.FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    file
                                )
                                cameraUri = uri
                                cameraLauncher.launch(uri)
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = ScentGold, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Ambil Foto", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground))
                    }
                    if (photoUri != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { photoUri = null; showPhotoDialog = false }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DeleteOutline, null, tint = Color(0xFFCF6679), modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Hapus Foto", style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFCF6679)))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoDialog = false }) {
                    Text(
                        "BATAL",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )
                }
            }
        )
    }
}

// ── Dialog Update Password ─────────────────────────────────────────────────────
@Composable
private fun UpdatePasswordDialog(
    updatePasswordState : UpdatePasswordState,
    onDismiss           : () -> Unit,
    onConfirm           : (String, String, String) -> Unit
) {
    var currentPassword     by remember { mutableStateOf("") }
    var newPassword         by remember { mutableStateOf("") }
    var confirmPassword     by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword     by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    val isLoading = updatePasswordState is UpdatePasswordState.Loading

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        containerColor   = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text  = "Ubah Password",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier            = Modifier.fillMaxWidth()
            ) {
                PasswordInputField(
                    label         = "Password Saat Ini",
                    value         = currentPassword,
                    onValueChange = { currentPassword = it },
                    isVisible     = showCurrentPassword,
                    onToggle      = { showCurrentPassword = !showCurrentPassword },
                    enabled       = !isLoading
                )
                PasswordInputField(
                    label         = "Password Baru",
                    value         = newPassword,
                    onValueChange = { newPassword = it },
                    isVisible     = showNewPassword,
                    onToggle      = { showNewPassword = !showNewPassword },
                    enabled       = !isLoading
                )
                PasswordInputField(
                    label         = "Konfirmasi Password Baru",
                    value         = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    isVisible     = showConfirmPassword,
                    onToggle      = { showConfirmPassword = !showConfirmPassword },
                    isError       = confirmPassword.isNotEmpty() && newPassword != confirmPassword,
                    errorMessage  = "Password tidak cocok",
                    enabled       = !isLoading
                )
                if (updatePasswordState is UpdatePasswordState.Error) {
                    Text(
                        text  = updatePasswordState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color    = ScentGold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(currentPassword, newPassword, confirmPassword) },
                enabled = !isLoading
            ) {
                Text(
                    text  = "SIMPAN",
                    color = ScentGold,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                    )
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { if (!isLoading) onDismiss() },
                enabled = !isLoading
            ) {
                Text(
                    text  = "BATAL",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                    )
                )
            }
        }
    )
}

@Composable
private fun PasswordInputField(
    label         : String,
    value         : String,
    onValueChange : (String) -> Unit,
    isVisible     : Boolean,
    onToggle      : () -> Unit,
    enabled       : Boolean = true,
    isError       : Boolean = false,
    errorMessage  : String  = ""
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label) },
        enabled       = enabled,
        singleLine    = true,
        isError       = isError,
        supportingText = {
            if (isError && errorMessage.isNotEmpty()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }
        },
        visualTransformation = if (isVisible) VisualTransformation.None
        else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon  = {
            IconButton(onClick = onToggle, enabled = enabled) {
                Icon(
                    imageVector        = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (isVisible) "Sembunyikan" else "Tampilkan",
                    tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AccountFormField(
    label        : String,
    value        : String,
    onChange     : (String) -> Unit,
    keyboardType : KeyboardType = KeyboardType.Text,
    modifier     : Modifier     = Modifier
) {
    Column(modifier = modifier.padding(vertical = 16.dp)) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize      = 10.sp,
                letterSpacing = 1.5.sp,
                color         = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        )
        Spacer(Modifier.height(8.dp))
        BasicTextField(
            value           = value,
            onValueChange   = onChange,
            textStyle       = MaterialTheme.typography.titleMedium.copy(
                color      = MaterialTheme.colorScheme.onBackground,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Normal
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            cursorBrush     = SolidColor(ScentGold),
            modifier        = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AddressFormField(
    value    : String,
    onChange : (String) -> Unit,
    modifier : Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector        = Icons.Default.LocationOn,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier           = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text  = "ALAMAT PENGIRIMAN",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize      = 10.sp,
                    letterSpacing = 1.5.sp,
                    color         = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            )
        }
        Spacer(Modifier.height(8.dp))
        BasicTextField(
            value         = value,
            onValueChange = onChange,
            textStyle     = MaterialTheme.typography.titleMedium.copy(
                color      = MaterialTheme.colorScheme.onBackground,
                fontSize   = 16.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 24.sp
            ),
            cursorBrush   = SolidColor(ScentGold),
            maxLines      = 4,
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text  = "Masukkan alamat lengkap, kelurahan, kota...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                fontSize   = 16.sp,
                                lineHeight = 24.sp
                            )
                        )
                    }
                    inner()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
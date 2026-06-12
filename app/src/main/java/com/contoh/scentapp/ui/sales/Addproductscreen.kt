package com.contoh.scentapp.ui.sales

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.contoh.scentapp.data.model.Parfum
import com.contoh.scentapp.ui.theme.*
import java.io.File

private val aromaFamilies = listOf("Woody", "Floral", "Oriental", "Citrus", "Gourmand", "Aquatic")
private val sizeOptions   = listOf("30", "50", "100")
private val usageOptions  = listOf("SIANG", "MALAM", "KEDUANYA")
private val bankOptions   = listOf("BCA", "BNI", "BRI", "Mandiri", "BSI", "Permata", "CIMB Niaga", "Lainnya")
private val walletOptions = listOf("GoPay", "OVO", "DANA", "ShopeePay", "LinkAja", "Lainnya")

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    onBack      : () -> Unit         = {},
    viewModel   : AddProductViewModel = viewModel()
) {
    val context     = LocalContext.current
    val state       by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Form state
    var namaParfum       by rememberSaveable { mutableStateOf("") }
    var brandParfum      by rememberSaveable { mutableStateOf("") }
    var deskripsi        by rememberSaveable { mutableStateOf("") }
    var hargaPenuh       by rememberSaveable { mutableStateOf("") }
    var hargaDecant      by rememberSaveable { mutableStateOf("") }
    var isDecantAvail    by rememberSaveable { mutableStateOf(false) }
    var selectedAroma    by rememberSaveable { mutableStateOf("Woody") }
    var jumlahStok       by rememberSaveable { mutableStateOf("") }
    var selectedSize     by rememberSaveable { mutableStateOf("50") }
    val aromaChips       = remember { mutableStateListOf("OUD", "BERGAMOT") }
    var newChipInput     by rememberSaveable { mutableStateOf("") }
    var showChipInput    by rememberSaveable { mutableStateOf(false) }
    var showPhotoDialog  by rememberSaveable { mutableStateOf(false) }
    var imageUri         by rememberSaveable { mutableStateOf<String?>(null) }
    var expandAromaMenu  by rememberSaveable { mutableStateOf(false) }
    var expandBankMenu   by rememberSaveable { mutableStateOf(false) }
    var expandWalletMenu by rememberSaveable { mutableStateOf(false) }
    var nomorRekening    by rememberSaveable { mutableStateOf("") }
    var namaRekening     by rememberSaveable { mutableStateOf("") }
    var selectedBank     by rememberSaveable { mutableStateOf("BCA") }
    var nomorWallet      by rememberSaveable { mutableStateOf("") }
    var selectedWallet   by rememberSaveable { mutableStateOf("") }
    var cameraUri        by remember { mutableStateOf<Uri?>(null) }

    // Kembali otomatis saat sukses
    LaunchedEffect(state) {
        if (state is AddProductState.Success) {
            viewModel.resetState()
            onBack()
        }
    }

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { imageUri = it.toString() } }

    // Camera picker
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) cameraUri?.let { imageUri = it.toString() } }

    // ── TAMBAHAN: Permission launcher untuk kamera ─────────────────────────
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val photoFile = File.createTempFile("scent_photo_", ".jpg", context.cacheDir)
            val uri = FileProvider.getUriForFile(
                context, "${context.packageName}.provider", photoFile
            )
            cameraUri = uri
            cameraLauncher.launch(uri)
        }
        // Kalau ditolak, tidak crash — diam saja
    }

    fun launchCamera() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Dialog pilih sumber foto
    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            containerColor   = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    "Pilih Sumber Foto",
                    style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onBackground)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .clickable { showPhotoDialog = false; galleryLauncher.launch("image/*") }
                            .padding(16.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null, tint = ScentGold, modifier = Modifier.size(24.dp))
                        Column {
                            Text("Galeri", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium))
                            Text("Pilih foto dari galeri", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)))
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            // ── PERUBAHAN: pakai launchCamera() yang sudah wrap permission ──
                            .clickable { showPhotoDialog = false; launchCamera() }
                            .padding(16.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = ScentGold, modifier = Modifier.size(24.dp))
                        Column {
                            Text("Kamera", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium))
                            Text("Ambil foto baru", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoDialog = false }) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                }
            }
        )
    }

    // Error Snackbar
    if (state is AddProductState.Error) {
        val message = (state as AddProductState.Error).message
        AlertDialog(
            onDismissRequest = { viewModel.resetState() },
            containerColor   = MaterialTheme.colorScheme.surface,
            title = { Text("Gagal", style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onBackground)) },
            text  = { Text(message, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground)) },
            confirmButton = {
                TextButton(onClick = { viewModel.resetState() }) {
                    Text("OK", color = ScentGold)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 120.dp)
        ) {

            // ── Top Bar ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, "Kembali",
                    tint     = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp).clickable(onClick = onBack)
                )
                Text("SCENT", style = MaterialTheme.typography.titleLarge.copy(letterSpacing = 6.sp, fontSize = 18.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.size(22.dp))
            }

            // ── Header ────────────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text("MANAJEMEN INVENTARIS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)))
                Spacer(Modifier.height(6.dp))
                Text("Tambah Produk Baru", style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold, fontSize = 26.sp), color = MaterialTheme.colorScheme.onBackground)
            }

            // ── Upload Foto ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, if (imageUri != null) ScentGold.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clickable { showPhotoDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri, contentDescription = "Foto produk",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                    )
                    Box(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(14.dp))
                            Text("GANTI", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onBackground))
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AddAPhoto, "Upload", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), modifier = Modifier.size(40.dp))
                        Text("UNGGAH GAMBAR PRODUK", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)))
                        Text("Ketuk untuk memilih dari galeri atau kamera", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 11.sp))
                    }
                }
            }

            // ── Nama & Brand ──────────────────────────────────────────────────
            ProductFormField(label = "NAMA PARFUM", value = namaParfum, onChange = { namaParfum = it }, placeholder = "contoh: Noir Éphémère", modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
            ProductFormField(label = "BRAND", value = brandParfum, onChange = { brandParfum = it }, placeholder = "contoh: Atelier V", modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))

            // ── Deskripsi ─────────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text("DESKRIPSI PARFUM", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)))
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    BasicTextField(
                        value = deskripsi, onValueChange = { deskripsi = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground, lineHeight = 22.sp),
                        cursorBrush = SolidColor(ScentGold), minLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (deskripsi.isEmpty()) Text("Gambarkan karakter dan jiwa dari wewangian ini...", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), lineHeight = 22.sp))
                            inner()
                        }
                    )
                }
            }

            // ── Harga Penuh ───────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text("HARGA PENUH (RP)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)))
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Rp", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)))
                        Spacer(Modifier.width(8.dp))
                        BasicTextField(
                            value = hargaPenuh, onValueChange = { hargaPenuh = it },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            cursorBrush = SolidColor(ScentGold), singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                if (hargaPenuh.isEmpty()) Text("250.000", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), fontSize = 16.sp))
                                inner()
                            }
                        )
                    }
                }
            }

            // ── Decant Toggle ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("TERSEDIA SEBAGAI DECANT", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)))
                    Spacer(Modifier.height(2.dp))
                    Text("Jual dalam ukuran kecil (5ml, 10ml, dst)", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), fontSize = 11.sp))
                }
                Switch(
                    checked = isDecantAvail, onCheckedChange = { isDecantAvail = it },
                    colors  = SwitchDefaults.colors(
                        checkedThumbColor   = MaterialTheme.colorScheme.background,
                        checkedTrackColor   = ScentGold,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }

            if (isDecantAvail) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    Text("HARGA DECANT (RP)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)))
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                            .border(1.dp, ScentGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Rp", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)))
                            Spacer(Modifier.width(8.dp))
                            BasicTextField(
                                value = hargaDecant, onValueChange = { hargaDecant = it },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                cursorBrush = SolidColor(ScentGold), singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { inner ->
                                    if (hargaDecant.isEmpty()) Text("25.000", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), fontSize = 16.sp))
                                    inner()
                                }
                            )
                        }
                    }
                }
            }

            // ── Aroma Family ──────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text("WANGI (OLFACTORY FAMILY)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)))
                Spacer(Modifier.height(10.dp))
                ExposedDropdownMenuBox(expanded = expandAromaMenu, onExpandedChange = { expandAromaMenu = it }) {
                    TextField(
                        value = selectedAroma, onValueChange = {}, readOnly = true,
                        trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), modifier = Modifier.size(20.dp)) },
                        colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer, unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer, focusedTextColor = MaterialTheme.colorScheme.onBackground, unfocusedTextColor = MaterialTheme.colorScheme.onBackground, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    )
                    ExposedDropdownMenu(expanded = expandAromaMenu, onDismissRequest = { expandAromaMenu = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
                        aromaFamilies.forEach { aroma ->
                            DropdownMenuItem(text = { Text(aroma, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground)) }, onClick = { selectedAroma = aroma; expandAromaMenu = false })
                        }
                    }
                }
            }

            // ── Notes Aroma ───────────────────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp).fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text("CATATAN AROMA (NOTES)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)))
                Spacer(Modifier.height(4.dp))
                Text("Tambahkan bahan-bahan aroma utama", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), fontSize = 11.sp))
                Spacer(Modifier.height(12.dp))
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    aromaChips.forEach { chip ->
                        Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.secondaryContainer).padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(chip, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onBackground))
                                Icon(Icons.Default.Close, "Hapus", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), modifier = Modifier.size(12.dp).clickable { aromaChips.remove(chip) })
                            }
                        }
                    }
                    if (showChipInput) {
                        BasicTextField(
                            value = newChipInput, onValueChange = { newChipInput = it },
                            textStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onBackground, fontSize = 12.sp),
                            cursorBrush = SolidColor(ScentGold), singleLine = true,
                            modifier = Modifier.clip(RoundedCornerShape(6.dp)).border(1.dp, ScentGold, RoundedCornerShape(6.dp)).padding(horizontal = 12.dp, vertical = 6.dp).width(100.dp)
                        )
                    }
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(6.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                            .clickable {
                                if (showChipInput && newChipInput.isNotBlank()) {
                                    aromaChips.add(newChipInput.uppercase()); newChipInput = ""; showChipInput = false
                                } else { showChipInput = !showChipInput }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(if (showChipInput && newChipInput.isNotBlank()) "✓ SIMPAN" else "+ TAMBAH NOTE",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)))
                    }
                }
            }

            // ── Stok & Ukuran ─────────────────────────────────────────────────
            ProductFormField(label = "JUMLAH STOK", value = jumlahStok, onChange = { jumlahStok = it }, placeholder = "48", keyboardType = KeyboardType.Number, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text("UKURAN BOTOL (ML)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)))
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    sizeOptions.forEach { size ->
                        val isSelected = size == selectedSize
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent)
                                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .clickable { selectedSize = size }.padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(size, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)))
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        // ── Tombol Simpan / Loading ────────────────────────────────────────────
        Box(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding()
        ) {
            val isFormValid = namaParfum.isNotBlank() && hargaPenuh.isNotBlank()
            val isLoading   = state is AddProductState.Loading

            Box(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when {
                            isLoading   -> MaterialTheme.colorScheme.outlineVariant
                            isFormValid -> MaterialTheme.colorScheme.onBackground
                            else        -> MaterialTheme.colorScheme.outlineVariant
                        }
                    )
                    .clickable(enabled = isFormValid && !isLoading) {
                        val parfum = Parfum(
                            name             = namaParfum,
                            brand            = brandParfum,
                            price            = hargaPenuh.replace(".", "").replace(",", "").toLongOrNull() ?: 0L,
                            decantPrice      = hargaDecant.replace(".", "").replace(",", "").toLongOrNull() ?: 0L,
                            stock            = jumlahStok.toIntOrNull() ?: 0,
                            description      = deskripsi,
                            olfactoryFamily  = selectedAroma,
                            topNotes         = aromaChips.toList(),
                            sizes            = listOf(selectedSize.toIntOrNull() ?: 50),
                            isDecantAvailable = isDecantAvail
                        )
                        viewModel.saveProduct(
                            context  = context,
                            imageUri = imageUri?.let { Uri.parse(it) },
                            parfum   = parfum
                        )
                    }
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = ScentGold, strokeWidth = 2.dp)
                        Text("MENGUPLOAD...", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)))
                    }
                } else {
                    Text("TAMBAH PRODUK", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold, color = if (isFormValid) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)))
                }
            }
        }
    }
}

@Composable
private fun ProductFormField(
    label        : String,
    value        : String,
    onChange     : (String) -> Unit,
    placeholder  : String       = "",
    keyboardType : KeyboardType = KeyboardType.Text,
    modifier     : Modifier     = Modifier
) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)))
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            BasicTextField(
                value = value, onValueChange = onChange,
                textStyle       = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                cursorBrush     = SolidColor(ScentGold), singleLine = true,
                modifier        = Modifier.fillMaxWidth(),
                decorationBox   = { inner ->
                    if (value.isEmpty()) Text(placeholder, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), fontSize = 16.sp))
                    inner()
                }
            )
        }
    }
}
package com.contoh.scentapp.ui.profile

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.contoh.scentapp.data.remote.dto.CityDto
import com.contoh.scentapp.data.remote.dto.ProvinceDto
import com.contoh.scentapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShippingAddressScreen(
    onBack : () -> Unit = {},
    viewModel: ShippingAddressViewModel = viewModel()
) {
    var namaPenerima   by rememberSaveable { mutableStateOf("") }
    var noTelepon      by rememberSaveable { mutableStateOf("") }
    var kodePos        by rememberSaveable { mutableStateOf("") }
    var alamatLengkap  by rememberSaveable { mutableStateOf("") }
    var labelAlamat    by rememberSaveable { mutableStateOf("RUMAH") }
    var isAlamatUtama  by rememberSaveable { mutableStateOf(false) }
    
    val provinces by viewModel.provinces.collectAsState()
    val cities by viewModel.cities.collectAsState()
    
    var selectedProvince by remember { mutableStateOf<ProvinceDto?>(null) }
    var selectedCity by remember { mutableStateOf<CityDto?>(null) }
    
    var expandedProvince by remember { mutableStateOf(false) }
    var expandedCity by remember { mutableStateOf(false) }

    val listState       = rememberLazyListState()
    val labelOptions = listOf("RUMAH", "KANTOR", "LAINNYA")

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier           = Modifier
                            .size(24.dp)
                            .clickable(onClick = onBack)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text  = "SHIPPING ADDRESS",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight    = FontWeight.Bold,
                            fontSize      = 14.sp,
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            item(key = "header") {
                Column(
                    modifier = Modifier.padding(
                        start  = 20.dp,
                        end    = 20.dp,
                        top    = 8.dp,
                        bottom = 28.dp
                    )
                ) {
                    Text(
                        text  = "INFORMASI PENGIRIMAN",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize   = 26.sp,
                            lineHeight = 32.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text  = "Masukkan detail alamat untuk pengiriman pesanan Anda.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            lineHeight = 22.sp
                        )
                    )
                }
            }
            item(key = "nama") {
                AddressFormField(
                    label       = "NAMA PENERIMA",
                    value       = namaPenerima,
                    onChange    = { namaPenerima = it },
                    placeholder = "Contoh: Adrian Wijaya",
                    modifier    = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
            item(key = "telepon") {
                AddressFormField(
                    label           = "NOMOR TELEPON",
                    value           = noTelepon,
                    onChange        = { noTelepon = it },
                    placeholder     = "0812 3456 7890",
                    keyboardType    = KeyboardType.Phone,
                    modifier        = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
            
            // PROVINCE DROPDOWN
            item(key = "provinsi") {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(
                        text  = "PROVINSI",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize      = 10.sp,
                            letterSpacing = 1.5.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    )
                    Spacer(Modifier.height(10.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedProvince,
                        onExpandedChange = { expandedProvince = !expandedProvince }
                    ) {
                        OutlinedTextField(
                            value = selectedProvince?.name ?: "Pilih Provinsi",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProvince) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedProvince,
                            onDismissRequest = { expandedProvince = false }
                        ) {
                            provinces.forEach { province ->
                                DropdownMenuItem(
                                    text = { Text(province.name) },
                                    onClick = {
                                        selectedProvince = province
                                        expandedProvince = false
                                        selectedCity = null
                                        viewModel.fetchCities(province.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // CITY AND POSTAL CODE
            item(key = "kota_kodepos") {
                Row(
                    modifier              = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text(
                            text  = "KOTA / KABUPATEN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize      = 10.sp,
                                letterSpacing = 1.5.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        )
                        Spacer(Modifier.height(10.dp))
                        ExposedDropdownMenuBox(
                            expanded = expandedCity,
                            onExpandedChange = { if(cities.isNotEmpty()) expandedCity = !expandedCity }
                        ) {
                            OutlinedTextField(
                                value = selectedCity?.name ?: "Pilih Kota",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCity) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedCity,
                                onDismissRequest = { expandedCity = false }
                            ) {
                                cities.forEach { city ->
                                    DropdownMenuItem(
                                        text = { Text(city.name) },
                                        onClick = {
                                            selectedCity = city
                                            expandedCity = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    AddressFormField(
                        label       = "KODE POS",
                        value       = kodePos,
                        onChange    = { kodePos = it },
                        placeholder = "40123",
                        keyboardType = KeyboardType.Number,
                        modifier    = Modifier.weight(1f)
                    )
                }
            }

            item(key = "alamat") {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text  = "ALAMAT LENGKAP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize      = 10.sp,
                            letterSpacing = 1.5.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    )
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            .padding(14.dp)
                    ) {
                        BasicTextField(
                            value         = alamatLengkap,
                            onValueChange = { alamatLengkap = it },
                            textStyle     = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            cursorBrush   = SolidColor(ScentGold),
                            minLines      = 3,
                            modifier      = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                if (alamatLengkap.isEmpty()) {
                                    Text(
                                        text  = "Nama jalan, nomor rumah, blok, atau unit apartemen",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                            lineHeight = 22.sp
                                        )
                                    )
                                }
                                inner()
                            }
                        )
                    }
                }
            }
            item(key = "label") {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text  = "LABEL ALAMAT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize      = 10.sp,
                            letterSpacing = 1.5.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        labelOptions.forEach { option ->
                            val isSelected = option == labelAlamat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)
                                    )
                                    .clickable { labelAlamat = option }
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text  = option,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize      = 10.sp,
                                        letterSpacing = 1.5.sp,
                                        fontWeight    = FontWeight.Bold,
                                        color         = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
            item(key = "utama") {
                Row(
                    modifier          = Modifier
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clickable { isAlamatUtama = !isAlamatUtama },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked         = isAlamatUtama,
                        onCheckedChange = { isAlamatUtama = it },
                        colors          = CheckboxDefaults.colors(
                            checkedColor        = MaterialTheme.colorScheme.onBackground,
                            uncheckedColor      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            checkmarkColor      = MaterialTheme.colorScheme.background
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text  = "Jadikan Alamat Utama",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }
        }
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
                    .clickable { 
                        if (namaPenerima.isBlank() || noTelepon.isBlank() || selectedCity == null || selectedProvince == null || alamatLengkap.isBlank()) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Harap lengkapi semua data alamat")
                            }
                        } else {
                            val fullAddress = "$namaPenerima - $noTelepon\n$alamatLengkap, ${selectedCity?.name}, ${selectedProvince?.name}"
                            viewModel.saveDestinationCity(selectedCity!!.id)
                            viewModel.saveFullAddress(fullAddress)
                            onBack() 
                        }
                    }
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = "SIMPAN ALAMAT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize      = 12.sp,
                        letterSpacing = 2.sp,
                        fontWeight    = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.background
                    )
                )
            }
        }
    }
}
}

@Composable
private fun AddressFormField(
    label        : String,
    value        : String,
    onChange     : (String) -> Unit,
    placeholder  : String        = "",
    keyboardType : KeyboardType  = KeyboardType.Text,
    modifier     : Modifier      = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize      = 10.sp,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        )
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            BasicTextField(
                value           = value,
                onValueChange   = onChange,
                textStyle       = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp
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
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                fontSize = 16.sp
                            )
                        )
                    }
                    inner()
                }
            )
        }
    }
}
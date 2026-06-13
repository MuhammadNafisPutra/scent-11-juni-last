package com.contoh.scentapp.ui.sales

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.contoh.scentapp.data.model.Parfum
import com.contoh.scentapp.data.repository.ProductRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── State ─────────────────────────────────────────────────────────────────────

sealed class AddProductState {
    object Idle    : AddProductState()
    object Loading : AddProductState()
    object Success : AddProductState()
    data class Error(val message: String) : AddProductState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class AddProductViewModel(
    private val repository: ProductRepositoryImpl = ProductRepositoryImpl()
) : ViewModel() {

    private val _state = MutableStateFlow<AddProductState>(AddProductState.Idle)
    val state: StateFlow<AddProductState> = _state.asStateFlow()

    // Data produk yang sedang diedit (null = mode tambah baru)
    private val _existingParfum = MutableStateFlow<Parfum?>(null)
    val existingParfum: StateFlow<Parfum?> = _existingParfum.asStateFlow()

    /**
     * Muat data produk yang akan diedit dari Firestore.
     */
    fun loadParfumForEdit(firestoreId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = AddProductState.Loading
            val result = repository.getParfumById(firestoreId)
            if (result.isSuccess) {
                _existingParfum.value = result.getOrNull()
                _state.value = AddProductState.Idle
            } else {
                _state.value = AddProductState.Error(
                    result.exceptionOrNull()?.message ?: "Gagal memuat produk"
                )
            }
        }
    }

    /**
     * Simpan produk BARU:
     * 1. Upload gambar ke Cloudinary (jika ada)
     * 2. Simpan data Parfum ke Firestore dengan imageUrl hasil upload
     */
    fun saveProduct(
        context  : Context,
        imageUri : Uri?,
        parfum   : Parfum
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = AddProductState.Loading

            try {
                val imageUrl = if (imageUri != null) {
                    val uploadResult = repository.uploadProductImage(context, imageUri)
                    if (uploadResult.isFailure) {
                        _state.value = AddProductState.Error(
                            "Gagal upload gambar: ${uploadResult.exceptionOrNull()?.message}"
                        )
                        return@launch
                    }
                    uploadResult.getOrDefault("")
                } else {
                    ""
                }

                val result = repository.addParfum(parfum.copy(imageUrl = imageUrl))

                _state.value = if (result.isSuccess) {
                    AddProductState.Success
                } else {
                    AddProductState.Error(
                        result.exceptionOrNull()?.message ?: "Gagal menyimpan produk"
                    )
                }
            } catch (e: Exception) {
                _state.value = AddProductState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    /**
     * Update produk yang SUDAH ADA:
     * 1. Upload gambar baru ke Cloudinary (jika user memilih gambar baru)
     * 2. Jika tidak ada gambar baru, gunakan imageUrl lama
     * 3. Update dokumen di Firestore
     */
    fun updateProduct(
        context        : Context,
        imageUri       : Uri?,
        parfum         : Parfum,
        existingImageUrl: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = AddProductState.Loading

            try {
                // Hanya upload gambar baru jika user memilih gambar baru (imageUri != null)
                val imageUrl = if (imageUri != null) {
                    val uploadResult = repository.uploadProductImage(context, imageUri)
                    if (uploadResult.isFailure) {
                        _state.value = AddProductState.Error(
                            "Gagal upload gambar: ${uploadResult.exceptionOrNull()?.message}"
                        )
                        return@launch
                    }
                    uploadResult.getOrDefault(existingImageUrl)
                } else {
                    existingImageUrl // Gunakan URL gambar lama
                }

                val result = repository.updateParfum(parfum.copy(imageUrl = imageUrl))

                _state.value = if (result.isSuccess) {
                    AddProductState.Success
                } else {
                    AddProductState.Error(
                        result.exceptionOrNull()?.message ?: "Gagal memperbarui produk"
                    )
                }
            } catch (e: Exception) {
                _state.value = AddProductState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun resetState() {
        _state.value = AddProductState.Idle
    }
}

class AddProductViewModelFactory(
    private val firestoreId: String? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddProductViewModel::class.java)) {
            val vm = AddProductViewModel()
            if (!firestoreId.isNullOrBlank()) {
                vm.loadParfumForEdit(firestoreId)
            }
            return vm as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

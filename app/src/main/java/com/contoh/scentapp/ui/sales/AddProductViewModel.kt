package com.contoh.scentapp.ui.sales

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
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

    /**
     * Simpan produk baru:
     * 1. Upload gambar ke Cloudinary (jika ada)
     * 2. Simpan data Parfum ke Firestore dengan imageUrl hasil upload
     *
     * @param context   diperlukan untuk membaca Uri gambar
     * @param imageUri  Uri gambar yang dipilih dari galeri/kamera, null = tanpa gambar
     * @param parfum    data produk yang akan disimpan (imageUrl masih kosong)
     */
    fun saveProduct(
        context  : Context,
        imageUri : Uri?,
        parfum   : Parfum
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = AddProductState.Loading

            try {
                // Step 1: Upload gambar jika ada
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
                    "" // Tidak ada gambar
                }

                // Step 2: Simpan ke Firestore dengan URL gambar
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

    fun resetState() {
        _state.value = AddProductState.Idle
    }
}
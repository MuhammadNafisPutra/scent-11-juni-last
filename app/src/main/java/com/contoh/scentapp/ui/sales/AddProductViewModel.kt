package com.contoh.scentapp.ui.sales

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contoh.scentapp.domain.model.Parfum
import com.contoh.scentapp.domain.usecase.product.AddProductUseCase
import com.contoh.scentapp.domain.usecase.product.GetProductDetailUseCase
import com.contoh.scentapp.domain.usecase.product.UpdateProductUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AddProductState {
    object Idle    : AddProductState()
    object Loading : AddProductState()
    object Success : AddProductState()
    data class Error(val message: String) : AddProductState()
}

class AddProductViewModel(
    private val getProductDetailUseCase: GetProductDetailUseCase,
    private val addProductUseCase: AddProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<AddProductState>(AddProductState.Idle)
    val state: StateFlow<AddProductState> = _state.asStateFlow()

    private val _existingParfum = MutableStateFlow<Parfum?>(null)
    val existingParfum: StateFlow<Parfum?> = _existingParfum.asStateFlow()

    fun loadParfumForEdit(firestoreId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = AddProductState.Loading
            val result = getProductDetailUseCase(firestoreId)
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

    fun saveProduct(
        context  : Context,
        imageUri : Uri?,
        parfum   : Parfum
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = AddProductState.Loading

            try {
                val result = addProductUseCase(context, imageUri, parfum)

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

    fun updateProduct(
        context        : Context,
        imageUri       : Uri?,
        parfum         : Parfum,
        existingImageUrl: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = AddProductState.Loading

            try {
                val result = updateProductUseCase(context, imageUri, parfum, existingImageUrl)

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

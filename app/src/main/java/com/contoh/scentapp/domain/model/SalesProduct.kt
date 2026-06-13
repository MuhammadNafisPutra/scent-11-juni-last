package com.contoh.scentapp.domain.model

data class SalesProduct(
    val id          : Int,
    val firestoreId : String = "",       // ID dokumen Firestore
    val name        : String,
    val aromaFamily : String,
    val volume      : String,
    val stockStatus : String,
    val price       : Int,
    val stock       : Int    = 0,
    val imageUrl    : String = "",       // URL gambar dari Cloudinary
    val cardColor   : Long   = 0xFF1A1A1A,
    val accentColor : Long   = 0xFFD4A853
)

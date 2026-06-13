package com.contoh.scentapp.data.model

data class Product(
    val id           : Int,
    val firestoreId  : String       = "",   // ID dokumen Firestore
    val sellerId     : String       = "",   // UID penjual pemilik produk
    val brand        : String,
    val name         : String,
    val price        : String,
    val volume       : String,
    val imageUrl     : String       = "",   // URL gambar dari Cloudinary
    val cardColor    : Long,
    val accentColor  : Long         = 0xFFD4A853,
    val isFavorite   : Boolean      = false,
    val collection   : String       = "",
    val fullBrand    : String       = "",
    val description  : String       = "",
    val aromaProfile : List<String> = emptyList(),
    val usage        : String       = "",
    val rating       : Float        = 4.8f,
    val reviewCount  : Int          = 0
)
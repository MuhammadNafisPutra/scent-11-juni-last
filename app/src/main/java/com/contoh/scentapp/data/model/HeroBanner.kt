package com.contoh.scentapp.data.model

data class HeroBanner(
    val tag           : String,
    val title         : String,
    val description   : String,
    val gradientStart : Long = 0xFF1A1A1A,
    val gradientEnd   : Long = 0xFF0A0A0A
)
package com.contoh.scentapp.data.model

data class ParfumFilter(
    val olfactoryFamily: String? = null,
    val topNotes: List<String> = emptyList(),
    val middleNotes: List<String> = emptyList(),
    val baseNotes: List<String> = emptyList(),
    val isDecantOnly: Boolean = false,
    val isLimitedDrop: Boolean = false,
    val usage: String? = null
)
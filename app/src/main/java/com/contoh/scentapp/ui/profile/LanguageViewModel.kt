package com.contoh.scentapp.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.contoh.scentapp.data.repository.LanguageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LanguageViewModel(application: Application) : AndroidViewModel(application) {

    private val languageManager = LanguageManager.getInstance(application)

    private val _selectedLang = MutableStateFlow(languageManager.selectedLanguage)
    val selectedLang: StateFlow<String> = _selectedLang

    fun setLanguage(langCode: String) {
        _selectedLang.value = langCode
        languageManager.applyLanguage(langCode) // Simpan + terapkan sekaligus
    }
}
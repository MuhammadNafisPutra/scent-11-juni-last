package com.contoh.scentapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.contoh.scentapp.domain.usecase.profile.GetLanguageUseCase
import com.contoh.scentapp.domain.usecase.profile.SetLanguageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LanguageViewModel(
    private val getLanguageUseCase: GetLanguageUseCase,
    private val setLanguageUseCase: SetLanguageUseCase
) : ViewModel() {

    private val _selectedLang = MutableStateFlow(getLanguageUseCase())
    val selectedLang: StateFlow<String> = _selectedLang

    fun setLanguage(langCode: String) {
        _selectedLang.value = langCode
        setLanguageUseCase(langCode)
    }
}
package com.contoh.scentapp.domain.usecase.profile

import com.contoh.scentapp.data.repository.LanguageManager

class SetLanguageUseCase(private val languageManager: LanguageManager) {
    operator fun invoke(langCode: String) {
        languageManager.applyLanguage(langCode)
    }
}

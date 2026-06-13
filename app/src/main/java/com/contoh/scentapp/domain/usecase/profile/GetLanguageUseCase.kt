package com.contoh.scentapp.domain.usecase.profile

import com.contoh.scentapp.data.repository.LanguageManager

class GetLanguageUseCase(private val languageManager: LanguageManager) {
    operator fun invoke(): String {
        return languageManager.selectedLanguage
    }
}

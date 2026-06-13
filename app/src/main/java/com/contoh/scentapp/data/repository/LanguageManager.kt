package com.contoh.scentapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class LanguageManager private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var INSTANCE: LanguageManager? = null

        fun getInstance(context: Context): LanguageManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LanguageManager(context.applicationContext)
                    .also { INSTANCE = it }
            }
        }
    }

    // ── Simpan bahasa yang dipilih ────────────────────────────────────────────

    var selectedLanguage: String
        get() = prefs.getString("selected_language", "id") ?: "id"
        set(value) {
            prefs.edit().putString("selected_language", value).apply()
        }

    // ── Terapkan bahasa ke seluruh app ────────────────────────────────────────

    fun applyLanguage(languageCode: String) {
        selectedLanguage = languageCode
        val localeList = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    // ── Terapkan bahasa saat app dibuka ───────────────────────────────────────

    fun applyStoredLanguage() {
        val localeList = LocaleListCompat.forLanguageTags(selectedLanguage)
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}
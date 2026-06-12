package com.contoh.scentapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SessionManager private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("ScentPrefs", Context.MODE_PRIVATE)

    // ── Dark Mode ──────────────────────────────────────────────
    private val _isDarkMode = MutableStateFlow(sharedPreferences.getBoolean("KEY_DARK_MODE", true))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    fun setDarkMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("KEY_DARK_MODE", enabled).apply()
        _isDarkMode.value = enabled
    }

    // ── Session / Login ────────────────────────────────────────
    fun saveSession(email: String) {
        sharedPreferences.edit()
            .putString("KEY_EMAIL", email)
            .putBoolean("KEY_IS_LOGGED_IN", true)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("KEY_IS_LOGGED_IN", false)
    }

    fun getEmail(): String? {
        return sharedPreferences.getString("KEY_EMAIL", null)
    }

    fun clearSession() {
        // Hanya hapus data sesi login, bukan preferensi seperti dark mode
        sharedPreferences.edit()
            .remove("KEY_EMAIL")
            .remove("KEY_IS_LOGGED_IN")
            .apply()
        _isDarkMode.value = sharedPreferences.getBoolean("KEY_DARK_MODE", true)
    }

    companion object {
        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
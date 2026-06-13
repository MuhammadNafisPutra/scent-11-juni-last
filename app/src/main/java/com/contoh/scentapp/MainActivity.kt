package com.contoh.scentapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity  // âœ… Ganti import ini
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.contoh.scentapp.data.repository.LanguageManager
import com.contoh.scentapp.data.repository.SessionManager
import com.contoh.scentapp.ui.navigation.AppNavigation
import com.contoh.scentapp.ui.theme.ScentAppTheme

class MainActivity : AppCompatActivity() {  // âœ… Ganti ini

    companion object {
        var isDarkModeState by mutableStateOf(value = true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageManager.getInstance(this).applyStoredLanguage()

        val isLoggedIn = SessionManager.getInstance(this).isLoggedIn()

        setContent {
            ScentAppTheme(darkTheme = isDarkModeState) {
                AppNavigation(startLoggedIn = isLoggedIn)
            }
        }
    }
}
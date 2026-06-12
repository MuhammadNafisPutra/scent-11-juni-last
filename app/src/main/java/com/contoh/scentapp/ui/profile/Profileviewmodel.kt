package com.contoh.scentapp.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.contoh.scentapp.data.model.ProfileUiState
import com.contoh.scentapp.data.repository.AuthRepositoryImpl
import com.contoh.scentapp.data.repository.LanguageManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ✅ BARU: Sealed class untuk state update password
sealed class UpdatePasswordState {
    object Idle    : UpdatePasswordState()
    object Loading : UpdatePasswordState()
    object Success : UpdatePasswordState()
    data class Error(val message: String) : UpdatePasswordState()
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val languageManager = LanguageManager.getInstance(application)
    private val authRepository  = AuthRepositoryImpl.getInstance()
    private val firestore       = FirebaseFirestore.getInstance()
    private val firebaseAuth    = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            language = getLanguageLabel(languageManager.selectedLanguage)
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // ✅ BARU: State untuk proses update password
    private val _updatePasswordState = MutableStateFlow<UpdatePasswordState>(UpdatePasswordState.Idle)
    val updatePasswordState: StateFlow<UpdatePasswordState> = _updatePasswordState.asStateFlow()

    init {
        loadCurrentUser()
    }

    // ── Fetch user dari Firestore ──────────────────────────────────────────
    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    _uiState.update {
                        it.copy(
                            fullName        = user.fullName,
                            email           = user.email,
                            profileImageUrl = user.profileImageUrl
                        )
                    }
                }
            } catch (e: Exception) { }
        }
    }

    // ── Update profil ke Firestore ─────────────────────────────────────────
    fun updateProfile(fullName: String, email: String) {
        viewModelScope.launch {
            try {
                val uid = authRepository.currentUserId ?: return@launch
                firestore.collection("users").document(uid)
                    .update(mapOf("fullName" to fullName, "email" to email))
                    .await()
                _uiState.update { it.copy(fullName = fullName, email = email) }
            } catch (e: Exception) { }
        }
    }

    // ✅ BARU: Update password langsung (ganti sendPasswordReset)
    fun updatePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            _updatePasswordState.value = UpdatePasswordState.Error("Semua field harus diisi")
            return
        }
        if (newPassword != confirmPassword) {
            _updatePasswordState.value = UpdatePasswordState.Error("Password baru tidak cocok")
            return
        }
        if (newPassword.length < 6) {
            _updatePasswordState.value = UpdatePasswordState.Error("Password minimal 6 karakter")
            return
        }
        if (newPassword == currentPassword) {
            _updatePasswordState.value = UpdatePasswordState.Error("Password baru tidak boleh sama dengan password lama")
            return
        }

        viewModelScope.launch {
            _updatePasswordState.value = UpdatePasswordState.Loading
            val result = authRepository.updatePassword(currentPassword, newPassword)
            _updatePasswordState.value = if (result.isSuccess) {
                UpdatePasswordState.Success
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Gagal mengubah password"
                // Pesan Firebase lebih ramah
                val friendlyMsg = when {
                    msg.contains("wrong-password", ignoreCase = true) ||
                            msg.contains("invalid-credential", ignoreCase = true) ->
                        "Password saat ini salah"
                    msg.contains("network", ignoreCase = true) ->
                        "Periksa koneksi internet Anda"
                    else -> msg
                }
                UpdatePasswordState.Error(friendlyMsg)
            }
        }
    }

    // ✅ BARU: Reset state setelah dialog ditutup
    fun resetUpdatePasswordState() {
        _updatePasswordState.value = UpdatePasswordState.Idle
    }


    fun refreshUser() { loadCurrentUser() }

    private fun getLanguageLabel(langCode: String): String {
        return when (langCode) {
            "en" -> "ENGLISH"
            else -> "INDONESIA"
        }
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun showDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun hideDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun confirmDeleteAccount() {
        viewModelScope.launch { hideDeleteDialog() }
    }
}

class ProfileViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
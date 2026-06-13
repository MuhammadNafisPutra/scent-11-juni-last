package com.contoh.scentapp.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contoh.scentapp.ui.state.ProfileUiState
import com.contoh.scentapp.domain.usecase.auth.GetCurrentUserUseCase
import com.contoh.scentapp.domain.usecase.auth.UpdatePasswordUseCase
import com.contoh.scentapp.domain.usecase.profile.UpdateProfileUseCase
import com.contoh.scentapp.domain.usecase.profile.GetLanguageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class UpdatePasswordState {
    object Idle    : UpdatePasswordState()
    object Loading : UpdatePasswordState()
    object Success : UpdatePasswordState()
    data class Error(val message: String) : UpdatePasswordState()
}

class ProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val updatePasswordUseCase: UpdatePasswordUseCase,
    private val getLanguageUseCase: GetLanguageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            language = getLanguageLabel(getLanguageUseCase())
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _updatePasswordState = MutableStateFlow<UpdatePasswordState>(UpdatePasswordState.Idle)
    val updatePasswordState: StateFlow<UpdatePasswordState> = _updatePasswordState.asStateFlow()

    private val _profileUpdateSuccess = MutableStateFlow(false)
    val profileUpdateSuccess: StateFlow<Boolean> = _profileUpdateSuccess.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val user = getCurrentUserUseCase()
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

    fun updateProfileWithPhoto(fullName: String, email: String, photoUri: Uri?) {
        viewModelScope.launch {
            val result = updateProfileUseCase(fullName, email, photoUri)
            if (result.isSuccess) {
                val imageUrl = result.getOrNull()
                _uiState.update {
                    it.copy(
                        fullName        = fullName,
                        email           = email,
                        profileImageUrl = imageUrl ?: it.profileImageUrl
                    )
                }
                _profileUpdateSuccess.value = true
            }
        }
    }

    fun resetProfileUpdateSuccess() {
        _profileUpdateSuccess.value = false
    }

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
            val result = updatePasswordUseCase(currentPassword, newPassword)
            _updatePasswordState.value = if (result.isSuccess) {
                UpdatePasswordState.Success
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Gagal mengubah password"
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
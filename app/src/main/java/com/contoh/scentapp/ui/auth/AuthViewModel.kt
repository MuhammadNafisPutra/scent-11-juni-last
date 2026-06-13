package com.contoh.scentapp.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.contoh.scentapp.ui.state.AuthUiState
import com.contoh.scentapp.data.repository.SessionManager
import com.contoh.scentapp.domain.usecase.auth.LoginUseCase
import com.contoh.scentapp.domain.usecase.auth.RegisterUseCase
import com.contoh.scentapp.domain.usecase.auth.LogoutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    application: Application,
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase
) : AndroidViewModel(application) {

    private val sessionManager = SessionManager.getInstance(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onLoginEmailChange(value: String) {
        _uiState.update { it.copy(loginEmail = value, errorMessage = null) }
    }
    fun onLoginPasswordChange(value: String) {
        _uiState.update { it.copy(loginPassword = value, errorMessage = null) }
    }
    fun toggleLoginPasswordVisibility() {
        _uiState.update { it.copy(showLoginPass = !it.showLoginPass) }
    }

    fun onRegisterNameChange(value: String) {
        _uiState.update { it.copy(registerName = value, errorMessage = null) }
    }
    fun onRegisterEmailChange(value: String) {
        _uiState.update { it.copy(registerEmail = value, errorMessage = null) }
    }
    fun onRegisterPasswordChange(value: String) {
        _uiState.update { it.copy(registerPassword = value, errorMessage = null) }
    }
    fun toggleRegisterPasswordVisibility() {
        _uiState.update { it.copy(showRegisterPass = !it.showRegisterPass) }
    }

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.loginEmail.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email tidak boleh kosong") }
            return
        }
        if (state.loginPassword.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Kata sandi tidak boleh kosong") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = loginUseCase(state.loginEmail, state.loginPassword)
                result.onSuccess { user ->
                    sessionManager.saveSession(email = user.email)
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                    onSuccess()
                }
                result.onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Login gagal")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Login gagal")
                }
            }
        }
    }

    fun register(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.registerName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nama tidak boleh kosong") }
            return
        }
        if (state.registerEmail.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email tidak boleh kosong") }
            return
        }
        if (state.registerPassword.length < 6) {
            _uiState.update { it.copy(errorMessage = "Kata sandi minimal 6 karakter") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = registerUseCase(
                    email = state.registerEmail,
                    password = state.registerPassword,
                    fullName = state.registerName
                )
                result.onSuccess { user ->
                    sessionManager.saveSession(email = user.email)
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                    onSuccess()
                }
                result.onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Registrasi gagal")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Registrasi gagal")
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            sessionManager.clearSession()
            _uiState.update { AuthUiState() }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

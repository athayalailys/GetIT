package com.mobile.getit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.getit.domain.model.User
import com.mobile.getit.domain.repository.GetItRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object WaitingForEmail : AuthState()
    object Verified : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val repo: GetItRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private var verificationJob: Job? = null

    private suspend fun syncUserToPrefs(uid: String) {
        try {
            repo.getUserProfile(uid)?.let { profile ->
                repo.saveAccount(profile.fullName, profile.username, "", profile.type)
                repo.saveLocalProfile(profile.fullName, profile.location, profile.photoUri)
                repo.saveLocalPaymentMethods(profile.whatsapp, profile.bankName, profile.bankAcc, profile.eWalletName, profile.eWalletAcc)
                
                val isUlm = profile.email.endsWith("@mhs.ulm.ac.id") || profile.email.endsWith("@ulm.ac.id")
                repo.saveVerification(profile.isVerified || isUlm)
            }
        } catch (e: Exception) {}
    }

    fun isUserLoggedIn(): Boolean = repo.isUserLoggedIn()

    fun checkInitialAuthStatus() {
        if (_authState.value != AuthState.Idle) return
        val uid = repo.getCurrentUserUid()
        if (uid != null) {
            if (repo.isEmailVerified()) {
                viewModelScope.launch {
                    syncUserToPrefs(uid)
                    repo.setLoggedIn(true)
                    _authState.value = AuthState.Success
                }
            } else {
                _authState.value = AuthState.WaitingForEmail
                startVerificationCheck()
            }
        }
    }

    fun register(user: User) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repo.register(user).onSuccess {
                _authState.value = AuthState.WaitingForEmail
                startVerificationCheck()
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Daftar Gagal")
            }
        }
    }

    private fun startVerificationCheck() {
        verificationJob?.cancel()
        verificationJob = viewModelScope.launch {
            repo.waitForEmailVerification().onSuccess {
                val uid = repo.getCurrentUserUid() ?: ""
                syncUserToPrefs(uid)
                _authState.value = AuthState.Verified
                repo.logout() 
            }.onFailure {
                if (_authState.value == AuthState.WaitingForEmail) {
                    _authState.value = AuthState.Error(it.message ?: "Verifikasi Gagal")
                }
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            withContext(NonCancellable) {
                repo.logout()
            }
            _authState.value = AuthState.Idle
            onComplete()
        }
    }

    fun deleteAccountPermanently(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repo.deleteCurrentUser().onSuccess {
                _authState.value = AuthState.Idle
                onResult(true, null)
            }.onFailure {
                _authState.value = AuthState.Error("Gagal Hapus")
                onResult(false, it.message)
            }
        }
    }

    fun cancelRegistration() {
        verificationJob?.cancel()
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repo.deleteCurrentUser()
            _authState.value = AuthState.Idle
        }
    }

    fun login(identifier: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repo.login(identifier, pass).onSuccess {
                val uid = repo.getCurrentUserUid() ?: ""
                if (repo.isEmailVerified()) {
                    syncUserToPrefs(uid)
                    repo.setLoggedIn(true)
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.WaitingForEmail
                    startVerificationCheck()
                }
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Login Gagal")
            }
        }
    }

    fun resendEmail() { viewModelScope.launch { repo.resendVerificationEmail() } }
    fun resetAuthState() { 
        verificationJob?.cancel()
        _authState.value = AuthState.Idle 
    }
}

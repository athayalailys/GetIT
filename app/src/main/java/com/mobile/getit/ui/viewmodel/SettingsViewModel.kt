package com.mobile.getit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.getit.domain.repository.GetItRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val fullName: String = "",
    val location: String = "",
    val photoUri: String = "",
    val whatsapp: String = "",
    val bankName: String = "",
    val bankAcc: String = "",
    val eWalletName: String = "",
    val eWalletAcc: String = "",
    val isVerified: Boolean = false,
    val isDarkTheme: Boolean = false,
    val language: String = "in",
    val isLoading: Boolean = false,
    val successMessage: String? = null
)

class SettingsViewModel(private val repository: GetItRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val currentUid = repository.getCurrentUserUid()
        _uiState.update {
            it.copy(
                fullName = repository.getSavedFullName(),
                location = repository.getSavedLocation(),
                photoUri = repository.getSavedPhotoUri(),
                whatsapp = repository.getSavedWhatsapp(),
                bankName = repository.getSavedBankName(),
                bankAcc = repository.getSavedBankAccount(),
                eWalletName = repository.getSavedEWalletName(),
                eWalletAcc = repository.getSavedEWalletAccount(),
                isVerified = repository.isUserVerified()
            )
        }

        viewModelScope.launch {
            repository.isDarkMode().collect { dark ->
                _uiState.update { it.copy(isDarkTheme = dark) }
            }
        }
        viewModelScope.launch {
            repository.getLanguage().collect { lang ->
                _uiState.update { it.copy(language = lang) }
            }
        }
    }

    fun onFullNameChange(value: String) { _uiState.update { it.copy(fullName = value) } }
    fun onLocationChange(value: String) { _uiState.update { it.copy(location = value) } }
    fun onWhatsappChange(value: String) { _uiState.update { it.copy(whatsapp = value) } }
    fun onBankNameChange(value: String) { _uiState.update { it.copy(bankName = value) } }
    fun onBankAccChange(value: String) { _uiState.update { it.copy(bankAcc = value) } }
    fun onEWalletNameChange(value: String) { _uiState.update { it.copy(eWalletName = value) } }
    fun onEWalletAccChange(value: String) { _uiState.update { it.copy(eWalletAcc = value) } }

    fun updatePhoto(uri: String) {
        _uiState.update { it.copy(photoUri = uri) }
        saveProfile()
    }

    fun saveProfile() {
        viewModelScope.launch {
            val s = _uiState.value
            repository.saveLocalProfile(s.fullName, s.location, s.photoUri)
            val uid = repository.getCurrentUserUid() ?: return@launch
            repository.updateUserProfile(uid, mapOf(
                "fullName" to s.fullName,
                "location" to s.location,
                "photoUri" to s.photoUri
            ))
            syncAll()
            _uiState.update { it.copy(successMessage = "Profil Berhasil Disinkronkan!") }
        }
    }

    fun savePaymentMethods() {
        viewModelScope.launch {
            val s = _uiState.value
            repository.saveLocalPaymentMethods(s.whatsapp, s.bankName, s.bankAcc, s.eWalletName, s.eWalletAcc)
            val uid = repository.getCurrentUserUid() ?: return@launch
            repository.updateUserProfile(uid, mapOf(
                "whatsapp" to s.whatsapp,
                "bankName" to s.bankName,
                "bankAcc" to s.bankAcc,
                "eWalletName" to s.eWalletName,
                "eWalletAcc" to s.eWalletAcc
            ))
            syncAll()
            _uiState.update { it.copy(successMessage = "Metode Pembayaran Tersimpan!") }
        }
    }

    private suspend fun syncAll() {
        val s = _uiState.value
        repository.syncSellerInfoInProducts(
            s.fullName, s.location, s.photoUri,
            s.whatsapp, s.bankName, s.bankAcc, s.eWalletName, s.eWalletAcc
        )
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { repository.setDarkMode(enabled) }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch { repository.setLanguage(lang) }
    }

    fun verifyUlm(otp: String, onResult: (Boolean) -> Unit) {
        if (otp == "123456") {
            viewModelScope.launch {
                _uiState.update { it.copy(isVerified = true) }
                // Di sini biasanya simpan ke repo juga
                onResult(true)
            }
        } else {
            onResult(false)
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            onComplete()
        }
    }

    fun deleteAccount(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            repository.deleteCurrentUser().onSuccess {
                onResult(true, null)
            }.onFailure {
                onResult(false, it.message)
            }
        }
    }

    fun clearSuccessMessage() { _uiState.update { it.copy(successMessage = null) } }
}

package com.mobile.getit.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NotificationData(val title: String, val body: String, val time: Long)

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("getit_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_USERNAME = "user_username" 
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_LANG = "language"

        private const val KEY_WA = "whatsapp"
        private const val KEY_BANK_NAME = "bank_name"
        private const val KEY_BANK_ACC = "bank_acc"
        private const val KEY_EWALLET_NAME = "ewallet_name"
        private const val KEY_EWALLET_ACC = "ewallet_acc"
        private const val KEY_IS_VERIFIED = "is_verified"
        private const val KEY_USER_FULL_NAME = "user_full_name"
        private const val KEY_USER_PASSWORD = "user_password"
        private const val KEY_USER_LOCATION = "user_location"
        private const val KEY_USER_PHOTO = "user_photo"
        private const val KEY_LOGIN_TYPE = "login_type"
        private const val KEY_NOTIFICATIONS = "notifications_history"

        private val _isDarkMode = MutableStateFlow(false)
        private val _language = MutableStateFlow("in")
        private val _notifications = MutableStateFlow<List<NotificationData>>(emptyList())
        private var isInitialized = false
    }

    val isDarkModeFlow: StateFlow<Boolean> get() = _isDarkMode
    val languageFlow: StateFlow<String> get() = _language
    val notificationsFlow: StateFlow<List<NotificationData>> get() = _notifications

    init {
        if (!isInitialized) {
            _isDarkMode.value = isDarkMode()
            _language.value = getLanguage()
            _notifications.value = getNotifications()
            isInitialized = true
        }
    }

    private fun uKey(key: String): String {
        val username = getSavedUsername()
        return if (username.isNotEmpty()) "${username}_$key" else key
    }

    fun saveAccount(fullName: String, username: String, pass: String, type: String = "EMAIL") {
        prefs.edit()
            .putString(KEY_USER_USERNAME, username) 
            .putString("${username}_$KEY_USER_FULL_NAME", fullName)
            .putString("${username}_$KEY_USER_PASSWORD", pass)
            .putString("${username}_$KEY_LOGIN_TYPE", type)
            .apply()
        _notifications.value = getNotifications()
    }

    fun setLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun updateProfile(fullName: String, location: String, photoUri: String) {
        prefs.edit()
            .putString(uKey(KEY_USER_FULL_NAME), fullName)
            .putString(uKey(KEY_USER_LOCATION), location)
            .putString(uKey(KEY_USER_PHOTO), photoUri)
            .apply()
    }

    fun getFullName(): String = prefs.getString(uKey(KEY_USER_FULL_NAME), "User GetIT") ?: "User GetIT"
    fun getSavedUsername(): String = prefs.getString(KEY_USER_USERNAME, "") ?: ""
    fun getLocation(): String = prefs.getString(uKey(KEY_USER_LOCATION), "Banjarmasin") ?: "Banjarmasin"
    fun getPhotoUri(): String = prefs.getString(uKey(KEY_USER_PHOTO), "") ?: ""
    fun getLoginType(): String = prefs.getString(uKey(KEY_LOGIN_TYPE), "EMAIL") ?: "EMAIL"

    fun savePaymentMethods(whatsapp: String, bankName: String, bankAcc: String, eWalletName: String, eWalletAcc: String) {
        prefs.edit()
            .putString(uKey(KEY_WA), whatsapp)
            .putString(uKey(KEY_BANK_NAME), bankName)
            .putString(uKey(KEY_BANK_ACC), bankAcc)
            .putString(uKey(KEY_EWALLET_NAME), eWalletName)
            .putString(uKey(KEY_EWALLET_ACC), eWalletAcc)
            .apply()
    }

    fun saveVerification(isVerified: Boolean) {
        prefs.edit().putBoolean(uKey(KEY_IS_VERIFIED), isVerified).apply()
    }

    fun getWhatsapp(): String = prefs.getString(uKey(KEY_WA), "") ?: ""
    fun getBankName(): String = prefs.getString(uKey(KEY_BANK_NAME), "") ?: ""
    fun getBankAccount(): String = prefs.getString(uKey(KEY_BANK_ACC), "") ?: ""
    fun getEWalletName(): String = prefs.getString(uKey(KEY_EWALLET_NAME), "") ?: ""
    fun getEWalletAccount(): String = prefs.getString(uKey(KEY_EWALLET_ACC), "") ?: ""
    fun isVerified(): Boolean = prefs.getBoolean(uKey(KEY_IS_VERIFIED), false)

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        _isDarkMode.value = enabled
    }
    fun isDarkMode(): Boolean = prefs.getBoolean(KEY_DARK_MODE, false)

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANG, lang).apply()
        _language.value = lang
    }
    fun getLanguage(): String = prefs.getString(KEY_LANG, "in") ?: "in"

    fun logout() {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply()
    }
    
    fun deleteAccount() {
        val username = getSavedUsername()
        val editor = prefs.edit()
        if (username.isNotEmpty()) {
            editor.remove("${username}_$KEY_WA")
            editor.remove("${username}_$KEY_BANK_NAME")
            editor.remove("${username}_$KEY_BANK_ACC")
            editor.remove("${username}_$KEY_EWALLET_NAME")
            editor.remove("${username}_$KEY_EWALLET_ACC")
            editor.remove("${username}_$KEY_IS_VERIFIED")
            editor.remove("${username}_$KEY_USER_FULL_NAME")
            editor.remove("${username}_$KEY_USER_LOCATION")
            editor.remove("${username}_$KEY_USER_PHOTO")
            editor.remove("${username}_$KEY_NOTIFICATIONS")
        }
        editor.remove(KEY_USER_USERNAME)
        editor.putBoolean(KEY_IS_LOGGED_IN, false)
        editor.apply()
        _notifications.value = emptyList()
    }

    fun addNotification(title: String, body: String) {
        val history = getNotifications().toMutableList()
        history.add(0, NotificationData(title, body, System.currentTimeMillis()))
        val json = gson.toJson(history)
        prefs.edit().putString(uKey(KEY_NOTIFICATIONS), json).apply()
        _notifications.value = history
    }

    fun getNotifications(): List<NotificationData> {
        val json = prefs.getString(uKey(KEY_NOTIFICATIONS), null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<NotificationData>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

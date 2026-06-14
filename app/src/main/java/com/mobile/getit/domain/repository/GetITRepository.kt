package com.mobile.getit.domain.repository

import android.net.Uri
import com.mobile.getit.data.local.NotificationData
import com.mobile.getit.domain.model.BannerPromo
import com.mobile.getit.domain.model.Product
import com.mobile.getit.domain.model.User
import com.mobile.getit.domain.model.WeatherInfo
import kotlinx.coroutines.flow.Flow

interface GetItRepository {
    // Auth & Profile
    suspend fun login(identifier: String, password: String): Result<Unit>
    suspend fun register(user: User): Result<Unit>
    suspend fun logout()
    fun isUserLoggedIn(): Boolean
    fun isEmailVerified(): Boolean
    fun getCurrentUserUid(): String?
    suspend fun getUserProfile(uid: String): User?
    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>): Result<Unit>

    // Reactive Data
    fun observeProducts(): Flow<List<Product>>
    fun observeCurrentUser(): Flow<User?>
    fun observeActiveBanner(): Flow<BannerPromo?>
    fun isDarkMode(): Flow<Boolean>
    fun getLanguage(): Flow<String>

    // Local Settings Access
    fun getSavedFullName(): String
    fun getSavedUsername(): String
    fun getSavedLocation(): String
    fun getSavedPhotoUri(): String
    fun getSavedUsernameFromPrefs(): String { return getSavedUsername() }
    fun getSavedWhatsapp(): String
    fun getSavedBankName(): String
    fun getSavedBankAccount(): String
    fun getSavedEWalletName(): String
    fun getSavedEWalletAccount(): String
    fun isUserVerified(): Boolean
    fun getSavedLoginType(): String
    
    suspend fun setDarkMode(enabled: Boolean)
    suspend fun setLanguage(lang: String)
    suspend fun saveLocalProfile(fullName: String, location: String, photoUri: String)
    suspend fun saveLocalPaymentMethods(whatsapp: String, bankName: String, bankAcc: String, eWalletName: String, eWalletAcc: String)
    suspend fun setLoggedIn(loggedIn: Boolean)
    suspend fun saveAccount(fullName: String, username: String, pass: String, type: String = "EMAIL")
    suspend fun deleteLocalAccount()
    suspend fun saveVerification(isVerified: Boolean)

    // Account Verification
    suspend fun waitForEmailVerification(): Result<Unit>
    suspend fun updateAndVerifyUlmEmail(newEmail: String): Result<Unit>
    suspend fun resendVerificationEmail(): Result<Unit>
    suspend fun deleteCurrentUser(): Result<Unit>

    // Marketplace Operations
    suspend fun fetchAllProducts()
    suspend fun getDummyJsonProducts(): List<Product>
    suspend fun getFirebaseProducts(): List<Product>
    suspend fun uploadProductToFirebase(product: Product): Boolean
    suspend fun updateFirebaseProduct(id: String, product: Product): Boolean
    suspend fun deleteFirebaseProduct(id: String): Boolean
    suspend fun uploadImage(uri: Uri): String?
    suspend fun syncSellerInfoInProducts(
        newName: String, newLocation: String, newPhoto: String,
        newWa: String, newBankName: String, newBankAcc: String, 
        newEWalletName: String, newEWalletAcc: String
    )
    
    // Wishlist & Weather
    suspend fun getWeatherBanjarmasin(): WeatherInfo
    fun getLocalWishlist(uid: String): Flow<List<Product>>
    suspend fun addToWishlist(product: Product)
    suspend fun removeFromWishlist(id: String)
    suspend fun updateWishlistNotes(id: String, notes: String)

    // Notifications
    fun getNotifications(): List<NotificationData>
    fun observeNotifications(): Flow<List<NotificationData>>
    fun addNotification(title: String, body: String)
}

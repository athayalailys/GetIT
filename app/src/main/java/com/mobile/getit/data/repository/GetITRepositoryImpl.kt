package com.mobile.getit.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.mobile.getit.R
import com.mobile.getit.data.local.NotificationData
import com.mobile.getit.data.local.PreferenceManager
import com.mobile.getit.data.local.WishlistDao
import com.mobile.getit.data.local.WishlistEntity
import com.mobile.getit.data.remote.FirebaseApiService
import com.mobile.getit.data.remote.ProductApiService
import com.mobile.getit.data.remote.WeatherApiService
import com.mobile.getit.domain.model.BannerPromo
import com.mobile.getit.domain.model.Product
import com.mobile.getit.domain.model.User
import com.mobile.getit.domain.model.WeatherInfo
import com.mobile.getit.domain.repository.GetItRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class GetItRepositoryImpl(
    private val wishlistDao: WishlistDao,
    private val weatherApi: WeatherApiService,
    private val productApi: ProductApiService,
    private val firebaseApi: FirebaseApiService,
    private val prefs: PreferenceManager
) : GetItRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase
        .getInstance("https://getit-182e0-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .reference
    private val storage = FirebaseStorage.getInstance()

    override suspend fun login(identifier: String, password: String): Result<Unit> {
        return try {
            val email = if (identifier.contains("@")) {
                identifier.trim()
            } else {
                val snapshot = database.child("usernames").child(identifier.trim()).get().await()
                snapshot.getValue(String::class.java) ?: throw Exception("Username tidak ditemukan")
            }
            auth.signInWithEmailAndPassword(email, password).await()
            auth.currentUser?.reload()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(user: User): Result<Unit> {
        return try {
            val email = user.email.trim()
            val usernameCheck =
                database.child("usernames").child(user.username.trim()).get().await()
            if (usernameCheck.exists()) throw Exception("Username sudah digunakan")

            val isUlm = email.endsWith("@mhs.ulm.ac.id") || email.endsWith("@ulm.ac.id")
            val finalUser = user.copy(email = email, isVerified = isUlm)

            val authResult =
                auth.createUserWithEmailAndPassword(email, user.password).await()
            val firebaseUser = authResult.user ?: throw Exception("Gagal membuat user")

            try {
                firebaseUser.sendEmailVerification().await()
                val uid = firebaseUser.uid
                database.child("users").child(uid).setValue(finalUser.copy(password = "")).await()
                database.child("usernames").child(user.username.trim()).setValue(email).await()
                Result.success(Unit)
            } catch (dbException: Exception) {
                firebaseUser.delete().await()
                throw dbException
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        auth.signOut()
        prefs.logout()
    }

    override fun isUserLoggedIn(): Boolean {
        val user = auth.currentUser
        return user != null && user.isEmailVerified
    }

    override fun isEmailVerified(): Boolean = auth.currentUser?.isEmailVerified ?: false

    override fun getCurrentUserUid(): String? = auth.currentUser?.uid

    override suspend fun getUserProfile(uid: String): User? {
        return try {
            val snapshot = database.child("users").child(uid).get().await()
            snapshot.getValue(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateUserProfile(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            database.child("users").child(uid).updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeProducts(): Flow<List<Product>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Product>()
                snapshot.children.forEach { child ->
                    child.getValue(Product::class.java)?.let { 
                        list.add(it.copy(id = child.key ?: "")) 
                    }
                }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                trySend(emptyList())
            }
        }
        database.child("products").addValueEventListener(listener)
        awaitClose { database.child("products").removeEventListener(listener) }
    }

    private fun observeAuthState(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun observeCurrentUser(): Flow<User?> = observeAuthState().flatMapLatest { uid ->
        if (uid == null) flowOf(null)
        else callbackFlow {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    trySend(snapshot.getValue(User::class.java))
                }
                override fun onCancelled(error: DatabaseError) {
                    trySend(null)
                }
            }
            database.child("users").child(uid).addValueEventListener(listener)
            awaitClose { database.child("users").child(uid).removeEventListener(listener) }
        }
    }

    override fun observeActiveBanner(): Flow<BannerPromo?> = callbackFlow {
        val defaultBanner = BannerPromo(id="1", title="Special Sale", tagline="GET BEST DEAL!", backgroundColorHex="#982598", subtitle = "Promo Unggulan")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val banner = snapshot.children.firstOrNull()?.getValue(BannerPromo::class.java)
                trySend(banner ?: defaultBanner)
            }
            override fun onCancelled(error: DatabaseError) {
                trySend(defaultBanner)
            }
        }
        database.child("banners").addValueEventListener(listener)
        awaitClose { database.child("banners").removeEventListener(listener) }
    }

    override fun isDarkMode(): Flow<Boolean> = prefs.isDarkModeFlow
    override fun getLanguage(): Flow<String> = prefs.languageFlow

    override fun getNotifications(): List<NotificationData> = prefs.getNotifications()
    override fun observeNotifications(): Flow<List<NotificationData>> = prefs.notificationsFlow
    override fun addNotification(title: String, body: String) = prefs.addNotification(title, body)
    
    override fun getSavedFullName(): String = prefs.getFullName()
    override fun getSavedUsername(): String = prefs.getSavedUsername()
    override fun getSavedLocation(): String = prefs.getLocation()
    override fun getSavedPhotoUri(): String = prefs.getPhotoUri()
    override fun getSavedWhatsapp(): String = prefs.getWhatsapp()
    override fun getSavedBankName(): String = prefs.getBankName()
    override fun getSavedBankAccount(): String = prefs.getBankAccount()
    override fun getSavedEWalletName(): String = prefs.getEWalletName()
    override fun getSavedEWalletAccount(): String = prefs.getEWalletAccount()
    override fun isUserVerified(): Boolean = prefs.isVerified()
    override fun getSavedLoginType(): String = prefs.getLoginType()
    
    override suspend fun setDarkMode(enabled: Boolean) = prefs.setDarkMode(enabled)
    override suspend fun setLanguage(lang: String) = prefs.setLanguage(lang)
    override suspend fun saveLocalProfile(fullName: String, location: String, photoUri: String) = prefs.updateProfile(fullName, location, photoUri)
    override suspend fun saveLocalPaymentMethods(whatsapp: String, bankName: String, bankAcc: String, eWalletName: String, eWalletAcc: String) = prefs.savePaymentMethods(whatsapp, bankName, bankAcc, eWalletName, eWalletAcc)
    override suspend fun setLoggedIn(loggedIn: Boolean) = prefs.setLoggedIn(loggedIn)
    override suspend fun saveAccount(fullName: String, username: String, pass: String, type: String) = prefs.saveAccount(fullName, username, pass, type)
    override suspend fun deleteLocalAccount() = prefs.deleteAccount()
    override suspend fun saveVerification(isVerified: Boolean) = prefs.saveVerification(isVerified)

    override suspend fun waitForEmailVerification(): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Sesi berakhir"))
            var verified = false
            var elapsed = 0
            while (!verified && elapsed < 120) {
                user.reload().await()
                if (user.isEmailVerified) {
                    verified = true
                } else {
                    delay(3000)
                    elapsed += 3
                }
            }
            if (verified) Result.success(Unit) else Result.failure(Exception("Waktu verifikasi habis"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCurrentUser(): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Tidak ada sesi user"))
            val uid = user.uid
            val snapshot = database.child("users").child(uid).get().await()
            val userData = snapshot.getValue(User::class.java)
            userData?.username?.let {
                database.child("usernames").child(it).removeValue().await()
            }
            database.child("users").child(uid).removeValue().await()
            user.delete().await()
            prefs.deleteAccount()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAndVerifyUlmEmail(newEmail: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("Sesi berakhir")
            user.verifyBeforeUpdateEmail(newEmail.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resendVerificationEmail(): Result<Unit> {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchAllProducts() {}

    override suspend fun getDummyJsonProducts(): List<Product> {
        return try {
            val smartphones = try { productApi.getSmartphones().products } catch (e: Exception) { emptyList() }
            val laptops = try { productApi.getLaptops().products } catch (e: Exception) { emptyList() }
            (smartphones + laptops).map { dto ->
                Product(
                    id = dto.id.toString(),
                    title = dto.title,
                    price = dto.price,
                    description = dto.description,
                    category = dto.category,
                    images = listOf(dto.thumbnail),
                    source = "DUMMYJSON"
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getFirebaseProducts(): List<Product> {
        return try {
            val snapshot = database.child("products").get().await()
            val list = mutableListOf<Product>()
            snapshot.children.forEach { child ->
                child.getValue(Product::class.java)?.let { 
                    list.add(it.copy(id = child.key ?: "")) 
                }
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun uploadProductToFirebase(product: Product): Boolean {
        return try {
            val finalId = if (product.id.isEmpty()) database.child("products").push().key ?: UUID.randomUUID().toString() else product.id
            database.child("products").child(finalId).setValue(product.copy(id = finalId)).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateFirebaseProduct(id: String, product: Product): Boolean {
        return try {
            database.child("products").child(id).setValue(product).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteFirebaseProduct(id: String): Boolean {
        return try {
            database.child("products").child(id).removeValue().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun uploadImage(uri: Uri): String? {
        return try {
            val fileName = UUID.randomUUID().toString()
            val ref = storage.reference.child("product_images/$fileName")
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun syncSellerInfoInProducts(
        newName: String, newLocation: String, newPhoto: String,
        newWa: String, newBankName: String, newBankAcc: String,
        newEWalletName: String, newEWalletAcc: String
    ) {
        val sellerId = prefs.getSavedUsername()
        if (sellerId.isEmpty()) return
        
        try {
            val snapshot = database.child("products").get().await()
            snapshot.children.forEach { child ->
                val p = child.getValue(Product::class.java)
                if (p?.sellerId == sellerId) {
                    val updates = mapOf(
                        "sellerName" to newName,
                        "sellerLocation" to newLocation,
                        "sellerPhotoUri" to newPhoto,
                        "sellerWhatsapp" to newWa,
                        "sellerBankName" to newBankName,
                        "sellerBankAcc" to newBankAcc,
                        "sellerEWalletName" to newEWalletName,
                        "sellerEWalletAcc" to newEWalletAcc
                    )
                    child.ref.updateChildren(updates).await()
                }
            }
        } catch (e: Exception) {}
    }

    override suspend fun getWeatherBanjarmasin(): WeatherInfo {
        return try {
            val response = weatherApi.getCurrentWeather()
            val temp = response.currentWeather.temperature
            val code = response.currentWeather.weatherCode
            
            val conditionResId = when (code) {
                0 -> R.string.weather_clear
                1, 2, 3 -> R.string.weather_partly_cloudy
                45, 48 -> R.string.weather_foggy
                51, 53, 55 -> R.string.weather_drizzle
                61, 63, 65 -> R.string.weather_rain
                80, 81, 82 -> R.string.weather_heavy_rain
                95, 96, 99 -> R.string.weather_thunderstorm
                else -> R.string.weather_unknown
            }

            val isSafe = code in 0..55 || code in 71..75
            WeatherInfo(temp, conditionResId, isSafe)
        } catch (e: Exception) {
            WeatherInfo(30.0, R.string.weather_load_err, false)
        }
    }

    override fun getLocalWishlist(uid: String): Flow<List<Product>> {
        return wishlistDao.getWishlistByUser(uid).map { entities ->
            entities.map { entity ->
                Product(
                    id = entity.id,
                    title = entity.title,
                    price = entity.price.toInt(),
                    description = entity.description ?: "",
                    category = "",
                    images = listOf(entity.thumbnail ?: ""),
                    notes = entity.notes ?: "",
                    source = "LOCAL"
                )
            }
        }
    }

    override suspend fun addToWishlist(product: Product) {
        val uid = auth.currentUser?.uid ?: ""
        val entity = WishlistEntity(
            id = product.id,
            userId = uid,
            title = product.title,
            price = product.price.toDouble(),
            description = product.description,
            thumbnail = product.images.firstOrNull() ?: "",
            notes = product.notes
        )
        wishlistDao.addToWishlist(entity)
    }

    override suspend fun updateWishlistNotes(id: String, notes: String) {
        val uid = auth.currentUser?.uid ?: ""
        val entity = wishlistDao.getWishlistById(id, uid)
        if (entity != null) {
            wishlistDao.updateNotes(id, notes, uid)
        }
    }

    override suspend fun removeFromWishlist(id: String) {
        val uid = auth.currentUser?.uid ?: ""
        val item = wishlistDao.getWishlistById(id, uid)
        if (item != null) {
            wishlistDao.removeFromWishlist(item)
        }
    }
}

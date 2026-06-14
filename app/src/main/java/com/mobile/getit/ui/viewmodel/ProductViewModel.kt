package com.mobile.getit.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.getit.R
import com.mobile.getit.domain.model.BannerPromo
import com.mobile.getit.domain.model.Category
import com.mobile.getit.domain.model.Product
import com.mobile.getit.domain.model.User
import com.mobile.getit.domain.repository.GetItRepository
import com.mobile.getit.utils.NotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ProductViewModel(
    private val repository: GetItRepository, 
    application: Application
) : AndroidViewModel(application) {
    
    private val notificationHelper = NotificationHelper(application)
    
    val products: StateFlow<List<Product>> = repository.observeProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeBanner: StateFlow<BannerPromo?> = repository.observeActiveBanner()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentUser: StateFlow<User?> = repository.observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _uploadStatus = MutableStateFlow<Boolean?>(null)
    val uploadStatus: StateFlow<Boolean?> = _uploadStatus.asStateFlow()

    private var lastProductsList = emptyList<Product>()

    val bestOffers: StateFlow<List<Product>> = products
        .map { list ->
            list.filter { !it.isSold && !it.isHandedOver && !it.isReceived }
                .sortedByDescending { it.timestamp }
                .take(5)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadCategories()
        setupNotificationObserver()
    }

    private fun loadCategories() {
        _categories.value = listOf(
            Category("1", "Laptop dan Komputer", R.string.cat_laptop, "💻"),
            Category("2", "Komponen untuk PC", R.string.cat_pc, "🖥️"),
            Category("3", "Aksesoris Komputer", R.string.cat_acc, "🎧"),
            Category("4", "Gadget dan Mobile", R.string.cat_gadget, "📱")
        )
    }

    private fun setupNotificationObserver() {
        viewModelScope.launch {
            products.collect { newProducts ->
                val currentUsername = repository.getSavedUsername()
                
                if (currentUsername.isNotEmpty() && lastProductsList.isNotEmpty()) {
                    newProducts.forEach { newProd ->
                        val oldProd = lastProductsList.find { it.id == newProd.id } ?: return@forEach
                        
                        // FIX LOGIC: Hanya notifikasi jika status benar-benar berubah dari false ke true (Masuk Keranjang)
                        // Dan pastikan sellerId sesuai dengan user saat ini
                        if (newProd.sellerId == currentUsername && !oldProd.isInCart && newProd.isInCart) {
                            triggerNotification(
                                "Produk Diminati!", 
                                "Seseorang baru saja memasukkan '${newProd.title}' ke keranjang mereka."
                            )
                        }

                        if (newProd.buyerId == currentUsername && !oldProd.isHandedOver && newProd.isHandedOver) {
                            triggerNotification(
                                "Pesanan Siap!", 
                                "Produk '${newProd.title}' siap untuk diserahterimakan."
                            )
                        }

                        if (newProd.sellerId == currentUsername && !oldProd.isReceived && newProd.isReceived) {
                            triggerNotification(
                                "Transaksi Selesai!", 
                                "Pembeli telah menerima produk '${newProd.title}'."
                            )
                        }
                    }
                }
                lastProductsList = newProducts
            }
        }
    }

    private fun triggerNotification(title: String, message: String) {
        notificationHelper.showNotification(title, message)
        repository.addNotification(title, message)
    }

    fun loadMarketplaceProducts() {
        viewModelScope.launch {
            repository.fetchAllProducts()
        }
    }

    fun addProduct(nama: String, harga: String, deskripsi: String, category: String, imageUrls: List<String>) {
        val user = currentUser.value ?: return
        val product = Product(
            id = UUID.randomUUID().toString(),
            title = nama,
            price = harga.toIntOrNull() ?: 0,
            description = deskripsi,
            category = category,
            imageUrl = imageUrls.firstOrNull() ?: "",
            images = imageUrls,
            timestamp = System.currentTimeMillis(),
            sellerId = user.username,
            sellerName = user.fullName,
            sellerWhatsapp = user.whatsapp,
            sellerLocation = user.location,
            sellerPhotoUri = user.photoUri,
            sellerBankName = user.bankName,
            sellerBankAcc = user.bankAcc,
            sellerEWalletName = user.eWalletName,
            sellerEWalletAcc = user.eWalletAcc
        )
        viewModelScope.launch {
            _uploadStatus.value = null
            _uploadStatus.value = repository.uploadProductToFirebase(product)
        }
    }

    fun updateProduct(productId: String, product: Product) {
        viewModelScope.launch { repository.updateFirebaseProduct(productId, product) }
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch { repository.deleteFirebaseProduct(id) }
    }
    
    fun updateTransaction(
        id: String, 
        isPaid: Boolean? = null, 
        isSold: Boolean? = null, 
        isHandedOver: Boolean? = null, 
        isReceived: Boolean? = null, 
        paymentMethod: String? = null,
        buyerId: String? = null
    ) {
        products.value.find { it.id == id }?.let {
            val updated = it.copy(
                isPaid = isPaid ?: it.isPaid,
                isSold = isSold ?: it.isSold,
                isHandedOver = isHandedOver ?: it.isHandedOver, 
                isReceived = isReceived ?: it.isReceived,
                paymentMethod = paymentMethod ?: it.paymentMethod,
                buyerId = buyerId ?: it.buyerId
            )
            viewModelScope.launch { repository.updateFirebaseProduct(id, updated) }
        }
    }

    fun markAsReceived(id: String) {
        updateTransaction(id = id, isReceived = true)
    }

    // Ganti toggleCart dengan updateCartState yang lebih eksplisit untuk mencegah status terbalik
    fun updateCartState(id: String, isInCart: Boolean) {
        products.value.find { it.id == id }?.let {
            if (it.isInCart == isInCart) return@let
            val updated = it.copy(isInCart = isInCart)
            viewModelScope.launch { repository.updateFirebaseProduct(id, updated) }
        }
    }

    fun resetUploadStatus() { _uploadStatus.value = null }
}

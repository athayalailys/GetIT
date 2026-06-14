package com.mobile.getit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.getit.domain.model.Product
import com.mobile.getit.domain.repository.GetItRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class WishlistViewModel(private val repository: GetItRepository) : ViewModel() {
    
    // Mengamati user yang sedang aktif dan mengganti sumber wishlist secara otomatis
    val wishlistItems: StateFlow<List<Product>> = repository.observeCurrentUser()
        .filterNotNull()
        .flatMapLatest { _ ->
            val uid = repository.getCurrentUserUid() ?: ""
            repository.getLocalWishlist(uid)
                .combine(repository.observeProducts()) { localItems, marketplaceProducts ->
                    localItems.filter { local -> 
                        marketplaceProducts.any { it.id == local.id }
                    }.map { local ->
                        // Gunakan data segar dari marketplace jika tersedia
                        marketplaceProducts.find { it.id == local.id } ?: local
                    }
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addToWishlist(product: Product) {
        viewModelScope.launch {
            repository.addToWishlist(product)
        }
    }

    fun deleteFromWishlist(id: String) {
        viewModelScope.launch {
            repository.removeFromWishlist(id)
        }
    }
    
    fun updateNotes(id: String, notes: String) {
        viewModelScope.launch {
            repository.updateWishlistNotes(id, notes)
        }
    }
}

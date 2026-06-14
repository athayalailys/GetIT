package com.mobile.getit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.getit.domain.model.Product
import com.mobile.getit.domain.model.User
import com.mobile.getit.domain.repository.GetItRepository
import kotlinx.coroutines.flow.*

data class ProfileUiState(
    val user: User? = null,
    val products: List<Product> = emptyList(),
    val totalSales: Int = 0,
    val isMyProfile: Boolean = false,
    val isLoading: Boolean = false
)

class ProfileViewModel(private val repository: GetItRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfileData(sellerId: String) {
        val isMe = sellerId == "current_user" || sellerId == repository.getSavedUsername()
        _uiState.update { it.copy(isMyProfile = isMe, isLoading = true) }

        repository.observeProducts()
            .combine(repository.observeCurrentUser()) { products, currentUser ->
                val targetId = if (isMe) repository.getSavedUsername() else sellerId
                val sellerProducts = products.filter { it.sellerId == targetId }
                val totalSales = sellerProducts.count { it.isSold }
                
                val user = if (isMe) currentUser else {
                    val firstProd = sellerProducts.firstOrNull()
                    if (firstProd != null) {
                        User(
                            fullName = firstProd.sellerName,
                            location = firstProd.sellerLocation,
                            photoUri = firstProd.sellerPhotoUri,
                            username = firstProd.sellerId
                        )
                    } else {
                        _uiState.value.user
                    }
                }
                
                _uiState.update { it.copy(
                    user = user,
                    products = sellerProducts,
                    totalSales = totalSales,
                    isLoading = false
                )}
            }.launchIn(viewModelScope)
    }
}

package com.mobile.getit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.getit.domain.model.Product
import com.mobile.getit.domain.repository.GetItRepository
import kotlinx.coroutines.flow.*

data class CategoryUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val title: String = ""
)

class CategoryViewModel(
    private val repository: GetItRepository,
    private val categoryName: String,
    private val searchQuery: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        observeProducts()
    }

    private fun observeProducts() {
        _uiState.update { it.copy(isLoading = true) }
        repository.observeProducts()
            .onEach { allProducts ->
                val filtered = allProducts.filter { product ->
                    val matchCat = categoryName == "Semua" || categoryName == "empty" || product.category.equals(categoryName, ignoreCase = true)
                    val matchSrc = searchQuery == "empty" || product.title.contains(searchQuery, ignoreCase = true)
                    matchCat && matchSrc
                }
                _uiState.update { it.copy(products = filtered, isLoading = false) }
            }.launchIn(viewModelScope)
    }
}

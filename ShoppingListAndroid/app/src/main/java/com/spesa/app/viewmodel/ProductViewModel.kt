package com.spesa.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spesa.app.data.models.*
import com.spesa.app.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductUiState(
    val products: List<ProductDto> = emptyList(),
    val categories: List<CategoryDto> = emptyList(),
    val supermarkets: List<SupermarketDto> = emptyList(),
    val selectedProduct: ProductDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null
)

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProductUiState())
    val state: StateFlow<ProductUiState> = _state.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val products = repository.getProducts()
            val categories = repository.getCategories()
            val supermarkets = repository.getSupermarkets()
            _state.update { s ->
                s.copy(
                    isLoading = false,
                    products = products.getOrDefault(emptyList()),
                    categories = categories.getOrDefault(emptyList()),
                    supermarkets = supermarkets.getOrDefault(emptyList()),
                    error = products.exceptionOrNull()?.message
                )
            }
        }
    }

    fun loadProduct(id: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getProduct(id)
                .onSuccess { product -> _state.update { it.copy(isLoading = false, selectedProduct = product) } }
                .onFailure { _state.update { it.copy(isLoading = false, error = it.error) } }
        }
    }

    fun createProduct(dto: CreateProductDto, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.createProduct(dto)
                .onSuccess {
                    loadAll()
                    _state.update { s -> s.copy(isLoading = false, success = "Prodotto creato") }
                    onSuccess()
                }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun updateProduct(id: Int, dto: CreateProductDto, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.updateProduct(id, dto)
                .onSuccess {
                    loadAll()
                    _state.update { s -> s.copy(isLoading = false, success = "Prodotto aggiornato") }
                    onSuccess()
                }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            repository.deleteProduct(id)
                .onSuccess { loadAll() }
                .onFailure { e -> _state.update { it.copy(error = e.message) } }
        }
    }

    fun createCategory(name: String) {
        viewModelScope.launch {
            repository.createCategory(name).onSuccess { loadAll() }
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            repository.deleteCategory(id).onSuccess { loadAll() }
        }
    }

    fun createSupermarket(name: String, address: String?) {
        viewModelScope.launch {
            repository.createSupermarket(name, address).onSuccess { loadAll() }
        }
    }

    fun deleteSupermarket(id: Int) {
        viewModelScope.launch {
            repository.deleteSupermarket(id).onSuccess { loadAll() }
        }
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getProducts(search = query.ifBlank { null })
                .onSuccess { products -> _state.update { it.copy(isLoading = false, products = products) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun clearMessages() = _state.update { it.copy(error = null, success = null) }
}

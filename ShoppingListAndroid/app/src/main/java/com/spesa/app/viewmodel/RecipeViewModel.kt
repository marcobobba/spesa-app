package com.spesa.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spesa.app.data.models.*
import com.spesa.app.data.repository.ProductRepository
import com.spesa.app.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecipeUiState(
    val recipes: List<RecipeDto> = emptyList(),
    val products: List<ProductDto> = emptyList(),
    val selectedRecipe: RecipeDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null
)

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val repository: RecipeRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeUiState())
    val state: StateFlow<RecipeUiState> = _state.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val recipes = repository.getRecipes()
            val products = productRepository.getProducts()
            _state.update { s ->
                s.copy(
                    isLoading = false,
                    recipes = recipes.getOrDefault(emptyList()),
                    products = products.getOrDefault(emptyList()),
                    error = recipes.exceptionOrNull()?.message
                )
            }
        }
    }

    fun loadRecipe(id: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getRecipe(id)
                .onSuccess { recipe -> _state.update { it.copy(isLoading = false, selectedRecipe = recipe) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun createRecipe(dto: CreateRecipeDto, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.createRecipe(dto)
                .onSuccess {
                    loadAll()
                    _state.update { s -> s.copy(isLoading = false, success = "Ricetta creata") }
                    onSuccess()
                }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun updateRecipe(id: Int, dto: CreateRecipeDto, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.updateRecipe(id, dto)
                .onSuccess {
                    loadAll()
                    _state.update { s -> s.copy(isLoading = false, success = "Ricetta aggiornata") }
                    onSuccess()
                }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun deleteRecipe(id: Int) {
        viewModelScope.launch {
            repository.deleteRecipe(id)
                .onSuccess { loadAll() }
                .onFailure { e -> _state.update { it.copy(error = e.message) } }
        }
    }

    fun clearMessages() = _state.update { it.copy(error = null, success = null) }
}

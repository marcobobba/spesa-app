package com.spesa.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spesa.app.data.models.*
import com.spesa.app.data.repository.RecipeRepository
import com.spesa.app.data.repository.WeeklyPlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeeklyPlanUiState(
    val plans: List<WeeklyPlanSummaryDto> = emptyList(),
    val recipes: List<RecipeDto> = emptyList(),
    val selectedPlan: WeeklyPlanDto? = null,
    val shoppingList: ShoppingListDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null
)

@HiltViewModel
class WeeklyPlanViewModel @Inject constructor(
    private val repository: WeeklyPlanRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WeeklyPlanUiState())
    val state: StateFlow<WeeklyPlanUiState> = _state.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val plans = repository.getWeeklyPlans()
            val recipes = recipeRepository.getRecipes()
            _state.update { s ->
                s.copy(
                    isLoading = false,
                    plans = plans.getOrDefault(emptyList()),
                    recipes = recipes.getOrDefault(emptyList()),
                    error = plans.exceptionOrNull()?.message
                )
            }
        }
    }

    fun loadPlan(id: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getWeeklyPlan(id)
                .onSuccess { plan -> _state.update { it.copy(isLoading = false, selectedPlan = plan) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun createPlan(dto: CreateWeeklyPlanDto, onSuccess: (Int) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.createWeeklyPlan(dto)
                .onSuccess { plan ->
                    loadAll()
                    _state.update { s -> s.copy(isLoading = false, success = "Piano creato") }
                    onSuccess(plan.id)
                }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun updatePlan(id: Int, dto: CreateWeeklyPlanDto, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.updateWeeklyPlan(id, dto)
                .onSuccess {
                    loadAll()
                    _state.update { s -> s.copy(isLoading = false, success = "Piano aggiornato") }
                    onSuccess()
                }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun deletePlan(id: Int) {
        viewModelScope.launch {
            repository.deleteWeeklyPlan(id)
                .onSuccess { loadAll() }
                .onFailure { e -> _state.update { it.copy(error = e.message) } }
        }
    }

    fun duplicatePlan(id: Int, newName: String) {
        viewModelScope.launch {
            repository.duplicateWeeklyPlan(id, newName)
                .onSuccess {
                    loadAll()
                    _state.update { s -> s.copy(success = "Piano duplicato come '$newName'") }
                }
                .onFailure { e -> _state.update { it.copy(error = e.message) } }
        }
    }

    fun loadShoppingList(planId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getShoppingList(planId)
                .onSuccess { list -> _state.update { it.copy(isLoading = false, shoppingList = list) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun clearMessages() = _state.update { it.copy(error = null, success = null) }
}

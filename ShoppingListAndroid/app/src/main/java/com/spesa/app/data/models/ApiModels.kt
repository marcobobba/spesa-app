package com.spesa.app.data.models

data class AuthRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val email: String, val password: String)
data class AuthResponse(val token: String, val email: String, val name: String)

data class CategoryDto(val id: Int, val name: String)
data class CreateCategoryDto(val name: String)

data class SupermarketDto(val id: Int, val name: String, val address: String?)
data class CreateSupermarketDto(val name: String, val address: String?)

data class ProductDto(
    val id: Int,
    val code: String,
    val description: String,
    val packageWeight: Double?,
    val weightUnit: String?,
    val categoryId: Int?,
    val categoryName: String?,
    val supermarketIds: List<Int>,
    val supermarketNames: List<String>
)

data class CreateProductDto(
    val code: String,
    val description: String,
    val packageWeight: Double?,
    val weightUnit: String?,
    val categoryId: Int?,
    val supermarketIds: List<Int>
)

data class RecipeIngredientDto(
    val id: Int,
    val productId: Int,
    val productCode: String,
    val productDescription: String,
    val quantity: Double,
    val unit: String
)

data class RecipeDto(
    val id: Int,
    val name: String,
    val description: String?,
    val servings: Int,
    val ingredients: List<RecipeIngredientDto>
)

data class CreateRecipeIngredientDto(
    val productId: Int,
    val quantity: Double,
    val unit: String
)

data class CreateRecipeDto(
    val name: String,
    val description: String?,
    val servings: Int,
    val ingredients: List<CreateRecipeIngredientDto>
)

data class WeeklyPlanDayRecipeDto(
    val id: Int,
    val recipeId: Int,
    val recipeName: String,
    val servings: Int
)

data class WeeklyPlanDayDto(
    val id: Int,
    val dayOfWeek: Int,
    val dayName: String,
    val mealType: String,
    val recipes: List<WeeklyPlanDayRecipeDto>
)

data class WeeklyPlanDto(
    val id: Int,
    val name: String,
    val description: String?,
    val createdAt: String,
    val days: List<WeeklyPlanDayDto>
)

data class WeeklyPlanSummaryDto(
    val id: Int,
    val name: String,
    val description: String?,
    val createdAt: String
)

data class CreateWeeklyPlanDayRecipeDto(val recipeId: Int, val servings: Int)
data class CreateWeeklyPlanDayDto(val dayOfWeek: Int, val mealType: String, val recipes: List<CreateWeeklyPlanDayRecipeDto>)
data class CreateWeeklyPlanDto(val name: String, val description: String?, val days: List<CreateWeeklyPlanDayDto>)

data class ShoppingListItemDto(
    val productId: Int,
    val productCode: String,
    val productDescription: String,
    val totalQuantity: Double,
    val unit: String,
    val packageWeight: Double?,
    val weightUnit: String?,
    val categoryName: String?,
    val supermarkets: List<String>
)

data class ShoppingListDto(
    val weeklyPlanId: Int,
    val weeklyPlanName: String,
    val items: List<ShoppingListItemDto>
)

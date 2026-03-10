package com.spesa.app.data.api

import com.spesa.app.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    // Categories
    @GET("categories")
    suspend fun getCategories(): Response<List<CategoryDto>>

    @POST("categories")
    suspend fun createCategory(@Body dto: CreateCategoryDto): Response<CategoryDto>

    @PUT("categories/{id}")
    suspend fun updateCategory(@Path("id") id: Int, @Body dto: CreateCategoryDto): Response<CategoryDto>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Int): Response<Unit>

    // Supermarkets
    @GET("supermarkets")
    suspend fun getSupermarkets(): Response<List<SupermarketDto>>

    @POST("supermarkets")
    suspend fun createSupermarket(@Body dto: CreateSupermarketDto): Response<SupermarketDto>

    @PUT("supermarkets/{id}")
    suspend fun updateSupermarket(@Path("id") id: Int, @Body dto: CreateSupermarketDto): Response<SupermarketDto>

    @DELETE("supermarkets/{id}")
    suspend fun deleteSupermarket(@Path("id") id: Int): Response<Unit>

    // Products
    @GET("products")
    suspend fun getProducts(
        @Query("search") search: String? = null,
        @Query("categoryId") categoryId: Int? = null
    ): Response<List<ProductDto>>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Int): Response<ProductDto>

    @POST("products")
    suspend fun createProduct(@Body dto: CreateProductDto): Response<ProductDto>

    @PUT("products/{id}")
    suspend fun updateProduct(@Path("id") id: Int, @Body dto: CreateProductDto): Response<ProductDto>

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<Unit>

    // Recipes
    @GET("recipes")
    suspend fun getRecipes(@Query("search") search: String? = null): Response<List<RecipeDto>>

    @GET("recipes/{id}")
    suspend fun getRecipe(@Path("id") id: Int): Response<RecipeDto>

    @POST("recipes")
    suspend fun createRecipe(@Body dto: CreateRecipeDto): Response<RecipeDto>

    @PUT("recipes/{id}")
    suspend fun updateRecipe(@Path("id") id: Int, @Body dto: CreateRecipeDto): Response<RecipeDto>

    @DELETE("recipes/{id}")
    suspend fun deleteRecipe(@Path("id") id: Int): Response<Unit>

    // Weekly Plans
    @GET("weeklyplans")
    suspend fun getWeeklyPlans(): Response<List<WeeklyPlanSummaryDto>>

    @GET("weeklyplans/{id}")
    suspend fun getWeeklyPlan(@Path("id") id: Int): Response<WeeklyPlanDto>

    @POST("weeklyplans")
    suspend fun createWeeklyPlan(@Body dto: CreateWeeklyPlanDto): Response<WeeklyPlanDto>

    @PUT("weeklyplans/{id}")
    suspend fun updateWeeklyPlan(@Path("id") id: Int, @Body dto: CreateWeeklyPlanDto): Response<WeeklyPlanDto>

    @DELETE("weeklyplans/{id}")
    suspend fun deleteWeeklyPlan(@Path("id") id: Int): Response<Unit>

    @POST("weeklyplans/{id}/duplicate")
    suspend fun duplicateWeeklyPlan(@Path("id") id: Int, @Body newName: String): Response<WeeklyPlanDto>

    @GET("weeklyplans/{id}/shoppinglist")
    suspend fun getShoppingList(@Path("id") id: Int): Response<ShoppingListDto>
}

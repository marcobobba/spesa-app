package com.spesa.app.data.repository

import com.spesa.app.data.api.ApiService
import com.spesa.app.data.models.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepository @Inject constructor(private val api: ApiService) {

    suspend fun getRecipes(search: String? = null) = safeCall { api.getRecipes(search) }
    suspend fun getRecipe(id: Int) = safeCall { api.getRecipe(id) }
    suspend fun createRecipe(dto: CreateRecipeDto) = safeCall { api.createRecipe(dto) }
    suspend fun updateRecipe(id: Int, dto: CreateRecipeDto) = safeCall { api.updateRecipe(id, dto) }
    suspend fun deleteRecipe(id: Int) = safeCall { api.deleteRecipe(id) }

    private suspend fun <T> safeCall(call: suspend () -> retrofit2.Response<T>): Result<T> = runCatching {
        val response = call()
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Risposta vuota")
        } else {
            throw Exception(response.errorBody()?.string() ?: "Errore ${response.code()}")
        }
    }
}

package com.spesa.app.data.repository

import com.spesa.app.data.api.ApiService
import com.spesa.app.data.models.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(private val api: ApiService) {

    suspend fun getCategories() = safeCall { api.getCategories() }
    suspend fun createCategory(name: String) = safeCall { api.createCategory(CreateCategoryDto(name)) }
    suspend fun updateCategory(id: Int, name: String) = safeCall { api.updateCategory(id, CreateCategoryDto(name)) }
    suspend fun deleteCategory(id: Int) = safeCall { api.deleteCategory(id) }

    suspend fun getSupermarkets() = safeCall { api.getSupermarkets() }
    suspend fun createSupermarket(name: String, address: String?) = safeCall { api.createSupermarket(CreateSupermarketDto(name, address)) }
    suspend fun updateSupermarket(id: Int, name: String, address: String?) = safeCall { api.updateSupermarket(id, CreateSupermarketDto(name, address)) }
    suspend fun deleteSupermarket(id: Int) = safeCall { api.deleteSupermarket(id) }

    suspend fun getProducts(search: String? = null, categoryId: Int? = null) =
        safeCall { api.getProducts(search, categoryId) }

    suspend fun getProduct(id: Int) = safeCall { api.getProduct(id) }

    suspend fun createProduct(dto: CreateProductDto) = safeCall { api.createProduct(dto) }

    suspend fun updateProduct(id: Int, dto: CreateProductDto) = safeCall { api.updateProduct(id, dto) }

    suspend fun deleteProduct(id: Int) = safeCall { api.deleteProduct(id) }

    private suspend fun <T> safeCall(call: suspend () -> retrofit2.Response<T>): Result<T> = runCatching {
        val response = call()
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Risposta vuota")
        } else {
            throw Exception(response.errorBody()?.string() ?: "Errore ${response.code()}")
        }
    }
}

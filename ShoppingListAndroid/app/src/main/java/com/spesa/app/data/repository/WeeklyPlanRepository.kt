package com.spesa.app.data.repository

import com.spesa.app.data.api.ApiService
import com.spesa.app.data.models.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeeklyPlanRepository @Inject constructor(private val api: ApiService) {

    suspend fun getWeeklyPlans() = safeCall { api.getWeeklyPlans() }
    suspend fun getWeeklyPlan(id: Int) = safeCall { api.getWeeklyPlan(id) }
    suspend fun createWeeklyPlan(dto: CreateWeeklyPlanDto) = safeCall { api.createWeeklyPlan(dto) }
    suspend fun updateWeeklyPlan(id: Int, dto: CreateWeeklyPlanDto) = safeCall { api.updateWeeklyPlan(id, dto) }
    suspend fun deleteWeeklyPlan(id: Int) = safeCall { api.deleteWeeklyPlan(id) }
    suspend fun duplicateWeeklyPlan(id: Int, newName: String) = safeCall { api.duplicateWeeklyPlan(id, newName) }
    suspend fun getShoppingList(id: Int) = safeCall { api.getShoppingList(id) }

    private suspend fun <T> safeCall(call: suspend () -> retrofit2.Response<T>): Result<T> = runCatching {
        val response = call()
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Risposta vuota")
        } else {
            throw Exception(response.errorBody()?.string() ?: "Errore ${response.code()}")
        }
    }
}

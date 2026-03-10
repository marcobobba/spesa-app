package com.spesa.app.data.repository

import com.spesa.app.data.api.ApiService
import com.spesa.app.data.local.TokenManager
import com.spesa.app.data.models.AuthRequest
import com.spesa.app.data.models.AuthResponse
import com.spesa.app.data.models.RegisterRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun login(email: String, password: String): Result<AuthResponse> = runCatching {
        val response = api.login(AuthRequest(email, password))
        if (response.isSuccessful) {
            val body = response.body()!!
            tokenManager.saveAuth(body.token, body.name, body.email)
            body
        } else {
            throw Exception(response.errorBody()?.string() ?: "Credenziali non valide")
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<AuthResponse> = runCatching {
        val response = api.register(RegisterRequest(name, email, password))
        if (response.isSuccessful) {
            val body = response.body()!!
            tokenManager.saveAuth(body.token, body.name, body.email)
            body
        } else {
            throw Exception(response.errorBody()?.string() ?: "Registrazione fallita")
        }
    }

    suspend fun logout() = tokenManager.clearAuth()
}

package com.phytotec.recepcion.data.repository

import com.phytotec.recepcion.data.remote.ApiService
import com.phytotec.recepcion.data.remote.LoginRequest
import com.phytotec.recepcion.data.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
) {
    val isLoggedIn: Flow<Boolean> = sessionManager.tokenFlow.map { !it.isNullOrBlank() }

    val userName: Flow<String?> get() = sessionManager.userNameFlow
    val userRoles: Flow<List<String>> get() = sessionManager.userRolesFlow

    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        val response = apiService.login(LoginRequest(email, password))
        sessionManager.saveSession(
            response.token,
            "${response.user.firstName} ${response.user.lastName}",
            response.user.roles,
        )
    }

    suspend fun logout() {
        sessionManager.clear()
    }
}

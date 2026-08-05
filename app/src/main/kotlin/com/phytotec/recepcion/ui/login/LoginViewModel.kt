package com.phytotec.recepcion.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phytotec.recepcion.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value, error = null)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value, error = null)
    }

    fun login(onSuccess: () -> Unit) {
        if (uiState.email.isBlank() || uiState.password.isBlank()) {
            uiState = uiState.copy(error = "Ingresa tu correo y contraseña.")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(loading = true, error = null)

            authRepository.login(uiState.email.trim(), uiState.password)
                .onSuccess {
                    uiState = uiState.copy(loading = false)
                    onSuccess()
                }
                .onFailure { e ->
                    uiState = uiState.copy(loading = false, error = e.message ?: "No se pudo iniciar sesión.")
                }
        }
    }
}

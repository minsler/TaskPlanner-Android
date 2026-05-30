package com.example.taskplanner.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskplanner.data.remote.AuthApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val email: String = "",
    val password: String = "",
    val isLoginMode: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val token: String? = null,
    val registrationSuccess: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val authApi = AuthApi()

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value, error = null, registrationSuccess = false)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value, error = null)
    }

    fun toggleAuthMode() {
        _state.value = _state.value.copy(
            isLoginMode = !_state.value.isLoginMode,
            error = null,
            registrationSuccess = false
        )
    }

    fun authenticate() {
        val validationError = validateInput()
        if (validationError != null) {
            _state.value = _state.value.copy(error = validationError)
            return
        }

        if (_state.value.isLoginMode) {
            login()
        } else {
            register()
        }
    }

    private fun login() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, registrationSuccess = false)

            try {
                val token = authApi.login(
                    email = _state.value.email,
                    password = _state.value.password
                )

                _state.value = _state.value.copy(
                    isLoading = false,
                    token = token
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Ошибка входа"
                )
            }
        }
    }

    private fun register() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, registrationSuccess = false)

            try {
                authApi.register(
                    email = _state.value.email,
                    password = _state.value.password
                )

                _state.value = _state.value.copy(
                    isLoading = false,
                    isLoginMode = true,
                    password = "",
                    registrationSuccess = true
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Ошибка регистрации"
                )
            }
        }
    }

    private fun validateInput(): String? {
        val email = _state.value.email.trim()
        val password = _state.value.password

        return when {
            email.isBlank() -> "Введите email"
            !email.contains("@") || !email.contains(".") -> "Введите корректный email"
            password.isBlank() -> "Введите пароль"
            password.length < 6 -> "Пароль должен быть не короче 6 символов"
            else -> null
        }
    }
}

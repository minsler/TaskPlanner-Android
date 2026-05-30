package com.example.taskplanner.data.remote

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class TokenResponse(
    val token: String
)

class AuthApi {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    suspend fun login(email: String, password: String): String {
        val response: HttpResponse = ApiClient.httpClient.post("${ApiClient.BASE_URL}/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email = email.trim(), password = password))
        }

        val responseText = response.bodyAsText()

        if (response.status != HttpStatusCode.OK) {
            throw Exception(mapLoginError(responseText))
        }

        return try {
            json.decodeFromString<TokenResponse>(responseText).token
        } catch (e: Exception) {
            throw Exception("Сервер вернул некорректный ответ")
        }
    }

    suspend fun register(email: String, password: String): Boolean {
        val response: HttpResponse = ApiClient.httpClient.post("${ApiClient.BASE_URL}/register") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email = email.trim(), password = password))
        }

        val responseText = response.bodyAsText()

        if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
            return true
        }

        if (response.status == HttpStatusCode.Conflict || responseText.contains("exists", ignoreCase = true)) {
            throw Exception("Пользователь с таким email уже существует")
        }

        throw Exception("Не удалось создать аккаунт")
    }

    private fun mapLoginError(responseText: String): String {
        return when {
            responseText.contains("User not found", ignoreCase = true) -> {
                "Пользователь с таким email не найден"
            }
            responseText.contains("password", ignoreCase = true) ||
                responseText.contains("Invalid", ignoreCase = true) -> {
                "Неверный email или пароль"
            }
            else -> "Не удалось войти. Проверьте данные и подключение к серверу"
        }
    }
}

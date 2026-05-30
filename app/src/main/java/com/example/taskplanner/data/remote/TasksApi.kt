package com.example.taskplanner.data.remote

import com.example.taskplanner.data.local.TokenManager
import com.example.taskplanner.data.remote.dto.TaskDto
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskRequest(
    val title: String,
    val description: String
)

@Serializable
data class UpdateTaskRequest(
    val title: String,
    val description: String,
    val isCompleted: Boolean
)

class TasksApi {

    suspend fun getTasks(): List<TaskDto> {
        val response = ApiClient.httpClient.get("${ApiClient.BASE_URL}/tasks") {
            addAuthHeader()
            accept(ContentType.Application.Json)
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body()
        }

        throw Exception(mapTaskError(response.status, response.bodyAsText()))
    }

    suspend fun createTask(title: String, description: String) {
        val response = ApiClient.httpClient.post("${ApiClient.BASE_URL}/tasks") {
            addAuthHeader()
            contentType(ContentType.Application.Json)
            setBody(CreateTaskRequest(title, description))
        }

        if (response.status != HttpStatusCode.OK && response.status != HttpStatusCode.Created) {
            throw Exception(mapTaskError(response.status, response.bodyAsText()))
        }
    }

    suspend fun updateTask(taskId: Int, title: String, description: String, isCompleted: Boolean) {
        val response = ApiClient.httpClient.put("${ApiClient.BASE_URL}/tasks/$taskId") {
            addAuthHeader()
            contentType(ContentType.Application.Json)
            setBody(UpdateTaskRequest(title, description, isCompleted))
        }

        if (response.status != HttpStatusCode.OK) {
            throw Exception(mapTaskError(response.status, response.bodyAsText()))
        }
    }

    suspend fun deleteTask(taskId: Int) {
        val response = ApiClient.httpClient.delete("${ApiClient.BASE_URL}/tasks/$taskId") {
            addAuthHeader()
        }

        if (response.status != HttpStatusCode.OK && response.status != HttpStatusCode.NoContent) {
            throw Exception(mapTaskError(response.status, response.bodyAsText()))
        }
    }

    private fun io.ktor.client.request.HttpRequestBuilder.addAuthHeader() {
        val token = TokenManager.getToken()
        if (!token.isNullOrBlank()) {
            header("Authorization", "Bearer $token")
        }
    }

    private fun mapTaskError(status: HttpStatusCode, body: String): String {
        return when (status) {
            HttpStatusCode.Unauthorized -> "Сессия истекла. Войдите заново."
            HttpStatusCode.Forbidden -> "Нет доступа к этой задаче"
            else -> body.ifBlank { "Ошибка сервера: ${status.value}" }
        }
    }
}

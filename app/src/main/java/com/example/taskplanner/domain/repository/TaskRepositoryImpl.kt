package com.example.taskplanner.data.repository

import com.example.taskplanner.data.remote.TasksApi
import com.example.taskplanner.domain.model.Task
import com.example.taskplanner.domain.repository.TaskRepository
import com.example.taskplanner.data.remote.dto.toDomain

class TaskRepositoryImpl(
    private val api: TasksApi
) : TaskRepository {

    override suspend fun getTasks(): List<Task> {
        val dtos = api.getTasks()
        return dtos.map { it.toDomain() }
    }

    override suspend fun createTask(title: String, description: String) {
        api.createTask(title, description)
    }

    override suspend fun updateTask(task: Task) {
        // ИЗМЕНЕНИЕ: Передаем все данные задачи на сервер
        api.updateTask(task.id, task.title, task.description ?: "", task.isCompleted)
    }

    override suspend fun deleteTask(taskId: Int) {
        api.deleteTask(taskId)
    }
}
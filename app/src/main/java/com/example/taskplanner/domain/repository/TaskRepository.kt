package com.example.taskplanner.domain.repository

import com.example.taskplanner.domain.model.Task

interface TaskRepository {
    suspend fun getTasks(): List<Task>
    suspend fun createTask(title: String, description: String)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(taskId: Int)
}
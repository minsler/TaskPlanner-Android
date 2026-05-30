package com.example.taskplanner.data.remote.dto

import com.example.taskplanner.domain.model.Task

fun TaskDto.toDomain(): Task {
    return Task(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted
    )
}
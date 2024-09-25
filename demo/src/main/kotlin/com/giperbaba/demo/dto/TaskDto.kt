package com.giperbaba.demo.dto

import com.giperbaba.demo.entity.Task

class TaskDto (
    var description: String,
    var isDone: Boolean
)

fun TaskDto.toEntity(): Task {
    return Task(description = description, isDone = isDone)
}

fun Task.toDto(): TaskDto {
    return TaskDto(description, isDone)
}
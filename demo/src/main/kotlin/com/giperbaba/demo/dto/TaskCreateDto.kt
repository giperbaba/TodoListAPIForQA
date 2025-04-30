package com.giperbaba.demo.dto

import com.giperbaba.demo.entity.Task
import com.giperbaba.demo.enums.Priority
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDate

class TaskCreateDto (
    @field:NotBlank(message = "Название не может быть пустым")
    @field:Size(min = 4, message = "Название должно содержать минимум 4 символа")
    var name: String,
    var description: String? = "",
    var priority: Priority?,
    var deadline: LocalDate? = null,
)

fun TaskCreateDto.toEntity(): Task {
    return Task(name = name,
        description = description,
        isDone = false,
        priority = priority,
        deadline = deadline)
}


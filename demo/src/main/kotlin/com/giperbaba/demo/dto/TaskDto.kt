package com.giperbaba.demo.dto

import com.giperbaba.demo.entity.Task
import com.giperbaba.demo.enums.Priority
import java.time.LocalDate

class TaskCreateDto (
    var name: String,
    var description: String? = "",
    var isDone: Boolean = false,
    var priority: Priority? = Priority.Medium,
    var deadline: LocalDate? = null,
)

fun TaskCreateDto.toEntity(): Task {
    return Task(name = name,
        description = description,
        isDone = isDone,
        priority = priority,
        deadline = deadline)
}


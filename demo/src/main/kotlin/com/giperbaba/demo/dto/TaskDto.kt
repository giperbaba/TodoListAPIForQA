package com.giperbaba.demo.dto

import com.giperbaba.demo.entity.Task
import com.giperbaba.demo.enums.Priority
import java.time.LocalDate

class TaskCreateDto (
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


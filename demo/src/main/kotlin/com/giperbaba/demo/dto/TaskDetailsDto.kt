package com.giperbaba.demo.dto

import com.giperbaba.demo.entity.Task
import com.giperbaba.demo.enums.Priority
import com.giperbaba.demo.enums.Status
import java.time.LocalDate
import java.util.*

data class TaskDetailsDto(
    val id: Long?,
    val name: String,
    val description: String?,
    val isDone: Boolean,
    val status: Status?,
    val priority: Priority?,
    val dateCreated: LocalDate,
    val dateUpdated: LocalDate?,
    val deadline: LocalDate?
)

fun Task.toDetailsDto(): TaskDetailsDto {
    return TaskDetailsDto(
        id = this.id,
        name = this.name,
        description = this.description,
        isDone = this.isDone,
        status = this.status,
        priority = this.priority,
        dateCreated = this.dateCreated ?: LocalDate.now(),
        dateUpdated = this.dateUpdated,
        deadline = this.deadline
    )
}

fun TaskDetailsDto.toEntity(): Task {
    return Task(
        id = this.id,
        name = this.name,
        description = this.description,
        isDone = this.isDone,
        status = this.status,
        priority = this.priority,
        dateCreated = this.dateCreated,
        dateUpdated = this.dateUpdated,
        deadline = this.deadline
    )
}

fun List<TaskDetailsDto>.sortByPriority(): List<TaskDetailsDto> {
    return this.sortedBy { it.priority?.ordinal ?: Priority.Medium.ordinal }
}

fun List<TaskDetailsDto>.sortByStatus(): List<TaskDetailsDto> {
    return this.sortedBy { it.status?.ordinal ?: Status.Active.ordinal }
}

fun List<TaskDetailsDto>.sortByDateCreated(ascending: Boolean = true): List<TaskDetailsDto> {
    return if (ascending) {
        this.sortedBy { it.dateCreated }
    } else {
        this.sortedByDescending { it.dateCreated }
    }
}
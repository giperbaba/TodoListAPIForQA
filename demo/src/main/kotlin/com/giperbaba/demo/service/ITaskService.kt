package com.giperbaba.demo.service

import com.giperbaba.demo.dto.*
import java.time.LocalDate

interface ITaskService {

    fun create(taskDto: TaskCreateDto): TaskDetailsDto

    fun deleteTask(id: Long)

    fun updateTaskName(id: Long, request: UpdateTaskNameRequest)

    fun updateTaskDescription(id: Long, request: UpdateTaskDescriptionRequest)

    fun updateTaskPriority(id: Long, request: UpdateTaskPriorityRequest)

    fun updateTaskIsDone(id: Long, request: UpdateTaskIsDoneRequest)

    fun updateTaskDeadline(id: Long, request: UpdateTaskDeadlineRequest)

    fun getTasks( filter: TaskFilterRequest): List<TaskDetailsDto>

    fun getTask(id: Long): TaskDetailsDto

}
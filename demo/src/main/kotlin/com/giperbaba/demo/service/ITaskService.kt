package com.giperbaba.demo.service

import com.giperbaba.demo.dto.*

interface ITaskService {

    fun create(taskDto: TaskCreateDto): TaskDetailsDto

    fun deleteTask(id: Long)

    fun updateTaskName(id: Long, request: UpdateTaskNameRequest)

    fun updateTaskDescription(id: Long, request: UpdateTaskDescriptionRequest)

    fun updateTaskPriority(id: Long, request: UpdateTaskPriorityRequest)

    fun updateTaskIsDone(id: Long, request: UpdateTaskIsDoneRequest)

    fun getTasks( filter: TaskFilterRequest): List<TaskDetailsDto>

}
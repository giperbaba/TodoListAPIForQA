package com.giperbaba.demo.service

import com.giperbaba.demo.repository.TaskRepository
import org.springframework.stereotype.Service
import com.giperbaba.demo.dto.*
import com.giperbaba.demo.entity.Task
import com.giperbaba.demo.mapper.TaskMapper
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate


@Service
class TaskService(private val repository: TaskRepository): ITaskService {

    override fun create(taskDto: TaskCreateDto): TaskDetailsDto {
        val entity = TaskMapper.mapToEntity(taskDto)
        return repository.save(entity).toDetailsDto()
    }

    override fun deleteTask(id: Long) {
        val task = repository.findById(id)
            .orElseThrow { EntityNotFoundException("Task with id $id not found") }
        repository.delete(task)
    }

    override fun updateTaskName(id: Long, request: UpdateTaskNameRequest) {
        updateTask(id) { task ->
            val (cleanName, params) = TaskMapper.extractParameters(request.newName)
            task.name = cleanName
            task.priority = params.priority ?: task.priority
            task.deadline = params.deadline ?: task.deadline
        }
    }

    override fun updateTaskDescription(id: Long, request: UpdateTaskDescriptionRequest) {
        updateTask(id) { task ->
            task.description = request.newDescription
        }
    }

    override fun updateTaskPriority(id: Long, request: UpdateTaskPriorityRequest) {
        updateTask(id) { task ->
            task.priority = request.newPriority
        }
    }

    override fun updateTaskIsDone(id: Long, request: UpdateTaskIsDoneRequest) {
        updateTask(id) { task ->
            task.isDone = request.isDone
            task.updateStatus()
        }
    }

    override fun updateTaskDeadline(id: Long, request: UpdateTaskDeadlineRequest) {
        updateTask(id) { task ->
            task.deadline = request.newDeadline
        }
    }

    private inline fun updateTask(id: Long, updateAction: (Task) -> Unit) {
        val task = repository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found")
        }

        updateAction(task)
        task.dateUpdated = LocalDate.now()
        repository.save(task)
    }


    override fun getTasks(filter: TaskFilterRequest): List<TaskDetailsDto> {
        return repository.findAll()
            .map {
                it.updateStatus()
                it.toDetailsDto()
            }
            .filter { task ->
                (filter.priorities == null || task.priority in filter.priorities) &&
                        (filter.statuses == null || task.status in filter.statuses) &&
                        (filter.isDone == null || task.isDone == filter.isDone) &&
                        (filter.name.isNullOrBlank() || task.name.contains(filter.name, ignoreCase = true))
            }
    }

    override fun getTask(id: Long): TaskDetailsDto {
        return repository.findById(id)
            .map {
                it.updateStatus()
                it.toDetailsDto()
            }
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found")
            }
    }
}
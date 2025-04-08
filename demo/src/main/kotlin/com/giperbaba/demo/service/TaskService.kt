package com.giperbaba.demo.service

import com.giperbaba.demo.repository.TaskRepository
import org.springframework.stereotype.Service
import com.giperbaba.demo.dto.*
import com.giperbaba.demo.entity.Task
import com.giperbaba.demo.enums.Priority
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*


@Service
class TaskService(private val repository: TaskRepository): ITaskService {

    private val parameterPatterns = mapOf(
        "deadline" to Regex("!before\\s(\\d{2}[.-]\\d{2}[.-]\\d{4})"),
        "priority" to Regex("!(\\d)(?!\\d)")
    )

    override fun create(taskDto: TaskCreateDto): TaskDetailsDto {
        val entity = taskDto.toEntity().apply {
            val (cleanName, params) = extractAndApplyParameters(this.name)
            name = cleanName
            deadline = params["deadline"] as? LocalDate
            priority = params["priority"] as? Priority ?: Priority.Medium
            updateStatus()
        }
        return repository.save(entity).toDetailsDto();
    }

    private fun extractAndApplyParameters(rawName: String): Pair<String, Map<String, Any>> {
        var cleanName = rawName
        val params = mutableMapOf<String, Any>()

        parameterPatterns.forEach { (type, pattern) ->
            pattern.find(rawName)?.let { match ->
                when (type) {
                    "deadline" -> {
                        val dateStr = match.groupValues[1]
                        params["deadline"] = parseDate(dateStr)
                    }
                    "priority" -> {
                        params["priority"] = parsePriority(match.groupValues[1])
                    }
                }
                cleanName = cleanName.replace(match.value, "").trim()
            }
        }

        return cleanName to params
    }

    private fun parseDate(dateStr: String): LocalDate {
        val formatters = listOf(
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
        )

        for (formatter in formatters) {
            try {
                return LocalDate.parse(dateStr, formatter)
            } catch (e: DateTimeParseException) {
                // Пробуем следующий формат
            }
        }
        throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Invalid date format. Use DD.MM.YYYY or DD-MM-YYYY"
        )
    }

    private fun parsePriority(priorityStr: String): Priority {
        return when (priorityStr) {
            "1" -> Priority.Critical
            "2" -> Priority.High
            "3" -> Priority.Medium
            "4" -> Priority.Low
            else -> throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid priority. Use !1 (Critical) to !4 (Low)"
            )
        }
    }

    override fun deleteTask(id: Long) {
        repository.deleteById(id)
    }

    override fun updateTaskName(id: Long, request: UpdateTaskNameRequest) {
        val taskOption = repository.findById(id)
        update(task = taskOption, newName = request.newName)
    }

    override fun updateTaskDescription(id: Long, request: UpdateTaskDescriptionRequest) {
        val taskOption = repository.findById(id)
        update(task = taskOption, newDescription = request.newDescription)
    }

    override fun updateTaskPriority(id: Long, request: UpdateTaskPriorityRequest) {
        val taskOption = repository.findById(id)
        update(task = taskOption, newPriority = request.newPriority)
    }

    override fun updateTaskIsDone(id: Long, request: UpdateTaskIsDoneRequest) {
        val taskOption = repository.findById(id)
        update(task = taskOption, isDone = request.isDone)
    }

    private fun update(task: Optional<Task>, isDone: Boolean? = null, newDescription: String? = null, newName: String? = null, newPriority: Priority? = null) {
        if (task.isPresent) {
            val task = task.get()
            if (task.isDone != isDone && isDone != null) {
                task.isDone = isDone
            }
            if (task.description != newDescription && newDescription != null) {
                task.description = newDescription
            }
            if (task.name != newName && newName != null) {
                task.name = newName
            }
            if (task.priority != newPriority && newPriority != null) {
                task.priority = newPriority
            }
            task.dateUpdated = LocalDate.now()
            repository.save(task)
        }
        else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found")
        }
    }

    override fun getTasks( filter: TaskFilterRequest ): List<TaskDetailsDto> {
        var nameFilter = filter.name;
        return repository.findAll().map { it.toDetailsDto() }
            .filter { task -> (filter.priorities == null || task.priority in filter.priorities)
                        && (filter.statuses == null || task.status in filter.statuses)
                        && (filter.isDone == null || task.isDone == filter.isDone)
                        && (nameFilter.isNullOrBlank() || task.name.contains(nameFilter, ignoreCase = true))
            }
    }
}
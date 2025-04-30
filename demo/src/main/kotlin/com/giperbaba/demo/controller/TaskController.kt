package com.giperbaba.demo.controller

import com.giperbaba.demo.dto.*
import com.giperbaba.demo.service.ITaskService
import jakarta.persistence.EntityNotFoundException
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/todo_list")
@CrossOrigin(origins = ["*"])
class TaskController(private val service: ITaskService) {

    @PostMapping("create")
    fun createTask(@Valid @RequestBody taskDto: TaskCreateDto): ResponseEntity<TaskDetailsDto> {
        val task = service.create(taskDto)
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("delete/{id}")
    fun deleteTask(@PathVariable id: Long): ResponseEntity<Void> {
        return try {
            service.deleteTask(id)
            ResponseEntity.ok().build()
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("update/name/{id}")
    fun updateTaskName(@PathVariable id: Long, @RequestBody updateTaskDto: UpdateTaskNameRequest):
            ResponseEntity<String> {
        try {
            service.updateTaskName(id, updateTaskDto)
            return ResponseEntity.ok().build()
        }
        catch (e: Exception) {
            return ResponseEntity.badRequest().body(e.message)
        }
    }

    @PutMapping("update/desc/{id}")
    fun updateTaskDescription(@PathVariable id: Long, @RequestBody updateTaskDto: UpdateTaskDescriptionRequest):
            ResponseEntity<String> {
        try {
            service.updateTaskDescription(id, updateTaskDto)
            return ResponseEntity.ok().build()
        }
        catch (e: Exception) {
            return ResponseEntity.badRequest().body(e.message)
        }
    }

    @PutMapping("update/is_done/{id}")
    fun updateTaskIsDone(@PathVariable id: Long, @RequestBody updateTaskDto: UpdateTaskIsDoneRequest):
            ResponseEntity<String> {
        try {
            service.updateTaskIsDone(id, updateTaskDto)
            return ResponseEntity.ok().build()
        }
        catch (e: Exception) {
            return ResponseEntity.badRequest().body(e.message)
        }
    }

    @PutMapping("update/priority/{id}")
    fun updateTaskPriority(@PathVariable id: Long, @RequestBody updateTaskDto: UpdateTaskPriorityRequest):
            ResponseEntity<String> {
        try {
            service.updateTaskPriority(id, updateTaskDto)
            return ResponseEntity.ok().build()
        }
        catch (e: Exception) {
            return ResponseEntity.badRequest().body(e.message)
        }
    }

    @PutMapping("update/deadline/{id}")
    fun updateTaskDeadline(@PathVariable id: Long, @RequestBody updateTaskDto: UpdateTaskDeadlineRequest):
            ResponseEntity<String> {
        try {
            service.updateTaskDeadline(id, updateTaskDto)
            return ResponseEntity.ok().build()
        }
        catch (e: Exception) {
            return ResponseEntity.badRequest().body(e.message)
        }
    }

    @GetMapping
    fun getTasks( @ModelAttribute filter: TaskFilterRequest ): ResponseEntity<List<TaskDetailsDto>> {
        val tasks = service.getTasks(filter)
        return ResponseEntity.ok(tasks)
    }

    @GetMapping("task/{id}")
    fun getTask( @PathVariable id: Long): ResponseEntity<TaskDetailsDto> {
        val task = service.getTask(id)
        return ResponseEntity.ok(task)
    }
}
package com.giperbaba.demo.service

import com.giperbaba.demo.dto.TaskDto
import com.giperbaba.demo.dto.toDto
import com.giperbaba.demo.dto.toEntity
import com.giperbaba.demo.repository.ItemRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

//реализация бизнес-логики

@Service
class ItemService(private val repository: ItemRepository) {

    fun save(taskDto: TaskDto) {
        repository.save(taskDto.toEntity())
    }

    fun deleteTask(id: Long) {
        repository.deleteById(id)
    }

    fun <T> updateCondition(id: Long, newCondition: T) {
        val taskOption = repository.findById(id)
        if (taskOption.isPresent) {
            val task = taskOption.get()
            if (newCondition is Boolean) {
                task.isDone = newCondition
            } else if (newCondition is String) {
                task.description = newCondition
            }
            repository.save(task)
        } else {
            throw Exception("task with this id $id not found")
        }
    }

    fun getTasks(): List<TaskDto> {
        return repository.findAll().map { it.toDto() }
    }

    fun uploadArray(tasks: List<TaskDto>) {
        if (tasks.isEmpty()) {
            throw Exception("array is empty")
        }
        repository.deleteAll()
        for (task in tasks) {
            save(task)
        }
    }

    fun uploadJson(file: MultipartFile) {
        if (file.isEmpty) {
            throw Exception("file is empty")
        }
        val objectMapper = jacksonObjectMapper()
        val jsonContent = String(file.bytes)
        val tasks: List<TaskDto> = objectMapper.readValue(jsonContent)
        uploadArray(tasks)
    }
}
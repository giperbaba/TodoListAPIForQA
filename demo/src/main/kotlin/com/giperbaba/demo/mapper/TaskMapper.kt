package com.giperbaba.demo.mapper

import com.giperbaba.demo.dto.TaskCreateDto
import com.giperbaba.demo.entity.Task
import com.giperbaba.demo.enums.Priority
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object TaskMapper {
    private val deadlineRegex = Regex("!before\\s+(\\d{2}[.]\\d{2}[.]\\d{4}|\\d{2}-\\d{2}-\\d{4})")
    private val priorityRegex = Regex("!([1-4])")
    private val supportedDateFormats = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("dd.MM.yyyy"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy")
    )


    fun mapToEntity(dto: TaskCreateDto): Task {
        val (cleanName, extractedParams) = extractParameters(dto.name)

        return Task(
            name = cleanName,
            description = dto.description,
            priority = dto.priority ?: extractedParams.priority ?: Priority.Medium,
            deadline = dto.deadline ?: extractedParams.deadline
        ).apply { updateStatus() }
    }

    fun extractParameters(rawName: String): Pair<String, ExtractedParams> {
        var cleanName = rawName
        val params = ExtractedParams()

        deadlineRegex.find(cleanName)?.let { match ->
            params.deadline = parseDate(match.groupValues[1])
            cleanName = cleanName.replace(match.value, "").trim()
        }

        priorityRegex.find(cleanName)?.let { match ->
            params.priority = parsePriority(match.groupValues[1])
            cleanName = cleanName.replace(match.value, "").trim()
        }


        validateNameLength(cleanName, params)

        return cleanName to params
    }


    private fun parseDate(dateStr: String): LocalDate {
        for (formatter in supportedDateFormats) {
            try {
                return LocalDate.parse(dateStr, formatter)
            } catch (e: DateTimeParseException) {
                continue;
            }
        }
        throw IllegalArgumentException("Неподдерживаемый формат даты: $dateStr")
    }

    private fun parsePriority(priorityStr: String): Priority {
        return when (priorityStr) {
            "1" -> Priority.Critical
            "2" -> Priority.High
            "3" -> Priority.Medium
            "4" -> Priority.Low
            else -> throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Приоритет должен быть от !1 (Critical) до !4 (Low)"
            )
        }
    }

    private fun validateNameLength(name: String, params: ExtractedParams) {
        if (name.length < 4 && (params.priority != null || params.deadline != null)) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Название задачи должно содержать минимум 4 символа помимо макросов"
            )
        }
    }
}

data class ExtractedParams(
    var priority: Priority? = null,
    var deadline: LocalDate? = null
)
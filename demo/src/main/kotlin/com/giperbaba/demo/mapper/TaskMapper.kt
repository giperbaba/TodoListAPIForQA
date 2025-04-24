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

        if (cleanName.contains("!before")) {
            throw IllegalArgumentException("Неподдерживаемый формат даты: макрос указан, но не распознан")
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
                val parsedDate = LocalDate.parse(dateStr, formatter)
                val formattedBack = parsedDate.format(formatter)

                // если после парсинга и обратно форматирования дата отличается — значит была невалидная
                if (!formattedBack.equals(dateStr, ignoreCase = true)) {
                    continue
                }

                return parsedDate
            }
            catch (e: DateTimeParseException) {
                continue
            }
        }
        throw IllegalArgumentException("Неподдерживаемый формат даты: $dateStr")
    }

    private fun parsePriority(priorityStr: String): Priority {
        if (priorityStr == "1") {
            return Priority.Critical
        }
        else if (priorityStr == "2") {
            return Priority.High
        }
        else if (priorityStr == "4") {
            return Priority.Low
        }
        else {
            return Priority.Medium
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
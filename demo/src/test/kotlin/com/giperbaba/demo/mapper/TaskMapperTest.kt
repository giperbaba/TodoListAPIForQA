package com.giperbaba.demo.mapper

import com.giperbaba.demo.dto.TaskCreateDto
import com.giperbaba.demo.enums.Priority
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TaskMapperTest {

    @BeforeEach
    fun setup() {
        println("Running test...")
    }

    @AfterEach
    fun teardown() {
        println("Test finished.")
    }

    // Валидные даты
    @ParameterizedTest
    @CsvSource(
        "!before 01.05.2025,2025-05-01",
        "!before 01-05-2025,2025-05-01",
        "!before 31-12-2024,2024-12-31",
        "!before 15.06.2025,2025-06-15",
        "!before 29.02.2024,2024-02-29"
    )
    fun `should parse valid deadline formats correctly`(input: String, expectedDate: String) {
        val (cleanName, params) = TaskMapper.extractParameters("Test $input")
        assertEquals("Test", cleanName)
        assertEquals(LocalDate.parse(expectedDate), params.deadline)
    }

    // Невалидные даты
    @ParameterizedTest
    @CsvSource("!before 2025/01/01", "!before 01.13.2025", "!before 31.02.2025", "!before 31.04.2025", "!before 31.02.2025")
    fun `should throw error on invalid date formats`(input: String) {
        val exception = assertFailsWith<IllegalArgumentException> {
            TaskMapper.extractParameters("Task $input")
        }
        assert(exception.message!!.contains("Неподдерживаемый формат даты"))
    }

    // Приоритет макросов
    @ParameterizedTest
    @CsvSource(
        "!1,Critical",
        "!2,High",
        "!3,Medium",
        "!4,Low"
    )
    fun `should parse valid priority macros`(macro: String, expected: Priority) {
        val (cleanName, params) = TaskMapper.extractParameters("Task $macro")
        assertEquals("Task", cleanName)
        assertEquals(expected, params.priority)
    }

    // Корректное количество символов
    @Test
    fun `should pass for exactly 4 characters name`() {
        val name = "Abcd !1"
        val (clean, _) = TaskMapper.extractParameters(name)
        assertEquals("Abcd", clean)
    }

    // Некорректное количество символов
    @Test
    fun `should fail for name with less than 4 characters`() {
        val exception = assertFailsWith<ResponseStatusException> {
            TaskMapper.extractParameters("Ab !2")
        }
        assertEquals("400 BAD_REQUEST \"Название задачи должно содержать минимум 4 символа помимо макросов\"", exception.message)
    }

    // Несколько макросов
    @ParameterizedTest
    @CsvSource(
        "Task !1 !before 01.01.2025, Critical, 2025-01-01",
        "Task !before 01.01.2025 !2, High, 2025-01-01",
        "Task !4 !before 01-06-2025, Low, 2025-06-01",
        "Task !3, Medium,"
    )
    fun `should parse both priority and deadline macros`(input: String, expectedPriority: Priority, expectedDate: String?) {
        val (clean, params) = TaskMapper.extractParameters(input)
        assertEquals("Task", clean)
        assertEquals(expectedPriority, params.priority)
        expectedDate?.let {
            assertEquals(LocalDate.parse(it), params.deadline)
        }
    }

    // Приоритет макросов
    @ParameterizedTest
    @CsvSource(
        "!1,High",
        "!2,Critical",
        "!3,Low",
        "!4,Medium"
    )
    fun `should prioritize field value over macro`(macro: String, fieldPriority: Priority) {
        val dto = TaskCreateDto(
            name = "Task $macro",
            description = "Test",
            priority = fieldPriority,
            deadline = null
        )
        val entity = TaskMapper.mapToEntity(dto)
        assertEquals(fieldPriority, entity.priority)
    }

    // Приоритет макросов дедлайна
    @ParameterizedTest
    @CsvSource(
        "2025-06-01,01.07.2025",
        "2025-01-01,02.01.2025"
    )
    fun `should prioritize deadline from field over macro`(fieldDate: String, macroDate: String) {
        val dto = TaskCreateDto(
            name = "Task !before $macroDate",
            description = "desc",
            priority = null,
            deadline = LocalDate.parse(fieldDate)
        )
        val entity = TaskMapper.mapToEntity(dto)
        assertEquals(LocalDate.parse(fieldDate), entity.deadline)
    }

    // Приорирет с поля
    @Test
    fun `should parse priority only from field`() {
        val dto = TaskCreateDto("Task", "desc", Priority.High, null)
        val entity = TaskMapper.mapToEntity(dto)
        assertEquals(Priority.High, entity.priority)
    }

    // Дедлайн с поля
    @Test
    fun `should parse deadline only from field`() {
        val date = LocalDate.of(2025, 5, 1)
        val dto = TaskCreateDto("Task", "desc", null, date)
        val entity = TaskMapper.mapToEntity(dto)
        assertEquals(date, entity.deadline)
    }

    // Пустое название
    @Test
    fun `should fail for empty name`() {
        val exception = assertFailsWith<ResponseStatusException> {
            TaskMapper.extractParameters("!1 !before 01.01.2025")
        }
        assertEquals("400 BAD_REQUEST \"Название задачи должно содержать минимум 4 символа помимо макросов\"", exception.message)
    }

    // Пустое название (из пробелов)
    @Test
    fun `should fail for name with only spaces and macros`() {
        val exception = assertFailsWith<ResponseStatusException> {
            TaskMapper.extractParameters("   !1 !before 01.01.2025")
        }
        assertEquals("400 BAD_REQUEST \"Название задачи должно содержать минимум 4 символа помимо макросов\"", exception.message)
    }

    // Игнорирует неизвестный макрос
    @Test
    fun `should ignore unknown macros in name`() {
        val (clean, params) = TaskMapper.extractParameters("Task !9")
        assertEquals("Task !9", clean)
        assertEquals(null, params.priority)
    }

    // Очищает пробелы и парсит макросы
    @Test
    fun `should clean name and parse correctly with mixed spacing`() {
        val (clean, params) = TaskMapper.extractParameters("Task !1    !before   01.01.2025")
        assertEquals("Task", clean)
        assertEquals(Priority.Critical, params.priority)
        assertEquals(LocalDate.parse("2025-01-01"), params.deadline)
    }

    // Без макросов
    @Test
    fun `should handle no macros gracefully`() {
        val (clean, params) = TaskMapper.extractParameters("Regular task name")
        assertEquals("Regular task name", clean)
        assertEquals(null, params.priority)
        assertEquals(null, params.deadline)
    }

    // Все параметры
    @Test
    fun `should create task entity from full dto`() {
        val dto = TaskCreateDto("Test !2 !before 01.01.2025", "desc", null, null)
        val entity = TaskMapper.mapToEntity(dto)
        assertEquals("Test", entity.name)
        assertEquals("desc", entity.description)
        assertEquals(Priority.High, entity.priority)
        assertEquals(LocalDate.parse("2025-01-01"), entity.deadline)
    }
}

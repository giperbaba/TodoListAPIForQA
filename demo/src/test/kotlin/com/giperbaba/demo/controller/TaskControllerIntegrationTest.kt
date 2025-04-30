package com.giperbaba.demo.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.giperbaba.demo.dto.*
import com.giperbaba.demo.enums.Priority
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate
import com.giperbaba.demo.repository.TaskRepository

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TaskControllerIntegrationTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper
) {

    @Autowired
    private lateinit var taskRepository: TaskRepository
    private lateinit var validDto: TaskCreateDto

    @BeforeAll
    fun setupGlobal() {
    }

    @BeforeEach
    fun setup() {
        validDto = TaskCreateDto(
            name = "Valid Task Name",
            description = "Valid description",
            priority = Priority.High,
            deadline = LocalDate.now().plusDays(3),
        )
    }

    //создание задачи с валидными данными
    @Test
    @Order(1)
    fun `should create task with valid data`() {
        mockMvc.perform(
            post("/api/todo_list/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDto))
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Valid Task Name"))
            .andExpect(jsonPath("$.priority").value(Priority.High.toString()))
    }

    //создание задачи с невалидными именами задач
    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = ["", "a", "ab", "abc"])
    fun `should fail on invalid name length`(invalidName: String) {
        val dto = validDto.apply { name = invalidName }

        val content = objectMapper.writeValueAsString(dto)
        val requestBuilder = post("/api/todo_list/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)

        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest)
    }

    @Test
    @Order(3)
    fun `should get task by id`() {
        val dto = validDto.apply { name = "Task for get test" }
        val createResult = mockMvc.perform(
            post("/api/todo_list/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andReturn()

        val taskId = objectMapper.readTree(createResult.response.contentAsString).get("id").asLong()

        mockMvc.perform(get("/api/todo_list/task/$taskId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(taskId))
            .andExpect(jsonPath("$.name").value("Task for get test"))
    }

    // получение списка задач
    @Test
    @Order(4)
    fun `should get all tasks`() {
        mockMvc.perform(get("/api/todo_list"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }

     // удаление задачи существующий id
    @Test
    @Order(5)
    fun `should delete task by id`() {
        val dto = validDto
        dto.name = "Task to delete"

        val result = mockMvc.perform(
            post("/api/todo_list/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andReturn()

        val taskId = objectMapper.readTree(result.response.contentAsString).get("id").asLong()

        mockMvc.perform(delete("/api/todo_list/delete/$taskId"))
            .andExpect(status().isOk)

        mockMvc.perform(delete("/api/todo_list/delete/$taskId"))
            .andExpect(status().isNotFound)
    }

    // удаление задачи несуществующий id
    @Test
    @Order(6)
    fun `should return not found when deleting non-existing task`() {
        val nonExistingId = 999999L

        mockMvc.perform(delete("/api/todo_list/delete/$nonExistingId"))
            .andExpect(status().isNotFound)
    }


    // обновление названия задачи корректное
    @Test
    @Order(7)
    fun `should update task name`() {
        val dto = validDto.apply { name = "Old name" }
        val createResult = mockMvc.perform(
            post("/api/todo_list/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andReturn()

        val taskId = objectMapper.readTree(createResult.response.contentAsString).get("id").asLong()
        val updateDto = UpdateTaskNameRequest("New name")

        mockMvc.perform(
            put("/api/todo_list/update/name/$taskId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
        ).andExpect(status().isOk)
    }

    // обновление название задачи на пустое
    @Test
    @Order(8)
    fun `should fail to update task name with empty name`() {
        val dto = validDto.apply { name = "Valid name" }
        val createResult = mockMvc.perform(
            post("/api/todo_list/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andReturn()

        val taskId = objectMapper.readTree(createResult.response.contentAsString).get("id").asLong()
        val updateDto = UpdateTaskNameRequest("")

        mockMvc.perform(
            put("/api/todo_list/update/name/$taskId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
        ).andExpect(status().isBadRequest)
    }

    // обновление названия задачи у несуществующей задачи
    @Test
    @Order(9)
    fun `should return bad request for updating non-existing task name`() {
        val updateDto = UpdateTaskNameRequest("New name")

        mockMvc.perform(
            put("/api/todo_list/update/name/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
        ).andExpect(status().isBadRequest)
    }

    //обновление описания корректное
    @Test
    @Order(10)
    fun `should update task description`() {
        val dto = validDto.apply { name = "Task for description test" }
        val createResult = mockMvc.perform(
            post("/api/todo_list/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andReturn()

        val taskId = objectMapper.readTree(createResult.response.contentAsString).get("id").asLong()
        val updateDto = UpdateTaskDescriptionRequest("Updated description")

        mockMvc.perform(
            put("/api/todo_list/update/desc/$taskId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
        ).andExpect(status().isOk)
    }

    //обновление описания несуществующей задачи
    @Test
    @Order(11)
    fun `should return bad request for updating non-existing task description`() {
        val updateDto = UpdateTaskDescriptionRequest("New description")

        mockMvc.perform(
            put("/api/todo_list/update/desc/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
        ).andExpect(status().isBadRequest)
    }

    // обновление is_done корректное
    @Test
    @Order(12)
    fun `should update task is_done status`() {
        val dto = validDto.apply { name = "Task for is_done test" }
        val createResult = mockMvc.perform(
            post("/api/todo_list/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andReturn()

        val taskId = objectMapper.readTree(createResult.response.contentAsString).get("id").asLong()
        val updateDto = UpdateTaskIsDoneRequest(isDone = true)

        mockMvc.perform(
            put("/api/todo_list/update/is_done/$taskId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
        ).andExpect(status().isOk)
    }

    //обновление is_done несуществующей задачи
    @Test
    @Order(13)
    fun `should return bad request for updating non-existing task is_done status`() {
        val updateDto = UpdateTaskIsDoneRequest(isDone = true)

        mockMvc.perform(
            put("/api/todo_list/update/is_done/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
        ).andExpect(status().isBadRequest)
    }


    //обновление приоритета корректное
    @Test
    @Order(14)
    fun `should update task priority`() {
        val dto = validDto.apply { name = "Task for priority test" }
        val createResult = mockMvc.perform(
            post("/api/todo_list/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andReturn()

        val taskId = objectMapper.readTree(createResult.response.contentAsString).get("id").asLong()
        val updateDto = UpdateTaskPriorityRequest(Priority.Medium)

        mockMvc.perform(
            put("/api/todo_list/update/priority/$taskId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
        ).andExpect(status().isOk)
    }

    //обновление приоритета несуществующей задачи
    @Test
    @Order(15)
    fun `should return bad request for updating non-existing task priority`() {
        val updateDto = UpdateTaskPriorityRequest(Priority.Medium)

        mockMvc.perform(
            put("/api/todo_list/update/priority/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
        ).andExpect(status().isBadRequest)
    }

    //обновление дедлайна корректное
    @Test
    @Order(16)
    fun `should update task deadline`() {
        val dto = validDto.apply { name = "Task for deadline test" }
        val createResult = mockMvc.perform(
            post("/api/todo_list/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andReturn()

        val taskId = objectMapper.readTree(createResult.response.contentAsString).get("id").asLong()
        val updateDto = UpdateTaskDeadlineRequest(LocalDate.now().plusDays(5)) // новый дедлайн

        mockMvc.perform(
            put("/api/todo_list/update/deadline/$taskId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
        ).andExpect(status().isOk)
    }

    //обновление дедлайна несуществующей задачи
    @Test
    @Order(17)
    fun `should return bad request for updating non-existing task deadline`() {
        val updateDto = UpdateTaskDeadlineRequest(LocalDate.now().plusDays(5))

        mockMvc.perform(
            put("/api/todo_list/update/deadline/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
        ).andExpect(status().isBadRequest)
    }

    @AfterEach
    fun teardown() {
        taskRepository.deleteAll()
    }
}

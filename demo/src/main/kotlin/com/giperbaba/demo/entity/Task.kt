package com.giperbaba.demo.entity

import com.giperbaba.demo.enums.Priority
import com.giperbaba.demo.enums.Status
import jakarta.annotation.Nullable
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.validation.constraints.Size
import java.time.LocalDate

@Entity
data class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:Size(min = 4, message = "Name must be at least 4 characters long")
    var name: String = "",

    @Nullable
    var description: String? = null,

    var isDone: Boolean = false,

    var status: Status? = Status.Active,

    var priority: Priority? = Priority.Medium,

    var dateCreated: LocalDate = LocalDate.now(),

    @Nullable
    var dateUpdated: LocalDate? = null,

    @Nullable
    var deadline: LocalDate? = null,
) {
    fun updateStatus() {
        status = when {
            isDone -> calculateDoneStatus()
            deadline == null -> Status.Active
            LocalDate.now() > deadline -> Status.Overdue
            else -> Status.Active
        }
    }

    private fun calculateDoneStatus(): Status {
        return when {
            deadline == null -> Status.Completed
            LocalDate.now() <= deadline -> Status.Completed
            else -> Status.Late
        }
    }
}
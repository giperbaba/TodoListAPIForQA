package com.giperbaba.demo.dto

import com.giperbaba.demo.enums.Priority
import com.giperbaba.demo.enums.Status

data class TaskFilterRequest(
    val priorities: List<Priority>? = null,
    val statuses: List<Status>? = null,
    val isDone: Boolean? = null,
    val name: String? = null
)
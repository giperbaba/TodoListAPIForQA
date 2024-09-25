package com.giperbaba.demo.dto

data class UpdateTaskDto<T> (
    val newCondition: T? = null
)
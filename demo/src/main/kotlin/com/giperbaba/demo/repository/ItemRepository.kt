package com.giperbaba.demo.repository

import com.giperbaba.demo.entity.Task
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

//взаимодействие с базой данных

@Repository
interface ItemRepository : JpaRepository<Task, Long> {
    override fun findById(id: Long): Optional<Task>
}
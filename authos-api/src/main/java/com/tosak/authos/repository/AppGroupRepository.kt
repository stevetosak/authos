package com.tosak.authos.repository

import com.tosak.authos.entity.AppGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AppGroupRepository : JpaRepository<AppGroup, Int> {
    fun findByUserId(userId: Int): List<AppGroup>?
    fun findByName(name: String) : AppGroup?
    fun findAppGroupByUserIdAndIsDefault(userId: Int,isDefault: Boolean): AppGroup?
    fun findByIdAndUserId(id: Int, userId: Int): AppGroup?
    fun findGroupById(id: Int): AppGroup?
}
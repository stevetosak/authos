package com.tosak.authos.repository

import com.tosak.authos.entity.DusterApp
import com.tosak.authos.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface DusterAppRepository : JpaRepository<DusterApp, User> {
    fun findDusterAppByUser(user: User) : DusterApp?
    fun findByClientId(clientId: String) : DusterApp?
}
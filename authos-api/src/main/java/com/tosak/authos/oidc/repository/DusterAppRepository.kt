package com.tosak.authos.oidc.repository

import com.tosak.authos.oidc.entity.DusterApp
import com.tosak.authos.oidc.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface DusterAppRepository : JpaRepository<DusterApp, User> {
    fun findDusterAppByUser(user: User) : DusterApp?
    fun findByClientId(clientId: String) : DusterApp?
}
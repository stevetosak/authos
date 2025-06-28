package com.authos.repository

import com.authos.model.DusterApp

interface DusterAppRepository {
    suspend fun getDusterAppByClientId(clientId: String) : DusterApp
    suspend fun getAllDusterApps(): List<DusterApp>
    suspend fun save(dusterApp: DusterApp) : DusterApp
    fun delete(dusterApp: DusterApp)
    fun update(dusterApp: DusterApp)
    fun updateStatus(clientId: String, active: Boolean)
    suspend fun getDusterAppByName(name: String) : DusterApp
}
package com.authos.repository

import com.authos.model.DusterApp

interface DusterAppRepository {
    suspend fun getDusterApp(clientId: String) : DusterApp
    fun getAllDusterApps(): List<DusterApp>
    suspend fun save(dusterApp: DusterApp) : DusterApp
    fun delete(dusterApp: DusterApp)
    fun update(dusterApp: DusterApp)
    fun updateStatus(clientId: String, active: Boolean)
}
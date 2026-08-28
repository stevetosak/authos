package com.authos.model

import kotlinx.serialization.Serializable

@Serializable
data class DusterSession(
    val sessionId: String,
    val clientId: String,
    val sub: String,
    val userInfo: Map<String, String>,
    val createdAt: Long = System.currentTimeMillis(),
)
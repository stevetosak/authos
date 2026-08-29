package com.authos.model

import kotlinx.serialization.Serializable

@Serializable
data class DusterSession(
    val sessionId: String,
    val clientId: String,
    val sub: String,
    val userInfo: Map<String, String>,
    val createdAt: Long = System.currentTimeMillis(),
    // Synchronizer token echoed by `/me` (`X-Duster-Csrf`) and required back on `POST /logout`
    // for tier-1 apps, where `SameSite=None` removes the ambient CSRF protection. (design #27)
    val csrfToken: String = "",
)
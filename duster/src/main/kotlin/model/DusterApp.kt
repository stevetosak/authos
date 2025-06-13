package com.authos.model

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import kotlin.time.Instant

@Serializable
data class DusterApp (
    val clientId: String = "",
    val clientSecret: String = "",
    val redirectUri: String = "http://localhost:8785/duster/api/v1/oauth/start", // ova trevit da e nekoj uri na duster, defaultno e
    val scope : String = "openid",
    val grantType: String = "authorization_code",
    val isActive: Boolean = true,
    var accessToken: String? = null,
    var refreshToken: String? = null,
    val callbackUri: String,
    val lastSyncAt: Long,
    val updatedAt: Long,
){
}
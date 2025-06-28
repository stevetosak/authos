package com.authos.model

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import kotlin.time.Instant

@Serializable
data class DusterApp (
    @JsonProperty("client_id")
    val clientId: String = "",
    @JsonProperty("client_secret")
    val clientSecret: String = "",
    val name: String = "",
    @JsonProperty("redirect_uri")
    val redirectUri: String = "http://localhost:8785/duster/api/v1/oauth/start", // ova trevit da e nekoj uri na duster, defaultno e
    val scope : String = "openid",
    @JsonProperty("grant_type")
    val grantType: String = "authorization_code",
    val isActive: Boolean = true,
    @JsonProperty("callback_uri")
    val callbackUri: String,
    val lastSyncAt: Long,
    val updatedAt: Long,
){
}
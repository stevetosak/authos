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
    val callbackUri: String = "",
    val lastSyncAt: Long = 0,
    val updatedAt: Long = 0,
    val successUrl: String = "/",
    val logoutRedirectUrl: String = "/",
    // Where `/callback` sends the browser when the OAuth exchange fails, instead of a 500.
    // Empty => derived from `successUrl` (its origin, or root, + `/error`). (design decision #28)
    @JsonProperty("error_url")
    val errorUrl: String = "",
    val webhookSecret: String = "",
    val sessionTtl: Long = 86400,
    // Non-empty => this is a tier-1 (cross-origin frontend) app: Duster enables credentialed CORS
    // for exactly these origins on the browser-facing endpoints and issues the session cookie
    // SameSite=None. Empty (the default) keeps the tight tier-0/2 posture. (design decision #27)
    @JsonProperty("allowed_origins")
    val allowedOrigins: List<String> = emptyList(),
)
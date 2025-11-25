package com.tosak.authos.oidc.common.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class AuthosAppSyncDto(
    @JsonProperty("client_id")
    val clientId: String = "",
    @JsonProperty("client_secret")
    val clientSecret: String = "",
    @JsonProperty("redirect_uri")
    val redirectUri: String = "",
    @JsonProperty("scope")
    val scope: String = "",
    @JsonProperty("grant_type")
    val grantType: String = "",
    @JsonProperty("callback_uri")
    val callbackUri: String = "",
    @JsonProperty("name") val name: String = "",
)

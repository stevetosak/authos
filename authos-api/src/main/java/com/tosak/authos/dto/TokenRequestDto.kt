package com.tosak.authos.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TokenRequestDto (
    val code: String?,
    @JsonProperty("redirect_uri")
    val redirectUri: String,
    @JsonProperty("grant_type")
    val grantType: String,
    @JsonProperty("client_id")
    val clientId: String,
    @JsonProperty("client_secret")
    val clientSecret: String,
    @JsonProperty("refresh_token")
    val refreshToken: String?

)
package com.tosak.authos.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TokenRequestDto (
    val code: String?,
    @JsonProperty("redirect_uri")
    val redirectUri: String,
    @JsonProperty("grant_type")
    val grantType: String,
    @JsonProperty("client_id")
    var clientId: String?,
    @JsonProperty("client_secret")
    var clientSecret: String?,
    @JsonProperty("refresh_token")
    val refreshToken: String?

)
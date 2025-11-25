package com.tosak.authos.oidc.common.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class UserLoginRequestDTO (
    val email: String,
    val password: String,
    @JsonProperty("client_id")
    val clientId: String?,
    @JsonProperty("redirect_uri")
    val redirectUri: String?,
    @JsonProperty("state")
    val state: String?
){
}

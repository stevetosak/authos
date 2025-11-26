package com.tosak.authos.oidc.common.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class UserInfoAccessTokenReqDTO(
    @JsonProperty("access_token")
    val accessToken: String
)

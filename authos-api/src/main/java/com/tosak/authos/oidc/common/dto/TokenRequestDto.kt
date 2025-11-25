package com.tosak.authos.oidc.common.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TokenRequestDto (
    val code: String?,
    val redirectUri: String?,
    val grantType: String,
    var clientId: String?,
    var clientSecret: String?,
    val refreshToken: String?

)
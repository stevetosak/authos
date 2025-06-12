package com.authos.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class AuthTokenResponse(
    val accessToken: String = "",
    val refreshToken: String = "",
    val tokenType: String = "",
    val idToken: String = "",
    val expiresIn: Int = 0
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class TokenRequestDto (
    val code: String = "",
    val redirectUri: String = "",
    val grantType: String = "",
    val clientId: String = "",
    val clientSecret: String = ""
){
}
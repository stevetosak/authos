package com.authos.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class AuthTokenResponse(
    val accessToken: String = "",
    val refreshToken: String? = null,
    val tokenType: String = "",
    val idToken: String? = null,
    val expiresIn: Int = 0
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class TokenRequestDto(
    var code: String? = null,
    val redirectUri: String = "",
    val grantType: String = "",
    val clientId: String = "",
    val clientSecret: String = "",
    var refreshToken: String? = null,
) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class DusterAppRegisterDto(
    @JsonProperty("client_id") val clientId: String,
    @JsonProperty("client_secret") val clientSecret: String,
    @JsonProperty("redirect_uri") val redirectUri: String,
    @JsonProperty("grant_type") val grantType: String = "authorization_code",
    @JsonProperty("scope") val scope: String = "openid",
    @JsonProperty("callback_uri") val callbackUri: String,
    @JsonProperty("name") val name: String,
)
//@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
//@JsonIgnoreProperties(ignoreUnknown = true)
//data class DusterAppRegisterDto(
//    val clientId: String = "",
//    val clientSecret: String = "",
//    val redirectUri: String = "",
//    val grantType: String = "",
//    val scope : String = "openid"
//
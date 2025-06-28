package com.tosak.authos.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.nimbusds.jose.shaded.gson.annotations.SerializedName
import org.springframework.stereotype.Service


@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class TokenResponse (
    val accessToken: String,
    val refreshToken : String? = null,
    val tokenType: String,
    val idToken : String?,
    val expiresIn : Int,
)

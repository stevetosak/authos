package com.tosak.authos.dto

import com.nimbusds.jose.shaded.gson.annotations.SerializedName
import org.springframework.stereotype.Service


data class TokenResponse (
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken : String = "",
    @SerializedName("token_type")
    val tokenType: String,
    @SerializedName("id_token")
    val idToken : String,
    @SerializedName("expires_in")
    val expiresIn : Int,
)

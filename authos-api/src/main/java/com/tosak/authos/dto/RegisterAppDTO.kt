package com.tosak.authos.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class RegisterAppDTO (
    val appName: String,
    val appIconUrl: String,
    val shortDescription: String,
    val tokenEndpointAuthMethod: String,
    val grantTypes : List<String>,
    val responseTypes : List<String>,
    val appInfoUri : String,
    val redirectUris : List<String>,
    val scopes : List<String>,
)

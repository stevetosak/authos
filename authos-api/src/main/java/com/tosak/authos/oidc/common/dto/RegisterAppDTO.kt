package com.tosak.authos.oidc.common.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class RegisterAppDTO (
    val appName: String,
    val appIconUrl: String? = null,
    val shortDescription: String,
    val tokenEndpointAuthMethod: String,
    val grantTypes : List<String>,
    val responseTypes : List<String>,
    val appInfoUri : String? = null,
    val redirectUris : List<String>,
    val scope : List<String>,
    val group: Int?
)

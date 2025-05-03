package com.tosak.authos.dto
class RegisterAppDTO (
    val appName: String,
    val appIconUrl: String,
    val shortDescription: String,
    val tokenEndpointAuthMethod: String,
    val grantType : String,
    val responseTypes : Array<String>,
    val appInfoUri : String,
    val redirectUris : Array<String>,
    val scopes : Array<String>,
)

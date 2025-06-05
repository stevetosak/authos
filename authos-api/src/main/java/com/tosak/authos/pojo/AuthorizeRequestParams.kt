package com.tosak.authos.pojo

data class AuthorizeRequestParams(
    val clientId:String,
    val redirectUri:String,
    val state:String,
    val scope:String,
    val idTokenHint:String?,
    val responseType:String
)

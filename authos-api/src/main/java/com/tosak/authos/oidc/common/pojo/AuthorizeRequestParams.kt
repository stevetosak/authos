package com.tosak.authos.oidc.common.pojo

data class AuthorizeRequestParams(
    val clientId:String,
    val redirectUri:String,
    val state:String,
    val scope:String,
    val idTokenHint:String?,
    val responseType:String,
    val dusterSub:String?,
    val nonce:String?,
)

package com.tosak.authos.oidc.common.dto

import java.net.URI

data class LoginDTO (
    val user: UserDTO,
    val apps: List<AppDTO>,
    val groups: List<AppGroupDTO>,
    val redirectUri: URI? = null,
    val signature: String? = null
)
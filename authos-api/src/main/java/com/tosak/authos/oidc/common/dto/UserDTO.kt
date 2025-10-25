package com.tosak.authos.oidc.common.dto

data class UserDTO(
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String? = "",
){}

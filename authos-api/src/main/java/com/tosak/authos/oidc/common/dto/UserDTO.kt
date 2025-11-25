package com.tosak.authos.oidc.common.dto

import java.time.LocalDateTime

data class UserDTO(
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String? = "",
    val lastLoginAt: LocalDateTime? = null,
    val emailVerified: Boolean = false,
    val mfaEnabled: Boolean = false,
)

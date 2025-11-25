package com.tosak.authos.oidc.common.dto

import java.time.LocalDateTime

data class AppGroupDTO (
    val id: Int?,
    val name: String,
    val isDefault: Boolean,
    val createdAt: LocalDateTime,
    val ssoPolicy: String = "Partial",
    val mfaPolicy: String = "Disabled",
)
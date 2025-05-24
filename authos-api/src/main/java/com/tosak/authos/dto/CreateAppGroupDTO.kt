package com.tosak.authos.dto

import java.time.LocalDateTime

data class CreateAppGroupDTO (
    val name: String,
    val isDefault: Boolean,
    val ssoPolicy: String = "Partial",
    val mfaPolicy: String = "Disabled",
)
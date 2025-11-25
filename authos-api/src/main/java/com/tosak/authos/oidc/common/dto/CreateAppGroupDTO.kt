package com.tosak.authos.oidc.common.dto

data class CreateAppGroupDTO (
    val name: String,
    val isDefault: Boolean,
    val ssoPolicy: String = "Partial",
    val mfaPolicy: String = "Disabled",
)
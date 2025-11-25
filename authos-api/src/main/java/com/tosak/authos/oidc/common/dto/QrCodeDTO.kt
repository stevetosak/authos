package com.tosak.authos.oidc.common.dto

data class QrCodeDTO(
    val qrData: String,
    val secret: String
)

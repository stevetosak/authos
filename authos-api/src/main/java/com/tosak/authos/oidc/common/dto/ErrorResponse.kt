package com.tosak.authos.oidc.common.dto

data class ErrorResponse(val error: String, val errorDescription: String = "", val errorUri: String? = null) {
}
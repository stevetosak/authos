package com.tosak.authos.oidc.common.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * OAuth 2.0 error response (RFC 6749 §5.2): `{"error": "...", "error_description": "...", "error_uri": "..."}`.
 * SnakeCase naming + NON_EMPTY so the optional members are omitted when absent.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class ErrorResponse(val error: String, val errorDescription: String = "", val errorUri: String? = null)

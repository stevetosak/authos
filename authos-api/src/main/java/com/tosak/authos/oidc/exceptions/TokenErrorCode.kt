package com.tosak.authos.oidc.exceptions

enum class TokenErrorCode(val code: String) : OidcErrorCode{
    INVALID_REQUEST("invalid_request"),
    INVALID_CLIENT("invalid_client"),
    INVALID_GRANT("invalid_grant"),
    UNAUTHORIZED_CLIENT("unauthorized_client"),
    UNSUPPORTED_GRANT_TYPE("unsupported_grant_type"),
    INVALID_SCOPE("invalid_scope");


    override fun code(): String = code

    companion object {
        fun fromCode(code: String?): TokenErrorCode? =
            TokenErrorCode.entries.find { it.code == code }
    }
}

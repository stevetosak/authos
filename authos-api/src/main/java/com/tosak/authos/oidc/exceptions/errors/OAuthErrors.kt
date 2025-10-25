package com.tosak.authos.oidc.exceptions.errors



enum class OAuthErrors(val code: String, val message: String) {
    INVALID_REQUEST("invalid_request", "The request is missing a required parameter, includes an invalid parameter value, includes a parameter more than once, or is otherwise malformed."),
    UNAUTHORIZED_CLIENT("unauthorized_client", "The client is not authorized to request an authorization code using this method."),
    ACCESS_DENIED("access_denied", "The resource owner or authorization server denied the request."),
    UNSUPPORTED_RESPONSE_TYPE("unsupported_response_type", "The authorization server does not support obtaining an authorization code using this method."),
    INVALID_SCOPE("invalid_scope", "The requested scope is invalid, unknown, or malformed."),
    SERVER_ERROR("server_error", "The authorization server encountered an unexpected condition that prevented it from fulfilling the request. (This error code is needed because a 500 Internal Server Error HTTP status code cannot be returned to the client via an HTTP redirect.)"),
    TEMPORARILY_UNAVAILABLE("temporarily_unavailable", "The authorization server is currently unable to handle the request due to a temporary overloading or maintenance of the server. (This error code is needed because a 503 Service Unavailable HTTP status code cannot be returned to the client via an HTTP redirect.)");

    companion object {
        fun fromCode(code: String): OAuthErrors? = entries.find { it.code == code }
    }
}

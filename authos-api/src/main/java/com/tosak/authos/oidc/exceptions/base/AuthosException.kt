package com.tosak.authos.oidc.exceptions.base


open class AuthosException (
    override val message: String,
    val description: String = "",
    override val cause: Throwable? = HttpBadRequestException(),
    val redirect: Boolean = false,
) : RuntimeException(message,cause) {
}
package com.tosak.authos.oidc.exceptions.base


open class AuthosException (
    override val message: String,
    override val cause: Throwable,
    val redirectUrl: String? = null) : RuntimeException(message,cause) {
}
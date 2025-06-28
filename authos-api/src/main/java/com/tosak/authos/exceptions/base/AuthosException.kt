package com.tosak.authos.exceptions.base


open class AuthosException (
    override val message: String,
    override val cause: Throwable,
    val redirectUrl: String? = null) : Exception(message) {
}
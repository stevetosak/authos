package com.tosak.authos.oidc.common.enums

import com.tosak.authos.oidc.exceptions.badreq.PromptParseException
import com.tosak.authos.oidc.exceptions.base.AuthosException

enum class PromptType {
    NONE,
    LOGIN,
    CONSENT,
    SELECT_ACCOUNT;

    companion object{
        fun parse(prompt : String): PromptType {
            return entries.find { it.name.equals(prompt,ignoreCase = true) } ?: throw AuthosException("invalid request",PromptParseException())

        }
    }
    override fun toString(): String {
        return name.lowercase()
    }

}
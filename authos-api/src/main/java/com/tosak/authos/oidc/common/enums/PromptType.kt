package com.tosak.authos.oidc.common.enums

import com.tosak.authos.oidc.exceptions.badreq.PromptParseException
import com.tosak.authos.oidc.exceptions.base.AuthosException

enum class PromptType {
    NONE,
    LOGIN,
    CONSENT,
    OMITTED,
    SELECT_ACCOUNT;

    companion object{
        fun parse(prompt : String): PromptType? {
            return entries.find { it.name.equals(prompt,ignoreCase = true) } ?: OMITTED
        }
    }
    override fun toString(): String {
        return name.lowercase()
    }

}
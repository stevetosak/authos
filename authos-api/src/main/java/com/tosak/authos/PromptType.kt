package com.tosak.authos

import com.tosak.authos.exceptions.badreq.PromptParseException

enum class PromptType {
    NONE,
    LOGIN,
    CONSENT,
    SELECT_ACCOUNT;

    companion object{
        fun parse(prompt : String): PromptType {
            return entries.find { it.name.equals(prompt,ignoreCase = true) } ?: throw PromptParseException(
                "Can't parse prompt: $prompt"
            )
        }
    }
    override fun toString(): String {
        return name.lowercase()
    }

}
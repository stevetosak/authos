package com.authos.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

class ClientConfig private constructor(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val scope: String = "openid",
    val grantType: String = "authorization_code",
) {

    class Builder {
        private var clientId: String = ""
        private var clientSecret: String = ""
        private var redirectUri: String = ""
        private var scope: String = ""
        private var grantType: String = ""

        fun clientId(value: String) = apply {
            require(value.isNotBlank())
            clientId = value
        }

        fun clientSecret(value: String) = apply {
            require(value.isNotBlank())
            clientSecret = value
        }

        fun redirectUri(value: String) = apply {
            require(value.isNotBlank())
            redirectUri = value
        }

        fun scope(value: String) = apply {
            if (value.isNotBlank()) {
                scope = value
            }
        }

        fun grantType(value: String) = apply {
            if (value.isNotBlank()) {
                grantType = value
            }
        }

        fun build(): ClientConfig {
            return ClientConfig(
                clientId = clientId,
                clientSecret = clientSecret,
                redirectUri = redirectUri,
                scope = scope,
                grantType = grantType
            )
        }
    }

    companion object {
        fun builder() = Builder()
    }
}

//fun loadDusterConfig(): ClientConfig {
//
//}


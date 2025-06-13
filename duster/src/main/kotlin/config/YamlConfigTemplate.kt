package com.authos.config

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

data class ConfigFile(
    val config: Config
)

data class Config(
    val credentials: Credentials,
    val server: Server,
    val callback: Callback
)

data class Credentials(
    @JsonProperty("client_id")
    val clientId: String,
    @JsonProperty("client_secret")
    val clientSecret: String,
    @JsonProperty("redirect_uri")
    val redirectUri: String,
    @JsonProperty("scope")
    val scope: String,
    @JsonProperty("grant_type")
    val grantType: String
)

data class Server(
    val internal: PortConfig,
    val external: PortConfig
)

data class PortConfig(
    val port: Int
)
data class Callback(
    val url: String
)


inline fun <reified T> loadYaml(file: File): T {
    val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    return mapper.readValue(file, T::class.java)
}

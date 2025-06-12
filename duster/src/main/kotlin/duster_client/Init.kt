package com.authos.duster_client

import com.authos.config.ClientConfig
import com.authos.config.ConfigFile
import com.authos.config.loadYaml
import java.io.File

fun buildConfig() : ClientConfig {
    val yaml = File("src/main/resources/client-config.yaml");
    val clientConf = loadYaml<ConfigFile>(yaml)
    return ClientConfig
        .builder()
        .clientId(clientConf.config.credentials.clientId)
        .clientSecret(clientConf.config.credentials.clientSecret)
        .redirectUri(clientConf.config.credentials.redirectUri)
        .grantType(clientConf.config.credentials.grantType)
        .scope(clientConf.config.credentials.scope)
        .build()
}
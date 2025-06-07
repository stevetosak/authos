package com.tosak.authos.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.tosak.authos.entity.AppGroup
import java.io.Serializable
import java.time.LocalDateTime

/**
 * DTO for {@link com.tosak.authos.entity.App}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AppDTO(
    @JsonProperty("id")
    var id: Int? = null,
    var name: String = "",
    var redirectUris: List<String?> = listOf(),
    var clientId: String = "",
    var clientSecret: String = "",
    var clientSecretExpiresAt: LocalDateTime? = null,
    var shortDescription: String? = null,
    var createdAt: LocalDateTime = LocalDateTime.now(),
    var group: Int,
    var logoUri: String? = "",
    var appUrl: String? = "",
    var scopes: List<String> = listOf(),
    var responseTypes: List<String> = listOf(),
    var grantTypes: List<String> = listOf(),
    var tokenEndpointAuthMethod: String,
    ) :
    Serializable
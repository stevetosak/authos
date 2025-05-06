package com.tosak.authos.dto

import com.tosak.authos.entity.AppGroup
import java.io.Serializable
import java.time.LocalDateTime

/**
 * DTO for {@link com.tosak.authos.entity.App}
 */
data class AppDTO(
    val id: Int? = null,
    val name: String = "",
    val redirectUris: List<String?> = listOf(),
    val clientId: String = "",
    val clientSecret: String = "",
    val shortDescription: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val group: AppGroup,
    val logoUri: String? = "",
    val scopes: List<String> = listOf(),
    val responseTypes: List<String> = listOf(),
    val grantTypes: List<String> = listOf(),
    val tokenEndpointAuthMethod: String?,
    ) :
    Serializable
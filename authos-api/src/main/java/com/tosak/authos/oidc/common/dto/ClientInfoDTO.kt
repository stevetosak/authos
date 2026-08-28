package com.tosak.authos.oidc.common.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Public, non-sensitive client metadata shown on the consent screen. Deliberately excludes
 * client_secret, redirect_uris and internal identifiers - an unauthenticated end-user viewing
 * the consent page must be able to resolve it purely from client_id.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ClientInfoDTO(
    val name: String,
    val logoUri: String?,
    val shortDescription: String?,
)

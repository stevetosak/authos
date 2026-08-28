package com.tosak.authos.oidc.common.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * OpenID Provider metadata for `GET /.well-known/openid-configuration`
 * (OpenID Connect Discovery 1.0 / RFC 8414).
 *
 * The list fields default to what Authos actually supports today; the URL fields are
 * assembled per-request from `authos.api.host`. Keep this in sync with the real
 * implementation — a discovery doc that over-promises is worse than none.
 *
 * Notable omissions, each gated on a roadmap phase:
 *  - `grant_types_supported` lists only `authorization_code` + `refresh_token`.
 *    `client_credentials` works but is Duster-only (validates against `duster_app`),
 *    so it is not advertised to generic RPs.
 *  - no `revocation_endpoint` (Phase 1), `end_session_endpoint` (Phase 2),
 *    `introspection_endpoint` (Phase 3), or `registration_endpoint`.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class OpenIdProviderMetadata(
    val issuer: String,
    val authorizationEndpoint: String,
    val tokenEndpoint: String,
    val userinfoEndpoint: String,
    val jwksUri: String,
    val scopesSupported: List<String> = listOf("openid", "profile", "email", "offline_access"),
    val responseTypesSupported: List<String> = listOf("code"),
    val responseModesSupported: List<String> = listOf("query"),
    val grantTypesSupported: List<String> = listOf("authorization_code", "refresh_token"),
    val subjectTypesSupported: List<String> = listOf("pairwise"),
    val idTokenSigningAlgValuesSupported: List<String> = listOf("RS256"),
    val tokenEndpointAuthMethodsSupported: List<String> = listOf("client_secret_basic", "client_secret_post"),
    val codeChallengeMethodsSupported: List<String> = listOf("S256"),
    // Only claims ClaimService can actually resolve from the User entity.
    val claimsSupported: List<String> = listOf(
        "sub", "iss", "aud", "exp", "iat", "auth_time", "nonce",
        "email", "email_verified",
        "name", "given_name", "family_name", "middle_name", "picture", "gender",
    ),
    val claimsParameterSupported: Boolean = false,
    val requestParameterSupported: Boolean = false,
    val requestUriParameterSupported: Boolean = false,
)

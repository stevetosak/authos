package com.authos.service

import com.authos.crypto.computeHmac
import com.authos.data.AuthTokenResponse
import com.authos.getAuthosBaseUrl
import com.authos.getHostIp
import com.authos.model.DusterApp
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.jackson.jackson
import io.ktor.client.call.body

enum class NextAuthorizeRequestType {
    AUTO,
    OFFLINE_ACCESS
}

private val objectMapper = ObjectMapper()

val client = HttpClient(CIO) {
    expectSuccess = true
    install(ContentNegotiation) {
        jackson()
    }
}

class DusterOAuthClient(val dusterApp: DusterApp) {

    var nextRequestType = NextAuthorizeRequestType.AUTO

    @Throws(JsonProcessingException::class)
    suspend fun codeExchange(code: String, codeVerifier: String): AuthTokenResponse {
        return client.post(getAuthosTokenUrl()) {
            setBody(FormDataContent(Parameters.build {
                append("code", code)
                append("redirect_uri", dusterApp.redirectUri)
                append("grant_type", dusterApp.grantType)
                append("client_id", dusterApp.clientId)
                append("client_secret", dusterApp.clientSecret)
                append("code_verifier", codeVerifier)
            }))
        }.body()
    }

    suspend fun fetchUserInfo(accessToken: String): HttpResponse {
        val resp = client.get(getAuthosUserinfoUrl()) {
            headers.append("Authorization", "Bearer $accessToken")
        }
        require(resp.status.isSuccess()) { "Failed to fetch user information: ${resp.status}" }
        return resp
    }

    suspend fun refreshTokenRequest(refreshToken: String): HttpResponse {
        val resp = client.post(getAuthosTokenUrl()) {
            setBody(FormDataContent(Parameters.build {
                append("client_id", dusterApp.clientId)
                append("client_secret", dusterApp.clientSecret)
                append("grant_type", "refresh_token")
                append("refresh_token", refreshToken)
                append("redirect_uri", dusterApp.redirectUri)
            }))
        }
        require(resp.status.isSuccess()) { "Failed to refresh token" }
        return resp
    }

    /**
     * RFC 7009 revocation against Authos. Client-authenticated with the app's own credentials.
     * Revoking the refresh token cascades to the access tokens issued under the same grant.
     * Best-effort: the caller (logout) must still complete if this throws.
     */
    suspend fun revokeRefreshToken(refreshToken: String): HttpResponse {
        return client.post(getAuthosRevokeUrl()) {
            setBody(FormDataContent(Parameters.build {
                append("client_id", dusterApp.clientId)
                append("client_secret", dusterApp.clientSecret)
                append("token", refreshToken)
                append("token_type_hint", "refresh_token")
            }))
        }
    }

    suspend fun sendToCallback(prunedData: HashMap<String, String>): HttpResponse {
        var callbackUrl = dusterApp.callbackUri
        if (getHostIp() != "localhost" && callbackUrl.contains("localhost")) {
            callbackUrl = callbackUrl.replace("localhost", getHostIp())
        }
        val jsonBody = objectMapper.writeValueAsString(prunedData)
        return try {
            client.post(callbackUrl) {
                contentType(ContentType.Application.Json)
                setBody(jsonBody)
                if (dusterApp.webhookSecret.isNotBlank()) {
                    headers.append("X-Duster-Signature", "sha256=${computeHmac(dusterApp.webhookSecret, jsonBody)}")
                }
            }
        } catch (exception: ResponseException) {
            exception.response
        }
    }
}

fun getAuthosTokenUrl() = "${getAuthosBaseUrl()}/oauth/token"

fun getAuthosUserinfoUrl() = "${getAuthosBaseUrl()}/oauth/userinfo"

fun getAuthosRevokeUrl() = "${getAuthosBaseUrl()}/oauth/revoke"

suspend fun sendClientCredentialsTokenRequest(clientId: String, clientSecret: String): String {
    val resp: AuthTokenResponse = client.post(getAuthosTokenUrl()) {
        setBody(FormDataContent(Parameters.build {
            append("client_id", clientId)
            append("client_secret", clientSecret)
            append("grant_type", "client_credentials")
        }))
    }.body()
    return resp.accessToken
}
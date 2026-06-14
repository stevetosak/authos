package com.authos.service

import com.authos.data.AuthTokenResponse
import com.authos.getHostIp
import com.authos.model.DusterApp
import com.fasterxml.jackson.core.JsonProcessingException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.jackson.jackson

enum class NextAuthorizeRequestType {
    AUTO,
    OFFLINE_ACCESS
}

val client = HttpClient(CIO) {
    expectSuccess = true
    install(ContentNegotiation) {
        jackson()
    }
}

class DusterOAuthClient(val dusterApp: DusterApp) {

    var nextRequestType = NextAuthorizeRequestType.AUTO


    @Throws(JsonProcessingException::class)
    suspend fun codeExchange(code: String): AuthTokenResponse {
        val resp: AuthTokenResponse = client.post(getAuthosTokenUrl()) {
            setBody(FormDataContent(Parameters.build {
                append("code", code)
                append("redirect_uri", dusterApp.redirectUri)
                append("grant_type", dusterApp.grantType)
                append("client_id", dusterApp.clientId)
                append("client_secret", dusterApp.clientSecret)
            }))
        }.body()

        println("Response body: $resp")

        return resp;
    }

    suspend fun fetchUserInfo(accessToken: String): HttpResponse {
        val resp = client.get(getAuthosUserinfoUrl()) {
            headers.append("Authorization", "Bearer $accessToken")
        }

        require(resp.status.isSuccess()) { "Failed to fetch user information: ${resp.status}" }

        return resp;
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

    suspend fun sendToCallback(prunedData: HashMap<String, String>): HttpResponse {

        println("Callback url: ${dusterApp.callbackUri}")
        var callbackUrl = dusterApp.callbackUri
        if (getHostIp() != "localhost" && callbackUrl.contains("localhost")) {
            callbackUrl = callbackUrl.replace("localhost", getHostIp())
        };
        val resp: HttpResponse = try {
            client.post(callbackUrl) {
                contentType(ContentType.Application.Json)
                setBody(prunedData)
            }
        } catch (exception: ResponseException) {
            exception.response
        }
        return resp;

    }


}

fun getAuthosTokenUrl(): String {
    val hostIP = getHostIp()
    return "http://$hostIP:8080/oauth/token"
}

fun getAuthosUserinfoUrl(): String {
    val hostIP = getHostIp();
    return "http://$hostIP:8080/oauth/userinfo"
}


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

package com.authos.service

import com.authos.data.AuthTokenResponse
import com.authos.data.TokenRequestDto
import com.authos.getHostIp
import com.authos.model.DusterApp
import com.fasterxml.jackson.core.JsonProcessingException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
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
        val tokenRequest = TokenRequestDto(
            code = code,
            redirectUri = dusterApp.redirectUri,
            grantType = dusterApp.grantType,
            clientId = dusterApp.clientId,
            clientSecret = dusterApp.clientSecret
        )
        val resp: AuthTokenResponse = client.post(getAuthosTokenUrl()) {
            contentType(ContentType.Application.Json)
            setBody(tokenRequest)
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
        val tokenRequest = TokenRequestDto(
            clientId = dusterApp.clientId,
            clientSecret = dusterApp.clientSecret,
            grantType = "refresh_token",
            refreshToken = refreshToken,
            redirectUri = dusterApp.redirectUri
        )

        val resp = client.post(getAuthosTokenUrl()) {
            contentType(ContentType.Application.Json)
            setBody(tokenRequest)
        }
        require(resp.status.isSuccess()) { "Failed to refresh token" }
        return resp;
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
    return "http://$hostIP:9000/oauth/token"
}

fun getAuthosUserinfoUrl(): String {
    val hostIP = getHostIp();
    return "http://$hostIP:9000/oauth/userinfo"
}


suspend fun sendClientCredentialsTokenRequest(clientId: String, clientSecret: String): String {
    val body = mapOf("client_id" to clientId, "client_secret" to clientSecret, "grant_type" to "client_credentials")
    val resp: AuthTokenResponse = client.post(getAuthosTokenUrl()) {
        setBody(body)
        contentType(ContentType.Application.Json)
    }.body()
    return resp.accessToken
}

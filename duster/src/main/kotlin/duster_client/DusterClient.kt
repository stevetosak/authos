package com.authos.duster_client

import com.authos.data.AuthTokenResponse
import com.authos.data.TokenRequestDto
import com.authos.model.DusterApp
import com.authos.model.UserInfo
import com.fasterxml.jackson.core.JsonProcessingException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.jackson.jackson

enum class NextAuthorizeRequestType{
    AUTO,
    OFFLINE_ACCESS
}

class DusterClient(val dusterApp: DusterApp) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson()
        }
    }

    var nextRequestType = NextAuthorizeRequestType.AUTO


    private val AUTHOS_AUTHORIZE_URL = "http://localhost:9000/oauth/authorize"
    private val AUTHOS_TOKEN_URL = "http://localhost:9000/oauth/token"
    private val AUTHOS_USERINFO_URL = "http://localhost:9000/oauth/userinfo"


    @Throws(JsonProcessingException::class)
    suspend fun codeExchange(code: String): AuthTokenResponse {
        val tokenRequest = TokenRequestDto(
            code = code,
            redirectUri = dusterApp.redirectUri,
            grantType = dusterApp.grantType,
            clientId = dusterApp.clientId,
            clientSecret = dusterApp.clientSecret
        )
        val resp: AuthTokenResponse = client.post(AUTHOS_TOKEN_URL) {
            contentType(ContentType.Application.Json)
            setBody(tokenRequest)
        }.body()

        println("Response body: $resp")

        return resp;
    }

    suspend fun fetchUserInfo(accessToken: String): HttpResponse {
        val resp = client.get(AUTHOS_USERINFO_URL) {
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

        val resp = client.post(AUTHOS_TOKEN_URL) {
            contentType(ContentType.Application.Json)
            setBody(tokenRequest)
        }
        require(resp.status.isSuccess()) { "Failed to refresh token" }
        return resp;
    }

    suspend fun sendToCallback(prunedData: HashMap<String,String>): HttpResponse {

        return client.post(dusterApp.callbackUri) {
            contentType(ContentType.Application.Json)
            setBody(prunedData)
        }
    }

}
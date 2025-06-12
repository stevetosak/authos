package com.authos.duster_client

import com.authos.config.ClientConfig
import com.authos.data.AuthTokenResponse
import com.authos.data.TokenRequestDto
import com.authos.model.UserInfo
import com.fasterxml.jackson.core.JsonProcessingException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.jackson.jackson

class DusterClient (val config: ClientConfig) {
    private val client = HttpClient(CIO){
        install(ContentNegotiation) {
            jackson()
        }
    }

    private val AUTHOS_AUTHORIZE_URL = "http://localhost:9000/oauth/authorize"
    private val AUTHOS_TOKEN_URL = "http://localhost:9000/oauth/token"
    private val AUTHOS_USERINFO_URL = "http://localhost:9000/oauth/userinfo"

    @Throws(JsonProcessingException::class)
    suspend fun codeExchange(code: String) : AuthTokenResponse {
        val tokenRequest = TokenRequestDto(
            code = code,
            redirectUri = config.redirectUri,
            grantType = config.grantType,
            clientId = config.clientId,
            clientSecret = config.clientSecret
        )
        val resp: AuthTokenResponse = client.post(AUTHOS_TOKEN_URL) {
            contentType(ContentType.Application.Json)
            setBody(tokenRequest)
        }.body()

        println("Response body: $resp")

        return resp;
    }

    suspend fun fetchUserInfo(tokenResponse: AuthTokenResponse) : UserInfo {
        val resp: UserInfo = client.get(AUTHOS_USERINFO_URL) {
            headers.append("Authorization", "Bearer ${tokenResponse.accessToken}")
        }.body()

        return resp;
    }

}
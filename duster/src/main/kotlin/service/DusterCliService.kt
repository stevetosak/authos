package com.authos.service

import com.authos.model.DusterCredentials
import com.authos.repository.CredentialsRepository
import io.ktor.client.request.get

class DusterCliService (private val credentialsRepository: CredentialsRepository){

    suspend fun getAccessToken() : String{
        println("Getting access token...")
        val result = credentialsRepository.getCredentials();
        val credentials = DusterCredentials.fromRedisMap(result)
        var token = credentials.token
        try{
            println("Checking token validity...")
            client.get("http://localhost:9000/duster/validate-token") {
                headers.append("Authorization", "Bearer $token")
            }
            println("Success! Token is valid")
        } catch (e: Exception) {
            println("Token is invalid")
            println("Fetching new access token...")
            token = sendClientCredentialsTokenRequest(clientId = credentials.clientId, clientSecret = credentials.clientSecret)
            credentialsRepository.saveToken(token)
            println("Fetched token!")
        }
        return token!!

    }
}
package com.tosak.authos.duster.commands

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.option
import com.tosak.authos.duster.DusterAppDto
import com.tosak.authos.duster.client
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder

class Sync() : SuspendingCliktCommand(name = "sync") {

    private val lookup by requireObject<AppLookup>()

    suspend fun sendPullRequest(param: String, value: String) {
        val requestUrl = URLBuilder("http://localhost:9000/duster/pull").apply {
            parameters.append(param, value)
        }.buildString()
        println("Request URL: $requestUrl")

        val token = client.get("http://localhost:8785/duster/api/v1/internal/credentials/token").body<String>()

        val dusterAppDto: DusterAppDto = client.post(requestUrl) {
            header("Authorization", "Bearer $token")
        }.body()

        val saveReq = client.post("http://localhost:8785/duster/api/v1/internal/apps/create") {
            setBody(dusterAppDto)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
        if(saveReq.status != HttpStatusCode.OK) {
            println("Could not save Duster App ")
            println(saveReq.body<String>())
        } else {
            println("Duster App Synced")
        }
    }


    override suspend fun run() {
        if((!lookup.clientId.isNullOrBlank() && !lookup.name.isNullOrBlank()) || (lookup.clientId.isNullOrBlank() && lookup.name.isNullOrBlank())){
            throw IllegalArgumentException("Either clientid or name must be present.")
        }
        if(!lookup.clientId.isNullOrBlank()){
            sendPullRequest("client_id",lookup.clientId!!)
        } else {
            sendPullRequest("client_name",lookup.name!!)
        }
    }

}

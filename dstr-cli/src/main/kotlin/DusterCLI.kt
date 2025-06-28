package com.tosak.authos.duster

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache5.Apache5
import io.ktor.client.engine.cio.*
import io.ktor.client.engine.jetty.jakarta.Jetty
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*


val client = HttpClient(Apache5) {
    install(ContentNegotiation) {
        jackson()
    }
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.INFO
    }
    expectSuccess = true
}

class DusterCli : SuspendingCliktCommand(name = "dstr") {
    override suspend fun run() = println("see --help")

}

class PullApp() : SuspendingCliktCommand(name = "pull") {
    val clientId by option(
        "-cid", "--clientid",
        help = "Client Identifier of the application."
    )
    val name by option(
        "-n", "--name",
        help = "Name of the application"
    )


    override suspend fun run() {
        if((!clientId.isNullOrBlank() && !name.isNullOrBlank()) || (clientId.isNullOrBlank() && name.isNullOrBlank())){
            throw IllegalArgumentException("Either clientid or name must be present.")
        }
        if(!clientId.isNullOrBlank()){
            sendPullRequest("client_id",clientId!!)
        } else {
            sendPullRequest("client_name",name!!)
        }
    }

}

suspend fun sendPullRequest(param: String, value: String) {
    val requestUrl = URLBuilder("http://localhost:9000/duster/pull").apply {
        parameters.append(param, value)
    }.buildString()
    println("Request URL: $requestUrl")

    val dusterAppDto: DusterAppDto = client.post(requestUrl) {
        header("Authorization", "Bearer SfRmBcmG533XhcnTnNonuH-7-z-8yF_ULgsUvh7FSFcd")
    }.body()

    val saveReq = client.post("http://localhost:8785/duster/api/internal/apps/create") {
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

class ViewApp() : SuspendingCliktCommand(name = "apps") {

    val clientId by option(
        "-cid", "--clientid",
        help = "Client Identifier of the application."
    )
    val name by option(
        "-n", "--name",
        help = "Name of the application"
    )
    override suspend fun run() {
        if((!clientId.isNullOrBlank() && !name.isNullOrBlank())){
            throw IllegalArgumentException("Either clientid or name must be present.")
        }
        val resp = client.get("http://localhost:8785/duster/api/internal/apps") {
            parameter("client_id", clientId )
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
        if(clientId.isNullOrBlank() && name.isNullOrBlank()){
            println("======== All Apps =========")
            val apps = resp.body<List<DusterAppDto>>()
            apps.forEachIndexed { i,app ->
                println("#${i+1}")
                println(app.toString())
                println("=====================")
            }
        }else {
            val dusterAppDto = resp.body<DusterAppDto>()
            println("======== Duster App =========")
            println(dusterAppDto.toString())
        }

    }

}
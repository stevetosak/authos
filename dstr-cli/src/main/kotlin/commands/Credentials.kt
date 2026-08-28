package com.tosak.authos.duster.commands

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.tosak.authos.duster.DusterConfig
import com.tosak.authos.duster.client
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

class Credentials : SuspendingCliktCommand(name = "credentials") {
    override val invokeWithoutSubcommand = true;
    override suspend fun run() {
        val resp = client.get("${DusterConfig.dusterBaseUrl}/duster/api/v1/internal/credentials") {
            header(HttpHeaders.Authorization, "Bearer ${DusterConfig.adminToken}")
        }
        val credentials = resp.body<Map<String,String>>()
        println("===== Credentials ======")
        println("clientId: ${credentials["client_id"]}")
        println("clientSecret: ${credentials["client_secret"]}")

    }

}
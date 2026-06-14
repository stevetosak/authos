package com.tosak.authos.duster.commands

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.tosak.authos.duster.DusterAppDto
import com.tosak.authos.duster.DusterConfig
import com.tosak.authos.duster.client
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

class Apps : SuspendingCliktCommand(name = "apps") {
    val clientId by option(
        "-cid", "--clientid",
        help = "Client ID of the application."
    )
    val name by option(
        "-n", "--name",
        help = "Name of the application"
    )

    override suspend fun run() {
        if (!clientId.isNullOrBlank() && !name.isNullOrBlank()) {
            throw IllegalArgumentException("Provide either -cid or -n, not both.")
        }
        val resp = client.get("${DusterConfig.dusterBaseUrl}/duster/api/v1/internal/apps") {
            parameter("client_id", clientId)
            parameter("client_name", name)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
        if (clientId.isNullOrBlank() && name.isNullOrBlank()) {
            println("======== All Apps =========")
            val apps = resp.body<List<DusterAppDto>>()
            apps.forEachIndexed { i, app ->
                println("#${i + 1}")
                println(app.toString())
                println("=====================")
            }
        } else {
            val dusterAppDto = resp.body<DusterAppDto>()
            println("======== Duster App =========")
            println(dusterAppDto.toString())
        }
    }
}
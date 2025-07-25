package com.tosak.authos.duster

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
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

class DusterCli : SuspendingCliktCommand(
    name = "dstr",
) {
    override fun help(context: Context) = "DSTR - CLI tool for your Duster Client"
    override  fun helpEpilog(context: Context) = """
        - Add client credentials generated by Authos by running:${"\u0085"}
        dstr credentials save -cid <your_client_id> -cs <your_client_secret>
    """.trimIndent()

    override suspend fun run() = Unit
}

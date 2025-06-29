package com.tosak.authos.duster.commands

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.tosak.authos.duster.client
import io.ktor.client.request.post

class SaveCredentials : SuspendingCliktCommand(name = "save") {
    val clientId by option("-cid","--clientid", help = "Client Identifier for your Duster Client").required()
    val clientSecret by option("-cs","--clientsecret", help = "Client Secret for your Duster Client").required()
    override suspend fun run() {
        println("Saving credentials...")
        client.post ("http://localhost:8785/duster/api/internal/v1/credentials/save?client_id=$clientId&client_secret=$clientSecret")
        println("Credentials successfully saved.")
    }

}
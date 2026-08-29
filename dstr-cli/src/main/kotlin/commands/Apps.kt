package com.tosak.authos.duster.commands

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.BorderType
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.widgets.Panel
import com.github.ajalt.mordant.widgets.Text
import com.github.ajalt.mordant.widgets.definitionList
import com.tosak.authos.duster.DusterAppDto
import com.tosak.authos.duster.DusterConfig
import com.tosak.authos.duster.client
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

private fun maskSecret(secret: String): String {
    if (secret.length <= 4) return "•".repeat(secret.length)
    return "•".repeat(secret.length - 4) + secret.takeLast(4)
}

class Apps : SuspendingCliktCommand(name = "apps") {
    override val invokeWithoutSubcommand = true
    val clientId by option("-cid", "--clientid", help = "Client ID of the application.")
    val name by option("-n", "--name", help = "Name of the application")
    val showSecret by option("-s", "--show-secret", help = "Show client secret in full (default: masked)").flag()

    override suspend fun run() {
        if (!clientId.isNullOrBlank() && !name.isNullOrBlank()) {
            throw IllegalArgumentException("Provide either -cid or -n, not both.")
        }
        val resp = client.get("${DusterConfig.dusterBaseUrl}/duster/api/v1/internal/apps") {
            parameter("client_id", clientId)
            parameter("client_name", name)
            header(HttpHeaders.Authorization, "Bearer ${DusterConfig.adminToken}")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
        val t = terminal

        if (clientId.isNullOrBlank() && name.isNullOrBlank()) {
            val apps = resp.body<List<DusterAppDto>>()
            if (apps.isEmpty()) { echo("No apps registered."); return }
            t.println(table {
                borderType = BorderType.SQUARE_DOUBLE_SECTION_SEPARATOR
                header {
                    style(bold = true)
                    row("#", "Name", "Client ID", "Scope", "Grant")
                }
                body {
                    apps.forEachIndexed { i, app ->
                        row(i + 1, app.name, app.clientId, app.scope, app.grantType)
                    }
                }
            })
        } else {
            val app = resp.body<DusterAppDto>()
            val secret = if (showSecret) app.clientSecret else maskSecret(app.clientSecret)
            val webhookSecret = if (showSecret) app.webhookSecret else maskSecret(app.webhookSecret)
            t.println(
                Panel(
                    content = definitionList {
                        inline = true
                        descriptionSpacing = 2
                        entry("ID",         app.clientId)
                        entry("Secret",     secret)
                        entry("Scope",      app.scope)
                        entry("Grant",      app.grantType)
                        entry("Redirect",   app.redirectUri)
                        entry("Callback",   app.callbackUri)
                        entry("Success URL",app.successUrl)
                        entry("Logout URL", app.logoutRedirectUrl)
                        entry("Session TTL",app.sessionTtl.toString())
                        entry("Webhook Secret", webhookSecret)
                        entry("Allowed Origins", app.allowedOrigins.joinToString(", ").ifEmpty { "—" })
                    },
                    title = Text(TextStyles.bold(app.name)),
                    borderType = BorderType.ROUNDED
                )
            )
        }
    }
}
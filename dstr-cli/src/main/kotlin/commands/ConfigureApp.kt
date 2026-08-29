package com.tosak.authos.duster.commands

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.long
import com.tosak.authos.duster.DusterConfig
import com.tosak.authos.duster.client
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

data class AppConfigUpdateRequest(
    @JsonProperty("success_url") val successUrl: String? = null,
    @JsonProperty("logout_redirect_url") val logoutRedirectUrl: String? = null,
    @JsonProperty("webhook_secret") val webhookSecret: String? = null,
    @JsonProperty("session_ttl") val sessionTtl: Long? = null,
    @JsonProperty("allowed_origins") val allowedOrigins: List<String>? = null,
    @JsonProperty("error_url") val errorUrl: String? = null,
)

class ConfigureApp : SuspendingCliktCommand(name = "configure") {
    val clientId by option("-cid", "--clientid", help = "Client ID of the app to configure").required()
    val successUrl by option("--success-url", help = "Where to redirect the browser after a successful login")
    val logoutRedirectUrl by option("--logout-url", help = "Where to redirect the browser after logout")
    val errorUrl by option(
        "--error-url",
        help = "Where to redirect the browser when the OAuth callback fails (default: success-url origin + /error). Pass an empty string to reset to the default.",
    )
    val webhookSecret by option(
        "--webhook-secret",
        help = "Shared secret used to HMAC-SHA256 sign the callback webhook payload (X-Duster-Signature)"
    )
    val sessionTtl by option("--session-ttl", help = "Session lifetime in seconds").long()
    val allowedOrigins by option(
        "--allowed-origins",
        help = "Comma-separated web origins allowed to make cross-origin credentialed calls " +
            "(tier 1, e.g. https://app.example.com). Pass an empty string to clear.",
    ).split(",")

    override suspend fun run() {
        if (successUrl == null && logoutRedirectUrl == null && webhookSecret == null &&
            sessionTtl == null && allowedOrigins == null && errorUrl == null
        ) {
            throw IllegalArgumentException(
                "Provide at least one of --success-url, --logout-url, --error-url, --webhook-secret, --session-ttl, --allowed-origins"
            )
        }
        val resp = client.patch("${DusterConfig.dusterBaseUrl}/duster/api/v1/internal/apps/config") {
            parameter("client_id", clientId)
            header(HttpHeaders.Authorization, "Bearer ${DusterConfig.adminToken}")
            contentType(ContentType.Application.Json)
            setBody(
                AppConfigUpdateRequest(
                    successUrl, logoutRedirectUrl, webhookSecret, sessionTtl,
                    allowedOrigins?.map { it.trim() }?.filter { it.isNotEmpty() },
                    errorUrl,
                )
            )
        }
        println("App config updated (${resp.status}).")
    }
}

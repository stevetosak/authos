package com.authos.rest

import com.authos.duster_client.DusterClient
import com.authos.duster_client.StateStore
import com.tosak.authos.crypto.getSecureRandomValue
import io.ktor.http.Url
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import java.net.URI
import java.net.URLEncoder

//import com.tosak.authos.crypto.*

fun Application.externalRoutes() {
    val AUTHOS_AUTHORIZE_URL = "http://localhost:9000/oauth/authorize"
    routing {
        route("/duster/api/v1/oauth") {
            get("/start") {
                val stateStore: StateStore by inject()
                val client : DusterClient by inject()
                val state = stateStore.generateState()
                // todo prompt handling

                val url = "$AUTHOS_AUTHORIZE_URL?client_id=${client.config.clientId}" +
                        "&redirect_uri=${Url(client.config.redirectUri)}&state=$state" +
                        "&scope=${URLEncoder.encode(client.config.scope, "UTF-8")}" +
                        "&response_type=code"

                call.respond(mapOf("url" to url))

            }
            get("/callback"){

            }
            post("/app/register"){

            }
            get("/healthcheck"){

            }
        }
        get("/test") {
            call.respondText("{\"test\": \"test\"}")
        }
    }
}
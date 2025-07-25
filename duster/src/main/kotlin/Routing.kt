package com.authos

import com.authos.routes.appRoutes
import com.authos.routes.credentialsRoutes
import com.authos.routes.oAuthRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
        }
    }

    routing {
        oAuthRoutes()
        appRoutes()
        credentialsRoutes()
    }
}

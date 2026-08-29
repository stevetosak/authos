package com.authos

import com.authos.repository.DusterAppRepository
import com.authos.routes.appRoutes
import com.authos.routes.corsPreflightRoutes
import com.authos.routes.credentialsRoutes
import com.authos.routes.healthRoutes
import com.authos.routes.oAuthRoutes
import com.authos.routes.sessionRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
        }
    }

    val dusterAppRepository by inject<DusterAppRepository>()

    routing {
        corsPreflightRoutes(dusterAppRepository)
        healthRoutes()
        oAuthRoutes()
        sessionRoutes()
        appRoutes()
        credentialsRoutes()
    }
}

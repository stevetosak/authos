package com.authos.rest

import io.ktor.server.application.Application
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.externalRoutes(){
    routing {
        route("/duster/api/v1/start"){

        }
        route("/duster/api/v1/callback"){

        }
    }
}
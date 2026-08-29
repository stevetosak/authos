package com.authos

import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty


fun main(args: Array<String>) {
    // PORT lets a `docker run` / PaaS / multi-tenant host place Duster on any port; 8785 is the
    // documented default and what every bundled manifest and the SDK proxy examples assume.
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8785
    embeddedServer(
        Netty,
        port = port,
        host = "0.0.0.0",
        module = Application::mainModule
    ).start(wait = true)
}

fun Application.mainModule() {
    configureHTTP()
    configureSerialization()
    configureDi()
    configureRouting()
}

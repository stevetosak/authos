package com.authos

import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty


fun main(args: Array<String>) {
    embeddedServer(
        Netty,
        port = 8785,
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

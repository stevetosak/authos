package com.authos

import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main(args: Array<String>) {
    internalServer()
    externalServer()
}

fun internalServer() {
    embeddedServer(
        Netty,
        port = 7675,
        host = "127.0.0.1",
        module = Application::internalModule
    ).start(wait = false)
}

fun externalServer() {
    embeddedServer(
        Netty,
        8785,
        host = "0.0.0.0",
        module = Application::externalModule)
    .start(wait = true)
}

fun Application.externalModule() {
    configureHTTP()
    configureFrameworks()
    configureSerialization()
    configureDatabases()
    configureRouting()
}
fun Application.internalModule() {

}


package com.authos

import com.authos.rest.externalRoutes
import com.authos.rest.internalRoutes
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.koin.core.context.startKoin
import org.koin.logger.slf4jLogger


fun main(args: Array<String>) {
//    internalServer()
    externalServer()
}

//fun internalServer() {
//    embeddedServer(
//        Netty,
//        port = 7675,
//        host = "127.0.0.1",
//        module = Application::internalModule
//    ).start(wait = false)
//}

fun externalServer() {
    embeddedServer(
        Netty,
        port = 8785,
        host = "0.0.0.0",
        module = Application::externalModule
    ).start(wait = true)
}

fun Application.commonModule() {
    configureHTTP()
    configureSerialization()
}

fun Application.externalModule() {
    commonModule()
    configureDiExternal()
    externalRoutes()
}

fun Application.internalModule() {
    commonModule()
    configureDiInternal()
    internalRoutes()
}
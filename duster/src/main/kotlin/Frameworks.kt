package com.authos

import io.ktor.server.application.*
import org.koin.core.context.startKoin
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureDiExternal() {
    install(Koin) {
        slf4jLogger()
        modules(dusterExternalModule())
    }
}
fun Application.configureDiInternal() {
    startKoin {
        slf4jLogger()
        modules(dusterInternal)
    }
}


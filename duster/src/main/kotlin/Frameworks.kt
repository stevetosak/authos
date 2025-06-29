package com.authos

import io.ktor.server.application.*
import org.koin.core.context.startKoin
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureDi() {
    install(Koin) {
        slf4jLogger()
        modules(dusterExternalModule())
    }
}


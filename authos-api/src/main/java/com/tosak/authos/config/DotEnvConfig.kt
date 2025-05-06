package com.tosak.authos.config

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.context.annotation.Configuration

@Configuration
open class DotEnvConfig {
    companion object {
        val dotenv: Dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load()
    }
}
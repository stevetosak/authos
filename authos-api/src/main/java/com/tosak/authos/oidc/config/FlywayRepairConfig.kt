package com.tosak.authos.oidc.config

import org.flywaydb.core.Flyway
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


//@Configuration
//open class FlywayRepairConfig {
//    @Bean
//    open fun repairFlyway(flyway: Flyway): CommandLineRunner? {
//        return CommandLineRunner { args ->
//            flyway.repair()
//            println("Flyway repair executed.")
//        }
//    }
//}
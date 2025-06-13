package com.authos

import com.authos.config.RedisConfig
import com.authos.service.RedisManager
import com.typesafe.config.ConfigFactory
import io.ktor.server.application.*
import io.ktor.server.config.HoconApplicationConfig

//
//val hikariConfig = HikariConfig().apply {
//    jdbcUrl = applicationEnvironment().config.propertyOrNull("postgres.url")?.getString()
//        ?: throw ConfigurationException("Missing jdbcUrl")
//    driverClassName = "org.postgresql.Driver"
//    username = applicationEnvironment().config.propertyOrNull("postgres.username")?.getString()
//        ?: throw ConfigurationException("Missing pg username")
//    password = applicationEnvironment().config.propertyOrNull("postgres.password")?.getString()
//    ?: throw ConfigurationException("Missing pg password")
//    maximumPoolSize = 10
//    connectionTestQuery = "SELECT 1"
//}



fun buildRedisManager() : RedisManager {
    val config = HoconApplicationConfig(ConfigFactory.load())
    val redisConfig = RedisConfig.fromApplicationConfig(config)
    val redisManager = RedisManager(redisConfig)
    redisManager.connect()
    return redisManager;

}

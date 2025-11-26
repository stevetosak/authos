package com.tosak.authos.oidc.config

//import org.springframework.data.redis.connection.RedisConnectionFactory
//import org.springframework.data.redis.connection.jedis.JedisConnection
//import org.springframework.data.redis.core.RedisTemplate
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.EnableTransactionManagement


@Configuration
@EnableTransactionManagement
open class BeanConfig(

) {
    @Bean
    open fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(12);
    }

    @Bean
    open fun objectMapper(): ObjectMapper  = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerModule(KotlinModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
}
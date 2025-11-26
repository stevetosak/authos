package com.tosak.authos.oidc.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.tosak.authos.oidc.common.pojo.AuthorizationSession
import com.tosak.authos.oidc.common.pojo.SSOSession
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableTransactionManagement
open class RedisConfig(private val connectionFactory: LettuceConnectionFactory, private val objectMapper: ObjectMapper) {

    private inline fun <reified T> createTemplate(): RedisTemplate<String, T> {
        val template = RedisTemplate<String, T>()
        template.connectionFactory = connectionFactory
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = Jackson2JsonRedisSerializer(objectMapper,T::class.java)
        template.setEnableTransactionSupport(true)
        template.afterPropertiesSet()
        return template
    }

    @Bean
    open fun ssoSessionRedisTemplate(): RedisTemplate<String, SSOSession> = createTemplate()

    @Bean
    open fun authorizationSessionRedisTemplate(): RedisTemplate<String, AuthorizationSession> = createTemplate()

}

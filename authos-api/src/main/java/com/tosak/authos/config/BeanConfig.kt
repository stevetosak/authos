package com.tosak.authos.config

//import org.springframework.data.redis.connection.RedisConnectionFactory
//import org.springframework.data.redis.connection.jedis.JedisConnection
//import org.springframework.data.redis.core.RedisTemplate
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
    open fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = connectionFactory
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = GenericJackson2JsonRedisSerializer()
        template.setEnableTransactionSupport(true)
        template.afterPropertiesSet()
        return template
    }

}
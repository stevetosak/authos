package com.tosak.authos.config

import com.nimbusds.jose.Algorithm
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
//import org.springframework.data.redis.connection.RedisConnectionFactory
//import org.springframework.data.redis.connection.jedis.JedisConnection
//import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*

@Configuration
open class BeanConfig {

    @Bean
    open fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(12);
    }

    @Bean
    open fun rsaKeyGen(): RSAKey {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair();
        return RSAKey
            .Builder(keyPair.public as RSAPublicKey)
            .privateKey(keyPair.private as RSAPrivateKey)
            .keyID("1")
            .algorithm(Algorithm.parse("RS256"))
            .issueTime(Date())
            .keyUse(KeyUse.SIGNATURE)
            .build();
    }

//    @Bean
//    open fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<*, *> {
//        val template = RedisTemplate<Any, Any>()
//        template.connectionFactory = connectionFactory
//        return template
//    }
}
package com.tosak.authos.config

//import org.springframework.data.redis.connection.RedisConnectionFactory
//import org.springframework.data.redis.connection.jedis.JedisConnection
//import org.springframework.data.redis.core.RedisTemplate
import com.nimbusds.jose.Algorithm
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.tosak.authos.exceptions.KeyLoadException
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import java.io.FileInputStream
import java.security.Key
import java.security.KeyPair
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*


@Configuration
open class BeanConfig (

){
    @Bean
    open fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(12);
    }

    @Bean
    open fun rsaKeyGen(): RSAKey {

//        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
//        keyPairGenerator.initialize(2048)
//        val keyPair = keyPairGenerator.generateKeyPair();
//        return RSAKey
//            .Builder(keyPair.public as RSAPublicKey)
//            .privateKey(keyPair.private as RSAPrivateKey)
//            .keyID(UUID.randomUUID().toString())
//            .algorithm(Algorithm.parse("RS256"))
//            .issueTime(Date())
//            .keyUse(KeyUse.SIGNATURE)
//            .build();

        val ks : KeyStore = KeyStore.getInstance("PKCS12")
        val fis = FileInputStream("/home/stevetosak/private/keystore.p12")
        val keystorePass = DotEnvConfig.dotenv["KEYSTORE_PASS"] ?: throw IllegalStateException("Keystore password not loaded")

        var key: Key? = null
        var cert: Certificate? = null
        ks.load(fis,keystorePass.toCharArray())
        val aliasEnumeration: Enumeration<String> = ks.aliases()
        while (aliasEnumeration.hasMoreElements()) {
            val keyName = aliasEnumeration.nextElement()
            key = ks.getKey(keyName,keystorePass.toCharArray())
            cert = ks.getCertificate(keyName)
        }

        if (cert == null || key == null) {
            throw KeyLoadException("Certificate or private key not loaded")
        }


        val keyPair = KeyPair(cert.publicKey,key as PrivateKey);
        val x509 = cert as X509Certificate

        return RSAKey
            .Builder(keyPair.public as RSAPublicKey)
            .privateKey(keyPair.private as RSAPrivateKey)
            .keyID("authos-jwt-sign")
            .algorithm(Algorithm.parse("RS256"))
            .issueTime(x509.notBefore)
            .keyUse(KeyUse.SIGNATURE)
            .build();
    }

    @Bean
    open fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = connectionFactory
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = GenericJackson2JsonRedisSerializer()
        return template
    }
}
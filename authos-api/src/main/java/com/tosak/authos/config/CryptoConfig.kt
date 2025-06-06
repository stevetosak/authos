package com.tosak.authos.config

import com.nimbusds.jose.Algorithm
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.tosak.authos.exceptions.KeyLoadException
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream
import java.security.*
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import javax.crypto.SecretKey

@Configuration
open class CryptoConfig{

   private lateinit var keystorePass: String
   private lateinit var keyStore: KeyStore

   @PostConstruct
   fun init(){
       keystorePass = DotEnvConfig.dotenv["KEYSTORE_PASS"] ?: throw IllegalStateException("Keystore password not loaded")
       val ks: KeyStore = KeyStore.getInstance("PKCS12")
       val fis = FileInputStream("/home/stevetosak/private/keystore.p12")
       ks.load(fis, keystorePass.toCharArray())
       keyStore = ks;
   }

    @Bean
    open fun rsaSignKey(): RSAKey {
        val key = keyStore.getKey("authos-jwt-sign",keystorePass.toCharArray())
        val cert = keyStore.getCertificate("authos-jwt-sign")
        if (cert == null || key == null) {
            throw KeyLoadException("Certificate or private key not loaded")
        }

        val keyPair = KeyPair(cert.publicKey, key as PrivateKey);
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
    open fun secretKey(): SecretKey {
        val key = keyStore.getKey("authos-credentials-encrypt",keystorePass.toCharArray()) as SecretKey
        return key
    }

}
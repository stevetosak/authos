package com.tosak.authos

import com.tosak.authos.config.BeanConfig
import com.tosak.authos.service.jwt.JwtUtils
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import java.security.interfaces.RSAKey



@SpringBootTest
@ContextConfiguration(classes = [BeanConfig::class])
class JwtTests (private val rsaKey: RSAKey) {
    @Autowired
    private lateinit var jwtUtils: JwtUtils;


    @Test
    fun contextLoads() {
    }

}
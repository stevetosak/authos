package com.tosak.authos

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.yaml.snakeyaml.Yaml
import java.io.IOException

@SpringBootTest
class AuthosApplicationTests {

    @Test
    fun testYmlLoad(){
        val yaml = Yaml();
        try{
            val inputStream = javaClass.getResourceAsStream("scopes_to_claims.yml")
            val data : Map<String,Any> = yaml.load(inputStream)
            println("data: $data")
        } catch (error: Exception) {
            System.err.println("error: $error")
        }

    }
}
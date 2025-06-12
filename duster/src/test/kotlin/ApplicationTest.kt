package com.authos

import com.authos.config.ConfigFile
import com.authos.config.loadYaml
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            externalModule()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }
    @Test
    fun testYamlLoad() {
        val yaml = File("src/main/resources/client-config.yaml");
        println(yaml.readText())
        val yamlConfig: ConfigFile = loadYaml<ConfigFile>(yaml)
        println(yamlConfig)
    }

}

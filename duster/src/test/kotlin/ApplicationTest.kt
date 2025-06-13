package com.authos

import com.authos.config.ConfigFile
import com.authos.config.loadYaml
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    val client = HttpClient(CIO)

    @Test
    fun testDusterStart() = runTest{
        val response = client.get("http://127.0.0.1:8785/duster/api/v1/oauth/start")
        assertEquals(HttpStatusCode.OK, response.status)
    }


}

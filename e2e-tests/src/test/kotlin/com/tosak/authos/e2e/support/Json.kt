package com.tosak.authos.e2e.support

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

val json: JsonMapper = JsonMapper.builder()
    .addModule(kotlinModule())
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .build()

inline fun <reified T> String.parseJson(): T = json.readValue(this)

@Suppress("UNCHECKED_CAST")
fun String.asMap(): Map<String, Any?> = json.readValue(this, Map::class.java) as Map<String, Any?>

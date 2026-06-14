package com.authos

fun getHostIp(): String {
    return System.getenv("HOST_IP") ?: "localhost"
}

fun getAuthosBaseUrl(): String {
    return System.getenv("AUTHOS_BASE_URL") ?: "http://${getHostIp()}:8080"
}
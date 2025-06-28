package com.authos

fun getHostIp(): String {
   return System.getenv("HOST_IP") ?:"localhost"
}
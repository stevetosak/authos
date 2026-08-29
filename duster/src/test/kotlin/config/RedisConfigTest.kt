package com.authos.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedisConfigTest {

    @Test
    fun `discrete fields build a redis uri with the timeout`() {
        val c = RedisConfig(host = "redis.internal", port = 6379)
        assertEquals("redis://redis.internal:6379?timeout=60s", c.redisUri())
    }

    @Test
    fun `useSSL switches the scheme to rediss`() {
        val c = RedisConfig(host = "redis.cloud", port = 6380, useSSL = true, timeoutSeconds = 10)
        assertEquals("rediss://redis.cloud:6380?timeout=10s", c.redisUri())
    }

    @Test
    fun `a password is url-encoded into the userinfo`() {
        val c = RedisConfig(host = "h", port = 1, password = "p@ss:w/rd ")
        assertEquals("redis://:p%40ss%3Aw%2Frd%20@h:1?timeout=60s", c.redisUri())
    }

    @Test
    fun `an explicit REDIS_URL wins verbatim`() {
        val c = RedisConfig(host = "", port = 0, url = "rediss://user:sec@up.stash.io:6379/2")
        assertEquals("rediss://user:sec@up.stash.io:6379/2", c.redisUri())
    }

    @Test
    fun `redactedUri hides the password`() {
        assertEquals(
            "redis://***@h:1?timeout=60s",
            RedisConfig(host = "h", port = 1, password = "hunter2").redactedUri(),
        )
        assertTrue("sec" !in RedisConfig(host = "", port = 0, url = "redis://u:sec@h:1").redactedUri())
    }
}

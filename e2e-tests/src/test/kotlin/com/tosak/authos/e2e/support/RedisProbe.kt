package com.tosak.authos.e2e.support

import java.io.BufferedInputStream
import java.net.Socket

/**
 * A tiny blocking RESP client, just enough to assert on what Duster wrote to Redis.
 *
 * `docker-compose.e2e.yml` maps the stack Redis to `localhost:16379`. Under `-De2e.attach`
 * there is no such mapping — pass `-De2e.redis=host:port` (tests that need it `assumeTrue`
 * on its presence).
 */
class RedisProbe(
    spec: String = System.getProperty("e2e.redis") ?: "localhost:16379",
) : AutoCloseable {

    private val socket = Socket(spec.substringBefore(':'), spec.substringAfter(':').toInt())
    private val out = socket.getOutputStream()
    private val inp = BufferedInputStream(socket.getInputStream())

    fun get(key: String): String? {
        send("GET", key)
        return readReply() as String?
    }

    @Suppress("UNCHECKED_CAST")
    fun keys(pattern: String): List<String> {
        send("KEYS", pattern)
        return (readReply() as List<Any?>).map { it as String }
    }

    private fun send(vararg args: String) {
        val sb = StringBuilder().append('*').append(args.size).append("\r\n")
        for (a in args) sb.append('$').append(a.toByteArray().size).append("\r\n").append(a).append("\r\n")
        out.write(sb.toString().toByteArray())
        out.flush()
    }

    private fun readLine(): String {
        val sb = StringBuilder()
        while (true) {
            val c = inp.read()
            if (c == -1) throw RuntimeException("redis: unexpected EOF")
            if (c == '\r'.code) { inp.read(); return sb.toString() } // swallow the \n
            sb.append(c.toChar())
        }
    }

    private fun readReply(): Any? {
        val line = readLine()
        return when (line[0]) {
            '+' -> line.substring(1)
            ':' -> line.substring(1).toLong()
            '-' -> throw RuntimeException("redis error: ${line.substring(1)}")
            '$' -> {
                val len = line.substring(1).toInt()
                if (len < 0) return null
                val buf = ByteArray(len)
                var read = 0
                while (read < len) {
                    val n = inp.read(buf, read, len - read)
                    if (n == -1) throw RuntimeException("redis: unexpected EOF in bulk string")
                    read += n
                }
                inp.read(); inp.read() // trailing \r\n
                String(buf)
            }
            '*' -> {
                val count = line.substring(1).toInt()
                if (count < 0) null else (0 until count).map { readReply() }
            }
            else -> throw RuntimeException("redis: unhandled reply '$line'")
        }
    }

    override fun close() {
        runCatching { socket.close() }
    }
}

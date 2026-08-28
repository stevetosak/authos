package com.tosak.authos.e2e.support

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

class Resp(
    val status: Int,
    val headers: Map<String, List<String>>,
    val body: String,
) {
    val location: String? get() = header("location")
    fun header(name: String): String? =
        headers.entries.firstOrNull { it.key.equals(name, ignoreCase = true) }?.value?.firstOrNull()
    fun setCookies(): List<String> =
        headers.entries.firstOrNull { it.key.equals("set-cookie", ignoreCase = true) }?.value ?: emptyList()
}

/**
 * Minimal HTTP client for walking the OAuth flow by hand:
 *  - never follows redirects (we inspect every Location)
 *  - its own trivial cookie jar (name=value, single host) - the JDK CookieManager drops
 *    Secure cookies over http, which every Authos cookie is.
 */
class Http {
    private val client: HttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NEVER)
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    val cookies: MutableMap<String, String> = linkedMapOf()

    fun get(url: String, headers: Map<String, String> = emptyMap()): Resp =
        send(reqBuilder(url, headers).GET())

    fun postForm(
        url: String,
        form: Map<String, String?> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
    ): Resp {
        val body = form.entries.joinToString("&") { (k, v) ->
            "${enc(k)}=${enc(v ?: "")}"
        }
        return send(
            reqBuilder(url, headers + ("Content-Type" to "application/x-www-form-urlencoded"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
        )
    }

    fun postJson(url: String, payload: Any, headers: Map<String, String> = emptyMap()): Resp =
        bodyJson("POST", url, payload, headers)

    fun patchJson(url: String, payload: Any, headers: Map<String, String> = emptyMap()): Resp =
        bodyJson("PATCH", url, payload, headers)

    private fun bodyJson(method: String, url: String, payload: Any, headers: Map<String, String>): Resp {
        val body = json.writeValueAsString(payload)
        return send(
            reqBuilder(url, headers + ("Content-Type" to "application/json"))
                .method(method, HttpRequest.BodyPublishers.ofString(body))
        )
    }

    private fun reqBuilder(url: String, headers: Map<String, String>): HttpRequest.Builder {
        val b = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20))
        headers.forEach { (k, v) -> b.header(k, v) }
        if (cookies.isNotEmpty()) {
            b.header("Cookie", cookies.entries.joinToString("; ") { "${it.key}=${it.value}" })
        }
        return b
    }

    private fun send(b: HttpRequest.Builder): Resp {
        val r = client.send(b.build(), HttpResponse.BodyHandlers.ofString())
        r.headers().map().let { hm ->
            hm.entries.firstOrNull { it.key.equals("set-cookie", true) }?.value?.forEach { raw ->
                val pair = raw.substringBefore(';').trim()
                val name = pair.substringBefore('=')
                val value = pair.substringAfter('=', "")
                if (name.isNotEmpty()) {
                    if (value.isEmpty() || value == "\"\"") cookies.remove(name) else cookies[name] = value
                }
            }
        }
        return Resp(r.statusCode(), r.headers().map(), r.body())
    }

    private fun enc(s: String) = URLEncoder.encode(s, StandardCharsets.UTF_8)
}

fun urlEncode(s: String): String = URLEncoder.encode(s, StandardCharsets.UTF_8)

fun queryParams(url: String): Map<String, String> {
    val q = url.substringAfter('?', "")
    if (q.isEmpty()) return emptyMap()
    return q.split('&').mapNotNull {
        val i = it.indexOf('=')
        if (i < 0) null
        else java.net.URLDecoder.decode(it.substring(0, i), StandardCharsets.UTF_8) to
            java.net.URLDecoder.decode(it.substring(i + 1), StandardCharsets.UTF_8)
    }.toMap()
}

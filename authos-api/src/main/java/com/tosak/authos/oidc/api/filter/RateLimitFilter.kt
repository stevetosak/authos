package com.tosak.authos.oidc.api.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.tosak.authos.oidc.common.dto.ErrorResponse
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.filter.OncePerRequestFilter
import java.net.InetAddress
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * Fixed-window rate limiting on the unauthenticated, credential-facing endpoints — brute-force and
 * signup-abuse protection (roadmap Cross-cutting item).
 *
 *  - `POST /register`, `POST /oauth-login`, `POST /native-login`  → keyed by **client IP**
 *  - `POST /oauth/token`  → keyed by **`client_id`** (form param), falling back to IP. Duster
 *    silent-refreshes every user session from a single pod IP, so a per-IP limit there would cap
 *    Duster's total throughput; per-client keeps one shared caller in its own bucket and an
 *    anonymous abuser in another.
 *
 * The client IP is the first `X-Forwarded-For` hop **only when the direct peer is a trusted proxy**
 * (`authos.ratelimit.trusted-proxies`, a CIDR list); otherwise the socket address. Without the
 * trusted-proxy gate a caller could spoof `X-Forwarded-For` to dodge the limit.
 *
 * Fails **open**: any Redis error is logged and the request proceeds — a limiter outage must not
 * take authentication down with it.
 *
 * Registered into the Spring Security chain ahead of [JwtFilter] by `WebSecurityConfig`; its
 * stand-alone servlet registration is disabled there so it runs exactly once.
 */
class RateLimitFilter(
    private val redis: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    private val enabled: Boolean,
    private val windowSeconds: Long,
    trustedProxies: List<String>,
    registerLimit: Int,
    loginLimit: Int,
    tokenLimit: Int,
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(RateLimitFilter::class.java)
    private val trusted: List<Cidr> = trustedProxies.mapNotNull { Cidr.parse(it.trim()) }

    private enum class KeyBy { IP, CLIENT_ID }
    private data class Rule(val name: String, val method: String, val path: String, val limit: Int, val keyBy: KeyBy)

    private val rules = listOf(
        Rule("register", "POST", "/register", registerLimit, KeyBy.IP),
        Rule("login", "POST", "/oauth-login", loginLimit, KeyBy.IP),
        Rule("login", "POST", "/native-login", loginLimit, KeyBy.IP),
        Rule("token", "POST", "/oauth/token", tokenLimit, KeyBy.CLIENT_ID),
    )

    override fun shouldNotFilter(request: HttpServletRequest): Boolean = !enabled

    private fun ruleFor(request: HttpServletRequest): Rule? =
        rules.firstOrNull { it.method.equals(request.method, ignoreCase = true) && request.requestURI == it.path }

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val rule = ruleFor(request)
        if (rule == null) {
            chain.doFilter(request, response)
            return
        }

        val id = when (rule.keyBy) {
            KeyBy.IP -> clientIp(request)
            KeyBy.CLIENT_ID -> request.getParameter("client_id")?.takeIf { it.isNotBlank() } ?: clientIp(request)
        }

        val now = Instant.now().epochSecond
        val key = "authos:rl:${rule.name}:$id:${now / windowSeconds}"

        val count: Long? = try {
            redis.opsForValue().increment(key)?.also { if (it == 1L) redis.expire(key, windowSeconds, TimeUnit.SECONDS) }
        } catch (ex: Exception) {
            log.warn("rate-limit check failed open for {} {}: {}", request.method, request.requestURI, ex.message)
            null
        }

        if (count != null && count > rule.limit) {
            val retryAfter = windowSeconds - (now % windowSeconds)
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.setHeader(HttpHeaders.RETRY_AFTER, retryAfter.toString())
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.writer.write(
                objectMapper.writeValueAsString(
                    ErrorResponse("too_many_requests", "rate limit exceeded, retry after $retryAfter seconds"),
                ),
            )
            return
        }

        chain.doFilter(request, response)
    }

    /** First `X-Forwarded-For` hop when the peer is a trusted proxy, else the socket address. */
    private fun clientIp(request: HttpServletRequest): String {
        val peer = request.remoteAddr ?: return "unknown"
        if (trusted.any { it.contains(peer) }) {
            request.getHeader("X-Forwarded-For")
                ?.substringBefore(',')
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.let { return it }
        }
        return peer
    }

    /** Minimal CIDR membership test — IPv4 and IPv6, no external dependency. */
    private class Cidr private constructor(private val network: ByteArray, private val prefixBits: Int) {

        fun contains(ip: String): Boolean = try {
            val addr = InetAddress.getByName(ip).address
            if (addr.size != network.size) {
                false
            } else {
                var bits = prefixBits
                var matches = true
                for (i in addr.indices) {
                    if (bits <= 0) break
                    val mask = if (bits >= 8) 0xFF else (0xFF shl (8 - bits)) and 0xFF
                    if ((addr[i].toInt() and mask) != (network[i].toInt() and mask)) {
                        matches = false
                        break
                    }
                    bits -= 8
                }
                matches
            }
        } catch (_: Exception) {
            false
        }

        companion object {
            fun parse(cidr: String): Cidr? = try {
                val slash = cidr.indexOf('/')
                if (slash < 0) {
                    val addr = InetAddress.getByName(cidr).address
                    Cidr(addr, addr.size * 8)
                } else {
                    Cidr(InetAddress.getByName(cidr.substring(0, slash)).address, cidr.substring(slash + 1).trim().toInt())
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}

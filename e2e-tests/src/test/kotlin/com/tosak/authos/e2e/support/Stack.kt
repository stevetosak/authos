package com.tosak.authos.e2e.support

import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * Endpoints the suite talks to. Service-to-service traffic inside compose uses the
 * `authos-api` / `duster` hostnames; the host (this JVM) reaches them on fixed mapped ports.
 */
data class Endpoints(val authosBase: String, val dusterBase: String) {
    /** Rewrite a compose-internal URL (as emitted in a redirect) to a host-reachable one. */
    fun rebase(url: String): String = url
        .replace("http://authos-api:8080", authosBase)
        .replace("http://duster:8785", dusterBase)
}

class E2eFixture(
    val endpoints: Endpoints,
    val adminToken: String,
    val user: SeededUser,
    val dusterApp: SeededApp,
    /** A second Duster-wired app in the *same* AppGroup as [dusterApp] (same pairwise sub). */
    val dusterApp2: SeededApp,
    val directApp: SeededApp,
    val dusterSvc: SeededCredentials,
) {
    val authosBase get() = endpoints.authosBase
    val dusterBase get() = endpoints.dusterBase
}

/**
 * Owns the docker-compose stack for the whole test run (started once, torn down after all tests),
 * seeds a working tenant, and exposes an [E2eFixture].
 *
 * System properties:
 *   -De2e.attach="authos=http://localhost:8080;duster=http://localhost:8785"  skip compose
 *   -De2e.skipTeardown=true   leave the stack running after the suite
 */
class StackExtension : BeforeAllCallback {

    override fun beforeAll(context: ExtensionContext) {
        context.root.getStore(ExtensionContext.Namespace.GLOBAL)
            .getOrComputeIfAbsent(KEY, { StackHolder() }, StackHolder::class.java)
    }

    companion object {
        private const val KEY = "authos-e2e-stack"

        val fixture: E2eFixture
            get() = holder?.fixture ?: error("StackExtension not initialised - annotate the test class with @ExtendWith(StackExtension::class)")

        private var holder: StackHolder? = null

        private const val ADMIN_TOKEN = "test-admin-token"
        private val COMPOSE_PROJECT = "authos-e2e"
        private val moduleDir = File(System.getProperty("user.dir"))
        private val composeFile = moduleDir.resolve("docker-compose.e2e.yml")

        internal class StackHolder : ExtensionContext.Store.CloseableResource {
            val fixture: E2eFixture
            private val ownsCompose: Boolean

            init {
                holder = this
                val attach = System.getProperty("e2e.attach")
                val endpoints: Endpoints
                if (attach != null) {
                    ownsCompose = false
                    endpoints = parseAttach(attach)
                    println("[e2e] attaching to $endpoints")
                } else {
                    ownsCompose = true
                    endpoints = Endpoints("http://localhost:18080", "http://localhost:18785")
                    preflight()
                    compose("down", "-v", "--remove-orphans")
                    compose("up", "-d")
                    try {
                        waitFor("${endpoints.authosBase}/.well-known/jwks.json", Duration.ofSeconds(150))
                        waitFor("${endpoints.dusterBase}/health", Duration.ofSeconds(60))
                    } catch (e: Throwable) {
                        dumpLogs()
                        compose("down", "-v", "--remove-orphans")
                        throw e
                    }
                }
                fixture = SeedClient(endpoints, ADMIN_TOKEN).seed()
            }

            override fun close() {
                if (ownsCompose && System.getProperty("e2e.skipTeardown") != "true") {
                    compose("down", "-v", "--remove-orphans")
                }
            }
        }

        private fun parseAttach(spec: String): Endpoints {
            val m = spec.split(";").associate {
                val (k, v) = it.split("=", limit = 2); k.trim() to v.trim()
            }
            return Endpoints(
                m["authos"] ?: error("e2e.attach missing authos=..."),
                m["duster"] ?: error("e2e.attach missing duster=..."),
            )
        }

        private fun preflight() {
            val artifacts = listOf(
                moduleDir.resolve("../authos-api/target/Authos-1.0.0-alpha.jar"),
                moduleDir.resolve("../duster/build/libs/fat.jar"),
                moduleDir.resolve("build/keystore/keystore.p12"),
            )
            val missing = artifacts.filterNot { it.exists() }
            check(missing.isEmpty()) {
                "Missing build artifacts: ${missing.joinToString { it.path }}\n" +
                    "Run:  ./gradlew :e2e-tests:e2eTest   (or :e2e-tests:buildAuthosApiJar :duster:buildFatJar :e2e-tests:generateTestKeystore)"
            }
        }

        private fun compose(vararg args: String) {
            val cmd = listOf("docker", "compose", "-p", COMPOSE_PROJECT, "-f", composeFile.absolutePath) + args
            val rc = ProcessBuilder(cmd)
                .directory(moduleDir)
                .redirectErrorStream(true)
                .inheritIO()
                .start()
                .waitFor()
            check(rc == 0) { "`${cmd.joinToString(" ")}` exited $rc" }
        }

        private fun dumpLogs() {
            runCatching {
                ProcessBuilder(
                    "docker", "compose", "-p", COMPOSE_PROJECT, "-f", composeFile.absolutePath,
                    "logs", "--no-color", "--tail", "200",
                ).directory(moduleDir).redirectErrorStream(true).inheritIO().start().waitFor()
            }
        }

        private fun waitFor(url: String, timeout: Duration) {
            val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build()
            val deadline = System.nanoTime() + timeout.toNanos()
            var last: String = "no attempt"
            while (System.nanoTime() < deadline) {
                try {
                    val r = client.send(
                        HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(3)).GET().build(),
                        HttpResponse.BodyHandlers.discarding(),
                    )
                    if (r.statusCode() == 200) {
                        println("[e2e] ready: $url")
                        return
                    }
                    last = "HTTP ${r.statusCode()}"
                } catch (e: Exception) {
                    last = e.javaClass.simpleName + (e.message?.let { ": $it" } ?: "")
                }
                Thread.sleep(1000)
            }
            error("timed out waiting for $url ($last)")
        }
    }
}

package com.tosak.authos.e2e.support

data class SeededUser(val email: String, val password: String)
data class SeededApp(
    val id: Int,
    val clientId: String,
    val clientSecret: String,
    val group: Int,
    val redirectUri: String,
)
data class SeededCredentials(val clientId: String, val clientSecret: String)

/**
 * Brings a fresh Authos DB up to a state where a Duster PKCE login can run:
 * a user, an OAuth app wired for Duster, a second "direct" app for the negative PKCE tests,
 * a Duster service account, and that app synced into Duster's Redis.
 */
class SeedClient(private val endpoints: Endpoints, private val adminToken: String) {

    private val authos = endpoints.authosBase
    private val duster = endpoints.dusterBase
    private fun admin() = mapOf("Authorization" to "Bearer $adminToken")

    fun seed(): E2eFixture {
        val user = SeededUser("e2e+${System.currentTimeMillis()}@example.com", "TestPass123!")
        registerUser(user)

        val http = Http()
        login(http, user)

        val dusterApp = registerDusterApp(http)
        val directApp = registerApp(
            http,
            name = "e2e-direct",
            redirectUris = listOf("http://localhost:9/cb"),
            scope = listOf("openid"),
        )
        val dusterSvc = createDusterServiceAccount(http)

        saveDusterCredentials(dusterSvc)
        DusterSync.run(endpoints, adminToken, dusterApp.clientId, dusterSvc)

        return E2eFixture(endpoints, adminToken, user, dusterApp, directApp, dusterSvc)
    }

    private fun registerUser(u: SeededUser) {
        val r = Http().postJson(
            "$authos/register",
            mapOf("email" to u.email, "password" to u.password, "name" to "E2E", "surname" to "User"),
        )
        require(r.status == 201) { "register -> ${r.status} ${r.body}" }
    }

    private fun login(http: Http, u: SeededUser) {
        val r = http.postForm("$authos/native-login", mapOf("email" to u.email, "password" to u.password))
        require(r.status == 200) { "native-login -> ${r.status} ${r.body}" }
        require(http.cookies.containsKey("AUTH_TOKEN")) { "native-login set no AUTH_TOKEN cookie" }
    }

    private fun registerDusterApp(http: Http): SeededApp {
        val redirect = "$duster/duster/api/v1/oauth/callback"
        val app = registerApp(
            http,
            name = "e2e-duster",
            redirectUris = listOf(redirect),
            scope = listOf("openid", "profile", "email", "offline_access"),
        )
        // /app/register can't set duster_callback_uri; /duster/pull requires it non-null.
        // The register response *is* the AppDTO (no GET-one endpoint) - pass it back to /app/update.
        val updated = appDtoCache.getValue(app.id).toMutableMap().apply {
            this["dusterCallbackUri"] = "$duster/e2e-webhook"
            this["redirectUris"] = listOf(redirect)
        }
        val r = http.postJson("$authos/app/update", updated)
        require(r.status == 201) { "app/update -> ${r.status} ${r.body}" }
        return app
    }

    private val appDtoCache = mutableMapOf<Int, Map<String, Any?>>()

    private fun registerApp(
        http: Http,
        name: String,
        redirectUris: List<String>,
        scope: List<String>,
    ): SeededApp {
        val body = mapOf(
            "appName" to name,
            "shortDescription" to "e2e test app",
            "tokenEndpointAuthMethod" to "client_secret_post",
            "grantTypes" to listOf("authorization_code"),
            "responseTypes" to listOf("code"),
            "redirectUris" to redirectUris,
            "scope" to scope,
            "group" to null,
        )
        val r = http.postJson("$authos/app/register", body)
        require(r.status == 201) { "app/register($name) -> ${r.status} ${r.body}" }
        val dto = r.body.asMap()
        val app = SeededApp(
            id = (dto["id"] as Number).toInt(),
            clientId = dto["clientId"] as String,
            clientSecret = dto["clientSecret"] as String,
            group = (dto["group"] as Number).toInt(),
            redirectUri = redirectUris.first(),
        )
        appDtoCache[app.id] = dto
        return app
    }

    private fun createDusterServiceAccount(http: Http): SeededCredentials {
        val r = http.postJson("$authos/duster/create", emptyMap<String, Any?>())
        require(r.status == 201) { "duster/create -> ${r.status} ${r.body}" }
        val dto = r.body.asMap()
        return SeededCredentials(dto["clientId"] as String, dto["clientSecret"] as String)
    }

    private fun saveDusterCredentials(svc: SeededCredentials) {
        val r = Http().postForm(
            "$duster/duster/api/v1/internal/credentials/save?client_id=${svc.clientId}&client_secret=${svc.clientSecret}",
            headers = admin(),
        )
        require(r.status == 200) { "internal/credentials/save -> ${r.status} ${r.body}" }
    }
}

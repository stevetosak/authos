package com.tosak.authos.e2e.support

/** The `dstr sync` sequence for one app, done directly over HTTP. */
object DusterSync {

    fun run(endpoints: Endpoints, adminToken: String, appClientId: String, svc: SeededCredentials) {
        val tokenResp = Http().postForm(
            "${endpoints.authosBase}/oauth/token",
            mapOf(
                "grant_type" to "client_credentials",
                "client_id" to svc.clientId,
                "client_secret" to svc.clientSecret,
            ),
        )
        require(tokenResp.status == 200) { "client_credentials token -> ${tokenResp.status} ${tokenResp.body}" }
        val accessToken = tokenResp.body.asMap()["access_token"] as String

        val pull = Http().postForm(
            "${endpoints.authosBase}/duster/pull?client_id=$appClientId",
            headers = mapOf("Authorization" to "Bearer $accessToken"),
        )
        require(pull.status == 200) { "duster/pull -> ${pull.status} ${pull.body}" }

        val create = Http().postJson(
            "${endpoints.dusterBase}/duster/api/v1/internal/apps/create",
            pull.body.asMap(),
            mapOf("Authorization" to "Bearer $adminToken"),
        )
        require(create.status == 200) { "internal/apps/create -> ${create.status} ${create.body}" }
    }
}

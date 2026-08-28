package com.tosak.authos.e2e.support

/**
 * Drives the Authos authorization-code flow over HTTP (no browser). The frontend login/consent
 * pages are just shells that read query params and call back into the API - we do that directly.
 */
class OAuthFlow(private val fx: E2eFixture) {

    private val authos = fx.authosBase

    /** GET duster /start -> the (host-reachable) /oauth/authorize URL it redirects to. */
    fun startViaDuster(http: Http, clientId: String): String {
        val r = http.get("${fx.dusterBase}/duster/api/v1/oauth/start?client_id=$clientId")
        check(r.status == 302) { "duster /start -> ${r.status} ${r.body}" }
        return fx.endpoints.rebase(r.location ?: error("duster /start: no Location"))
    }

    fun buildAuthorizeUrl(
        clientId: String,
        redirectUri: String,
        scope: String,
        state: String = "st-${System.nanoTime()}",
        codeChallenge: String? = null,
        codeChallengeMethod: String? = null,
        prompt: String? = null,
    ): String {
        val sb = StringBuilder("$authos/oauth/authorize?client_id=$clientId")
        sb.append("&redirect_uri=").append(urlEncode(redirectUri))
        sb.append("&scope=").append(urlEncode(scope))
        sb.append("&response_type=code")
        sb.append("&state=").append(state)
        codeChallenge?.let { sb.append("&code_challenge=").append(urlEncode(it)) }
        codeChallengeMethod?.let { sb.append("&code_challenge_method=").append(it) }
        prompt?.let { sb.append("&prompt=").append(it) }
        return sb.toString()
    }

    /**
     * A `prompt=none` authorize attempt — no login/consent page walk. Returns the terminal 302,
     * whose Location is either `<redirect_uri>?code=..` (an SSO session was live and usable) or
     * `<redirect_uri>?error=login_required` (no usable session). Authos bounces a successful
     * `prompt=none` through `/oauth/approve`; this follows that hop.
     */
    fun silentAuthorize(http: Http, authorizeUrl: String): Resp {
        val r = http.get(authorizeUrl)
        check(r.status == 302) { "authorize(prompt=none) -> ${r.status} ${r.body}" }
        val loc = r.location ?: error("authorize(prompt=none): no Location")
        if (!loc.contains("/oauth/approve")) return r
        val approve = http.get(fx.endpoints.rebase(loc))
        check(approve.status == 302) { "approve (via prompt=none) -> ${approve.status} ${approve.body}" }
        return approve
    }

    /** The 302 that /oauth/authorize returns. */
    fun authorize(http: Http, authorizeUrl: String): Resp {
        val r = http.get(authorizeUrl)
        check(r.status == 302) { "authorize -> ${r.status} ${r.body}" }
        return r
    }

    /**
     * From an /oauth/authorize 302 that points at the login page, run login + consent(approve).
     * Returns the /oauth/approve 302 whose Location is `<redirect_uri>?code=..&state=..`.
     */
    fun loginAndApprove(http: Http, authorizeRedirect: Resp): Resp {
        val lp = queryParams(authorizeRedirect.location!!) // $FRONTEND_HOST/oauth/login?...
        val login = http.postForm(
            "$authos/oauth-login",
            mapOf(
                "email" to fx.user.email,
                "password" to fx.user.password,
                "client_id" to lp["client_id"],
                "redirect_uri" to lp["redirect_uri"],
                "state" to lp["state"],
                "scope" to lp["scope"],
                "authz_id" to lp["authz_id"],
            ),
        )
        check(login.status == 200) { "oauth-login -> ${login.status} ${login.body}" }
        val consentUrl = login.body.asMap()["redirectUri"] as? String
            ?: error("oauth-login response had no redirectUri: ${login.body}")
        val cp = queryParams(consentUrl)
        val approve = http.get(
            "$authos/oauth/approve" +
                "?client_id=${cp["client_id"]}" +
                "&redirect_uri=${urlEncode(cp["redirect_uri"]!!)}" +
                "&state=${cp["state"]}" +
                "&scope=${urlEncode(cp["scope"]!!)}" +
                "&authz_id=${cp["authz_id"]}",
        )
        check(approve.status == 302) { "approve -> ${approve.status} ${approve.body}" }
        return approve
    }

    /** authorize -> login -> approve, returning the authorization code from the final Location. */
    fun getCode(http: Http, authorizeUrl: String): String {
        val approve = loginAndApprove(http, authorize(http, authorizeUrl))
        return queryParams(approve.location!!)["code"] ?: error("approve Location had no code: ${approve.location}")
    }

    fun tokenRequest(
        http: Http,
        clientId: String,
        clientSecret: String,
        code: String,
        redirectUri: String,
        codeVerifier: String? = null,
    ): Resp = http.postForm(
        "$authos/oauth/token",
        buildMap {
            put("grant_type", "authorization_code")
            put("code", code)
            put("redirect_uri", redirectUri)
            put("client_id", clientId)
            put("client_secret", clientSecret)
            if (codeVerifier != null) put("code_verifier", codeVerifier)
        },
    )
}

package com.authos.routes

import com.authos.data.CallbackResponse
import com.authos.model.DusterSession
import com.authos.model.UserInfo
import com.authos.repository.DusterAppRepository
import com.authos.repository.DusterSessionRepository
import com.authos.repository.TokenRepository
import com.authos.service.DusterOAuthClient
import com.authos.service.DusterRequestService
import com.authos.service.StateStore
import com.authos.service.verifyIdToken
import io.ktor.client.call.body
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject
import java.security.InvalidParameterException
import java.util.UUID

fun Route.oAuthRoutes() {
    val stateStore by inject<StateStore>()
    val dusterAppRepository by inject<DusterAppRepository>()
    val tokenRepository by inject<TokenRepository>()
    val sessionRepository by inject<DusterSessionRepository>()

    route("/duster/api/v1/oauth") {

        get("/start") {
            val clientId = call.queryParameters["client_id"]
                ?: throw InvalidParameterException("client_id is required")

            val app = dusterAppRepository.getDusterAppByClientId(clientId)

            val existingSession = call.request.cookies["duster_session"]
            if (existingSession != null) {
                val session = sessionRepository.get(existingSession, clientId)
                if (session != null) {
                    call.respondRedirect(app.successUrl.ifBlank { "/" })
                    return@get
                }
            }

            val client = DusterOAuthClient(app)
            val requestService = DusterRequestService(client, tokenRepository)
            val (state, codeChallenge) = stateStore.generateState(clientId)
            val url = requestService.generateAuthorizeUrl(app, state, codeChallenge)
            call.respondRedirect(url)
        }

        get("/callback") {
            val code = call.queryParameters["code"]
            val state = call.queryParameters["state"]

            if (code == null || state.isNullOrEmpty()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing required parameters"))
                return@get
            }

            try {
                val stateData = stateStore.validateState(state)
                val app = dusterAppRepository.getDusterAppByClientId(stateData.clientId)
                val client = DusterOAuthClient(app)

                val tokenResponse = client.codeExchange(code, stateData.codeVerifier)
                val (idTokenObj, idTokenString) = verifyIdToken(tokenResponse.idToken!!)
                val sub = idTokenObj.jwtClaimsSet.subject

                val userInfoResponse = client.fetchUserInfo(tokenResponse.accessToken)
                val userInfo: UserInfo = userInfoResponse.body()
                val prunedInfo = UserInfo.getPrunedObject(userInfo)

                tokenRepository.saveAll(
                    sub,
                    idTokenString,
                    tokenResponse.accessToken,
                    tokenResponse.refreshToken,
                    idTokenObj.jwtClaimsSet.expirationTime.toInstant().epochSecond,
                    tokenResponse.expiresIn.toLong()
                )

                if (app.callbackUri.isNotBlank()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val webhookResp = client.sendToCallback(prunedInfo)
                            if (webhookResp.status.isSuccess()) {
                                try {
                                    webhookResp.body<CallbackResponse>()
                                } catch (e: Exception) {
                                    println("Callback webhook returned a malformed body (ignored): ${e.message}")
                                }
                            } else {
                                println("Callback webhook returned non-success status: ${webhookResp.status}")
                            }
                        } catch (e: Exception) {
                            println("Webhook failed (non-blocking): ${e.message}")
                        }
                    }
                }

                val sessionId = UUID.randomUUID().toString()
                sessionRepository.save(
                    DusterSession(sessionId, stateData.clientId, sub, prunedInfo),
                    app.sessionTtl
                )

                val cookie = Cookie(
                    name = "duster_session",
                    value = sessionId,
                    httpOnly = true,
                    secure = true,
                    path = "/",
                    maxAge = app.sessionTtl.toInt(),
                    extensions = mapOf("SameSite" to "Strict")
                )
                call.response.cookies.append(cookie)
                call.respondRedirect(app.successUrl.ifBlank { "/" })

            } catch (e: Exception) {
                println("Callback error: ${e.localizedMessage}")
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Authentication failed"))
            }
        }
    }
}

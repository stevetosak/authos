package com.authos.routes

import com.authos.data.AuthTokenResponse
import com.authos.model.UserInfo
import com.authos.repository.DusterAppRepository
import com.authos.repository.TokenRepository
import com.authos.service.DusterOAuthClient
import com.authos.service.DusterRequestService
import com.authos.service.StateStore
import com.authos.service.verifyIdToken
import io.ktor.client.call.body
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.security.InvalidParameterException
import kotlin.getValue
import kotlin.text.isEmpty

fun Route.oAuthRoutes(){
    val stateStore by inject<StateStore>()
    val dusterAppRepository by inject<DusterAppRepository>()
    val tokenRepository by inject<TokenRepository>()

    route("/duster/api/v1/oauth") {
        get("/start") {
            val clientId =
                call.queryParameters["client_id"] ?: throw InvalidParameterException("Client ID is required")
            val mode = call.queryParameters["mode"] ?: "auto";


            val sub = call.queryParameters["sub"] ?: call.request.cookies["sub"]

            println("Starting Duster Flow..")

            val app = dusterAppRepository.getDusterAppByClientId(clientId)
            val client = DusterOAuthClient(app)
            val requestService = DusterRequestService(client, tokenRepository)
            val state = stateStore.generateState(clientId)

            if (sub != null && mode == "auto") {
                println("Sub is present")
                println("Mode is \"${mode}\"")
                val result = requestService.tryAccessTokenExchange(sub)
                when (result) {
                    is DusterRequestService.ResponseResult.Failure -> {
                        println("Got Failure. Generating authorize url...")
                        val url = requestService.generateAuthorizeUrl(app, sub, state) + "&duster_uid=${sub}"
                        call.respondRedirect { url }

                    }

                    is DusterRequestService.ResponseResult.Success -> {
                        println("Got Success. Returning data...")
                        val redirectUrl = client.sendToCallback(result.data).headers["Location"]
                        val subCookie = Cookie("sub", result.data["sub"]!!, secure = true, path = "/", httpOnly = true, maxAge = 3600)
                        call.response.cookies.append(subCookie)
                        call.respondRedirect(redirectUrl!!)

                    }
                }

            } else {
                println("Generating authorization url...")
                println("Mode is \"${mode}\"")
                val url = requestService.generateAuthorizeUrl(app = app, state = state)
                call.respondRedirect(url)
            }

        }

        get("/callback") {
            val code = call.queryParameters["code"]
            val state = call.parameters["state"]

            println("Received callback from authos.")
            println("Validating request parameters...")

            if (code == null || state == null || state.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing required parameters"))
                return@get
            }

            try {
                val clientId = stateStore.validateState(state)
                val app = dusterAppRepository.getDusterAppByClientId(clientId)
                val client = DusterOAuthClient(app)
                println("Initiating code exchange...")
                val tokenResponse: AuthTokenResponse = client.codeExchange(code)
                println("Code exchange complete. Verifying id token...")
                val (idTokenObj, idTokenString) = verifyIdToken(tokenResponse.idToken)
                println("Fetching user information...")
                val userInfoResponse = client.fetchUserInfo(tokenResponse.accessToken)
                println("Success! User info fetched.")
                println("Updating data...")
                tokenRepository.saveAll(
                    idTokenObj.jwtClaimsSet.subject,
                    idTokenString,
                    tokenResponse.accessToken,
                    tokenResponse.refreshToken,
                    idTokenObj.jwtClaimsSet.expirationTime.toInstant().epochSecond,
                    tokenResponse.expiresIn.toLong()
                )
                println("Sending userinfo to specified callback url...")
                val userInfo: UserInfo = userInfoResponse.body()
                val resp = client.sendToCallback(UserInfo.getPrunedObject(userInfo))
                val subCookie = Cookie("sub", userInfo.sub, secure = true, path = "/", httpOnly = true, maxAge = 3600)
                call.response.cookies.append(subCookie)
                val redirectUrl = resp.headers["Location"]
                call.respondRedirect("$redirectUrl")
            } catch (e: Exception) {
                println("Error: ${e.localizedMessage}")
                e.printStackTrace()
            }

        }
    }
}
package com.authos.rest

import com.authos.data.AuthTokenResponse
import com.authos.data.DusterAppRegisterDto
import com.authos.duster_client.DusterClient
import com.authos.duster_client.StateStore
import com.authos.model.DusterApp
import com.authos.model.UserInfo
import com.authos.repository.DusterAppRepository
import com.authos.repository.DusterAppRepositoryImpl
import com.authos.repository.TokenRepository
import com.authos.service.DusterRequestService
import com.authos.service.verifyIdToken
import io.ktor.client.call.body
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import org.koin.ktor.ext.inject
import java.security.InvalidParameterException

//import com.tosak.authos.crypto.*

@OptIn(ExperimentalLettuceCoroutinesApi::class)
fun Application.externalRoutes() {
    routing {
        //todo state store vo redis
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
                // todo mode handling

                val app = dusterAppRepository.getDusterAppByClientId(clientId)
                val client = DusterClient(app)
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
                            val subCookie = Cookie(
                                "sub",
                                result.data["sub"]!!,
                                secure = true,
                                path = "/",
                                httpOnly = true,
                                maxAge = 3600
                            )
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
                    val client = DusterClient(app)
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
                    val subCookie =
                        Cookie("sub", userInfo.sub, secure = true, path = "/", httpOnly = true, maxAge = 3600)
                    call.response.cookies.append(subCookie)
                    val redirectUrl = resp.headers["Location"]
                    call.respondRedirect("$redirectUrl")
                } catch (e: Exception) {
                    println("Error: ${e.localizedMessage}")
                    e.printStackTrace()
                }

            }
        }
        route("/test") {
            post("/callback") {
                val userinfo = call.receive<UserInfo>()

                call.respondRedirect("https://imaps.mk/Maps/View/T1?floor=0")
            }
        }
        route("/redis/test") {
            val repo: DusterAppRepositoryImpl by inject()
            post("/save") {

            }
            get("/app") {
                val app = repo.getDusterAppByClientId(
                    call.request.queryParameters["clientId"] ?: throw InvalidParameterException("Client ID is required")
                )
                call.respond(app)
            }
        }

        route("/duster/api/internal/apps") {

            get {
                val clientId = call.request.queryParameters["client_id"]
                val name = call.request.queryParameters["client_name"]
                if (clientId.isNullOrEmpty() && name.isNullOrEmpty()) {
                    val apps = dusterAppRepository.getAllDusterApps()
                    call.respond(HttpStatusCode.OK, apps)
                    return@get
                }
                try {
                    if (!clientId.isNullOrEmpty()) {
                        val app = dusterAppRepository.getDusterAppByClientId(clientId)
                        call.respond(app)
                    } else {
                        val app = dusterAppRepository.getDusterAppByName(name = name!!)
                        call.respond(app)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.BadRequest, "Cant find duster app with specified client id.")
                }

            }
            post("/create") {
                try {
                    val body = call.receive<DusterAppRegisterDto>()
                    println("Received difference body: ${body.toString()}")
                    val dusterApp = DusterApp(
                        clientId = body.clientId,
                        clientSecret = body.clientSecret,
                        redirectUri = body.redirectUri,
                        isActive = true,
                        lastSyncAt = System.currentTimeMillis(),
                        callbackUri = body.callbackUri,
                        updatedAt = System.currentTimeMillis(),
                        scope = body.scope,
                        name = body.name,
                    )
                    dusterAppRepository.save(dusterApp)
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.toString()))
                }


            }
            post("/delete") {

            }

            route("/config") {
                get {
                    // ako imat parametar za daden app config
                }
                post {
                    // tuka so parametar daden app i daden config parametar, da sa update toj config parametar
                }
            }


        }
    }
}
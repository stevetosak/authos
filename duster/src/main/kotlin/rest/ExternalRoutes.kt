package com.authos.rest

import com.authos.data.AuthTokenResponse
import com.authos.data.DusterAppRegisterDto
import com.authos.duster_client.DusterClient
import com.authos.duster_client.StateStore
import com.authos.model.DusterApp
import com.authos.model.UserInfo
import com.authos.repository.DusterAppRepository
import com.authos.repository.DusterAppRepositoryImpl
import com.authos.repository.UserInfoRepository
import com.authos.service.verifyIdToken
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import org.koin.ktor.ext.inject
import java.net.URLEncoder
import java.security.InvalidParameterException

//import com.tosak.authos.crypto.*

@OptIn(ExperimentalLettuceCoroutinesApi::class)
fun Application.externalRoutes() {
    val AUTHOS_AUTHORIZE_URL = "http://localhost:9000/oauth/authorize"
    routing {
        //todo state store vo redis

        val userinfoRepository by inject<UserInfoRepository>()
        val stateStore by inject<StateStore>()
        val dusterAppRepository by inject<DusterAppRepository>()
        route("/duster/api/v1/oauth") {
            get("/start") {
                val clientId = call.queryParameters["client_id"] ?: throw InvalidParameterException("Client ID is required")
                val sub = call.queryParameters["sub"]


                // todo access token i refresh handling
                var params = "";

                if(sub != null){
                    val token = userinfoRepository.getTokenBySub(sub)
                    if(token != null) {
                        params = "&prompt=none&id_token_hint=${token}"
                    }
                }

                val app = dusterAppRepository.getDusterApp(clientId)
                val client = DusterClient(app)

                if(app.accessToken != null) {
                    val userInfoResponse = client.fetchUserInfo(app.accessToken!!)
                    if(userInfoResponse.status == HttpStatusCode.OK) {
                        val data = UserInfo.getPrunedObject(userInfoResponse.body())
                        call.respond(HttpStatusCode.OK,data)
                    }else {
                        if(app.refreshToken != null) {
                            val resp = client.refreshToken(app.refreshToken!!)
                            if(resp.status == HttpStatusCode.OK) {

                            }
                        }
                    }

                }


                val state = stateStore.generateState(clientId)
                // todo prompt handling
                val url = "$AUTHOS_AUTHORIZE_URL?client_id=${client.dusterApp.clientId}" +
                        "&redirect_uri=${Url(app.redirectUri)}&state=$state" +
                        "&scope=${URLEncoder.encode(client.dusterApp.scope, "UTF-8")}" +
                        "&response_type=code" + params

                call.respond(status = HttpStatusCode.OK,mapOf("url" to url))
            }

            get("/callback"){
                val code = call.queryParameters["code"]
                val state = call.parameters["state"]

                if(code == null || state == null || state.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest,mapOf("error" to "Missing required parameters"))
                    return@get
                }

                val clientId = stateStore.validateState(state)
                val app = dusterAppRepository.getDusterApp(clientId)
                val client = DusterClient(app)
                val tokenResponse: AuthTokenResponse = client.codeExchange(code)
                val (tokenObj,tokenString) = verifyIdToken(tokenResponse.idToken)
                val userinfo = client.fetchUserInfo(tokenResponse)
                app.accessToken = tokenResponse.accessToken
                app.refreshToken = tokenResponse.refreshToken

                userinfoRepository.save(userinfo.sub,tokenObj,tokenString)
                dusterAppRepository.update(app)
                client.sendToCallback(userinfo)
                call.respond(HttpStatusCode.OK,mapOf("success" to true))
            }
        }
        route("/test"){
            post("/callback"){
                val rsp = call.receive<String>()
                log.warn("Received user info: $rsp")
            }
        }
       route("/redis/test"){
           val repo: DusterAppRepositoryImpl by inject()
           post("/save"){
               val body = call.receive<DusterAppRegisterDto>()
              println("Received difference body: ${body.toString()}")
               val dusterApp = DusterApp(clientId = body.clientId, clientSecret = body.clientSecret, redirectUri = body.redirectUri,isActive = true,
                   lastSyncAt = System.currentTimeMillis(), callbackUri = body.callbackUri, updatedAt = System.currentTimeMillis(), scope = body.scope,)
               repo.save(dusterApp)
               call.respond(HttpStatusCode.OK)
           }
           get("/app"){
               val app = repo.getDusterApp(call.request.queryParameters["clientId"] ?: throw InvalidParameterException("Client ID is required"))
               call.respond(app)
           }
       }
    }
}
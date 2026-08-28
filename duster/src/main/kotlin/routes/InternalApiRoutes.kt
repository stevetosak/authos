package com.authos.routes

import com.authos.data.DusterAppConfigUpdateDto
import com.authos.data.DusterAppRegisterDto
import com.authos.model.DusterApp
import com.authos.repository.CredentialsRepository
import com.authos.repository.DusterAppRepository
import com.authos.security.requireAdminAuth
import com.authos.service.DusterCliService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import kotlin.getValue
import kotlin.text.isNullOrEmpty

fun Route.appRoutes(){
    val dusterAppRepository by inject<DusterAppRepository>()
    route("/duster/api/v1/internal/apps"){
        get {
            if (!call.requireAdminAuth()) return@get
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
            if (!call.requireAdminAuth()) return@post
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
        patch("/config") {
            if (!call.requireAdminAuth()) return@patch
            val clientId = call.request.queryParameters["client_id"]
                ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "client_id required"))
            val body = call.receive<DusterAppConfigUpdateDto>()
            val existing = try {
                dusterAppRepository.getDusterAppByClientId(clientId)
            } catch (e: IllegalStateException) {
                return@patch call.respond(HttpStatusCode.NotFound, mapOf("error" to "no app found for $clientId"))
            }
            val updated = existing.copy(
                successUrl = body.successUrl ?: existing.successUrl,
                logoutRedirectUrl = body.logoutRedirectUrl ?: existing.logoutRedirectUrl,
                webhookSecret = body.webhookSecret ?: existing.webhookSecret,
                sessionTtl = body.sessionTtl ?: existing.sessionTtl,
                updatedAt = System.currentTimeMillis(),
            )
            dusterAppRepository.save(updated)
            call.respond(HttpStatusCode.OK, updated)
        }

    }

}

fun Route.credentialsRoutes(){
    route("/duster/api/v1/internal/credentials") {
        val credentialsRepository by inject<CredentialsRepository>()
        val dusterCliService by inject<DusterCliService>()
        post("/save"){
            if (!call.requireAdminAuth()) return@post
            val clientId = call.request.queryParameters["client_id"]
            val clientSecret = call.request.queryParameters["client_secret"]
            if(clientId.isNullOrEmpty() || clientSecret.isNullOrEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Missing parameters")
                return@post
            }
            credentialsRepository.saveCredentials(clientId, clientSecret)
            call.respond(HttpStatusCode.OK)

        }
        get{
            if (!call.requireAdminAuth()) return@get
            val credentials = credentialsRepository.getCredentials()
            if(credentials == null){
                call.respond(HttpStatusCode.NotFound, "Missing credentials")
                return@get
            }
            call.respond(HttpStatusCode.OK,credentials)
        }
        get("/token"){
            if (!call.requireAdminAuth()) return@get
            val token = dusterCliService.getAccessToken()
            call.respond(HttpStatusCode.OK,token)
        }

    }


}
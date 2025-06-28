package com.authos.rest

import com.authos.data.DusterAppRegisterDto
import com.authos.model.DusterApp
import com.authos.repository.DusterAppRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import kotlin.getValue

fun Application.internalRoutes(){
    routing {
        val dusterAppRepository by inject<DusterAppRepository>()


//        route("/duster/api/internal/apps"){
//
//            get {
//                // ako imat parametar vrakjas daden app
//            }
//            post("/create"){
//                val body = call.receive<DusterAppRegisterDto>()
//                println("Received difference body: ${body.toString()}")
//                val dusterApp = DusterApp(
//                    clientId = body.clientId,
//                    clientSecret = body.clientSecret,
//                    redirectUri = body.redirectUri,
//                    isActive = true,
//                    lastSyncAt = System.currentTimeMillis(),
//                    callbackUri = body.callbackUri,
//                    updatedAt = System.currentTimeMillis(),
//                    scope = body.scope,
//                )
//                dusterAppRepository.save(dusterApp)
//                call.respond(HttpStatusCode.OK)
//            }
//            post("/delete"){
//
//            }
//
//            route("/config"){
//                get{
//                    // ako imat parametar za daden app config
//                }
//                post{
//                    // tuka so parametar daden app i daden config parametar, da sa update toj config parametar
//                }
//            }
//
//
//        }

    }
}
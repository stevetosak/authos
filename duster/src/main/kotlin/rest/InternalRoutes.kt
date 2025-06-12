package com.authos.rest

import io.ktor.server.application.Application
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.internalRoutes(){
    routing {
        route("/duster/api/v1/internal/oauth"){
            get("/userinfo"){

            }
        }

        route("/duster/apps"){

            get {
                // ako imat parametar vrakjas daden app
            }
            post("/create"){

            }
            post("/delete"){

            }

            route("/config"){
                get{
                    // ako imat parametar za daden app config
                }
                post{
                    // tuka so parametar daden app i daden config parametar, da sa update toj config parametar
                }
            }


        }

    }
}
package com.tosak.authos.service

import com.nimbusds.jwt.SignedJWT
import com.tosak.authos.entity.App
import com.tosak.authos.service.jwt.JwtUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.net.URI

@Service
class OAuthService (
    private val jwtUtils: JwtUtils,
    private val appService: AppService,
){


    //todo NE TREBIT SEKADE REDIRECT NA LOGIN, TREBIT ERROR RESPONSE DA IMAT VO NEKOJ OD OVIE
    // todo ovaj authorization code ttrebit nova tabela vo baza, trebit da imat za koj user e, expire time ..
    // todo sleden cekor e drug endpoint kaj so sa pret exchange code za access_token
    //todo pobaraj consent od userot za podatocite so ke sa koristat vo odnos na scopes (ne e impl)
    // todo IMPLEMENTACIJA REDIS ZA CUVANJE NA SESSII I STATE (MOZDA I ACESS CODES)



//    fun authorize(request: HttpServletRequest,
//                  response: HttpServletResponse,
//                  clientId: String,
//                  redirectUri: String,
//                  state: String,
//                  scope: String,
//                  prompt: String,
//                  idTokenHint: String): ResponseEntity<Void> {
//
//
//
//
//    }

}
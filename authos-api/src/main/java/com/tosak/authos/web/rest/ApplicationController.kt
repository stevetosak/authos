package com.tosak.authos.web.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.tosak.authos.dto.AppDTO
import com.tosak.authos.dto.RegisterAppDTO
import com.tosak.authos.entity.App
import com.tosak.authos.repository.AppRepository
import com.tosak.authos.service.AppService
import com.tosak.authos.service.PPIDService
import com.tosak.authos.service.UserService
import com.tosak.authos.service.jwt.JwtUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/app")
class ApplicationController (
    private val appService: AppService,
    private val jwtUtils: JwtUtils,
    private val userService: UserService,
    private val ppidService: PPIDService, private val appRepository: AppRepository,
)
{

    //todo input validation

    @PostMapping("/register")
    fun registerApp(@RequestBody appDto: RegisterAppDTO,@CookieValue(name = "AUTH_TOKEN") token: String): ResponseEntity<AppDTO> {
        val jwt = jwtUtils.verifyToken(token)
        val id = ppidService.getUserIdByHash(jwt.jwtClaimsSet.subject)
        val user = userService.getById(id)
        val response = appService.registerApp(appDto,user)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
    @PostMapping("/update")
    fun updateApp(@RequestBody appDto: AppDTO,
                  @CookieValue(name = "AUTH_TOKEN") authToken: String,
                  @CookieValue(name = "XSRF-TOKEN") token2: String) : ResponseEntity<AppDTO> {
        //todo validate
        val jwt = jwtUtils.verifyToken(authToken)
        val userId = ppidService.getUserIdByHash(jwt.jwtClaimsSet.subject)
        val user = userService.getById(userId)
        val app = appService.updateApp(user,appDto)


        return ResponseEntity.status(201).body(app.toDTO())

    }
    @PostMapping("/updatetest")
    fun updateApp(@RequestBody app: String){
        println("APP: ${app}")
        jacksonObjectMapper().readValue(app, AppDTO::class.java)
        println("SERAPP: $app")
    }
}
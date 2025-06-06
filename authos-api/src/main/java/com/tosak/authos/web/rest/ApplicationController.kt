package com.tosak.authos.web.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.tosak.authos.dto.AppDTO
import com.tosak.authos.dto.AppGroupDTO
import com.tosak.authos.dto.CreateAppGroupDTO
import com.tosak.authos.dto.RegisterAppDTO
import com.tosak.authos.repository.AppRepository
import com.tosak.authos.service.AppGroupService
import com.tosak.authos.service.AppService
import com.tosak.authos.service.PPIDService
import com.tosak.authos.service.UserService
import com.tosak.authos.service.JwtService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class ApplicationController(
    private val appService: AppService,
    private val userService: UserService,
    private val appGroupService: AppGroupService,
)
{

    //todo input validation

    @PostMapping("/app/register")
    fun registerApp(@RequestBody appDto: RegisterAppDTO,
                    authentication: Authentication? ): ResponseEntity<AppDTO> {
        val user = userService.getUserFromAuthentication(authentication)
        val response = appService.registerApp(appDto,user)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
    @PostMapping("/app/update")
    fun updateApp(@RequestBody appDto: AppDTO,
                  authentication: Authentication?) : ResponseEntity<AppDTO> {
        //todo validate
        val user = userService.getUserFromAuthentication(authentication)
        val app = appService.updateApp(user,appDto)

        return ResponseEntity.status(201).body(app.toDTO())

    }
    @PostMapping("/app/regenerate-secret")
    fun regenerateSecret(@RequestBody app: AppDTO) :ResponseEntity<AppDTO>{
        val appDto = appService.regenerateSecret(app)
        return ResponseEntity.status(201).body(appDto)
    }
    @PostMapping("/group/add")
    fun addGroup (@RequestBody groupDTO: CreateAppGroupDTO,authentication: Authentication?) : ResponseEntity<AppGroupDTO>{
        val user = userService.getUserFromAuthentication(authentication)
        val group = appGroupService.createAppGroup(groupDTO,user)
        return ResponseEntity.status(HttpStatus.CREATED).body(group)
    }
    @PostMapping("/updatetest")
    fun updateApp(@RequestBody app: String){
        println("APP: ${app}")
        jacksonObjectMapper().readValue(app, AppDTO::class.java)
        println("SERAPP: $app")
    }
}
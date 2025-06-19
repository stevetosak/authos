package com.tosak.authos.web.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.tosak.authos.dto.AppDTO
import com.tosak.authos.dto.AppGroupDTO
import com.tosak.authos.dto.CreateAppGroupDTO
import com.tosak.authos.dto.RegisterAppDTO
import com.tosak.authos.entity.AppGroup
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Controller responsible for managing application-related operations and group creation.
 * Provides endpoints for registering applications, updating them, regenerating secrets,
 * and adding application groups. Interacts with application services to process requests.
 */
@RestController
class ApplicationController(
    private val appService: AppService,
    private val userService: UserService,
    private val appGroupService: AppGroupService,
) {

    /**
     * Registers a new application with the provided details.
     *
     * @param appDto the details of the application to be registered, encapsulated as a RegisterAppDTO object
     * @param authentication the authentication object of the currently logged-in user, used to determine the application's owner
     * @return a ResponseEntity containing the details of the registered application as an AppDTO object
     */
    @PostMapping("/app/register")
    fun registerApp(
        @RequestBody appDto: RegisterAppDTO,
        authentication: Authentication?
    ): ResponseEntity<AppDTO> {
        val user = userService.getUserFromAuthentication(authentication)
        val response = appService.registerApp(appDto, user)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * Updates an existing application with the provided data.
     *
     * @param appDto Data transfer object containing the updated details of the application.
     * @param authentication Optional authentication object containing the authenticated user's details.
     * @return A ResponseEntity containing the updated AppDTO object and an HTTP status code indicating the result.
     */
    @PostMapping("/app/update")
    fun updateApp(
        @RequestBody appDto: AppDTO,
        authentication: Authentication?
    ): ResponseEntity<AppDTO> {
        //todo validate
        val user = userService.getUserFromAuthentication(authentication)
        val app = appService.updateApp(user, appDto)

        return ResponseEntity.status(201).body(appService.toDTO(app))

    }
    @PostMapping("/app/delete")
    fun deleteApp(@RequestParam("app_id") appId: Int,authentication: Authentication?): ResponseEntity<AppDTO> {
        val user = userService.getUserFromAuthentication(authentication)
        val app = appService.getAppByIdAndUser(appId = appId,user)
        appService.deleteApp(app)
        return ResponseEntity.status(200).build()
    }

    /**
     * Regenerates the client secret for the provided application.
     *
     * @param app the AppDTO object containing the application's details for which the secret should be regenerated
     * @return ResponseEntity containing the updated AppDTO with the regenerated secret information
     */
    @PostMapping("/app/regenerate-secret")
    fun regenerateSecret(@RequestBody app: AppDTO): ResponseEntity<AppDTO> {
        val appDto = appService.regenerateSecret(app)
        return ResponseEntity.status(201).body(appDto)
    }

    /**
     * Adds a new application group for the authenticated user.
     *
     * @param groupDTO The details of the application group to be created, including its name,
     *                 default status, and policy configurations.
     * @param authentication The authentication object representing the currently logged-in user.
     *                        It may be null if no user is authenticated.
     * @return A ResponseEntity containing the details of the newly created application group with
     *         a status of HTTP 201 (Created).
     */
    @PostMapping("/group/add")
    fun addGroup(
        @RequestBody groupDTO: CreateAppGroupDTO,
        authentication: Authentication?
    ): ResponseEntity<AppGroupDTO> {
        val user = userService.getUserFromAuthentication(authentication)
        val group = appGroupService.createAppGroup(groupDTO, user)
        return ResponseEntity.status(HttpStatus.CREATED).body(group)
    }

    @PostMapping("/group/update")
    fun updateGroup(@RequestBody appGroupDto: AppGroupDTO, authentication: Authentication?) :ResponseEntity<AppGroupDTO> {
        val user = userService.getUserFromAuthentication(authentication)
        val group = appGroupService.updateGroup(appGroupDto,user);
        return ResponseEntity.ok(group.toDTO())
    }

    @PostMapping("/group/delete")
    fun deleteGroup(@RequestParam("group_id") groupId: Int, authentication: Authentication?): ResponseEntity<Void> {
        val user = userService.getUserFromAuthentication(authentication)
        val group: AppGroup = appGroupService.findGroupByIdAndUser(groupId, user)
        appGroupService.deleteGroup(group)
        return ResponseEntity.ok().build()
    }
}
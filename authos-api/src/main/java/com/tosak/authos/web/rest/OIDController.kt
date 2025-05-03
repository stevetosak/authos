package com.tosak.authos.web.rest

import com.tosak.authos.dto.AppDTO
import com.tosak.authos.dto.AppRegisteredDTO
import com.tosak.authos.dto.RegisterAppDTO
import com.tosak.authos.service.AppService
import com.tosak.authos.service.PPIDService
import com.tosak.authos.service.UserService
import com.tosak.authos.service.jwt.JwtUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/connect")
class OIDController (
    private val appService: AppService,
    private val jwtUtils: JwtUtils,
    private val userService: UserService,
    private val ppidService: PPIDService
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
}
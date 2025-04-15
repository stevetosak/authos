package com.tosak.authos.web.rest

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/connect")
class OIDController {

    @PostMapping("/register")
    fun registerApp(){

    }
}
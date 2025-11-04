package com.tosak.authos.oidc.common.dto

import com.tosak.authos.oidc.common.enums.LoginResponseStatus
import java.time.LocalDateTime

data class LoginResponse(val status: LoginResponseStatus,val time: LocalDateTime = LocalDateTime.now())

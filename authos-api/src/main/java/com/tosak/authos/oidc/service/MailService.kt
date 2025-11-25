package com.tosak.authos.oidc.service

import com.tosak.authos.oidc.entity.User
import org.springframework.stereotype.Service

@Service
class MailService {

    fun sendRegistrationConfirmationEmail(user: User,token: String) {

    }
}
package com.tosak.authos.service

import com.tosak.authos.dto.CreateUserAccountDTO
import com.tosak.authos.entity.AppGroup
import com.tosak.authos.entity.User
import com.tosak.authos.exceptions.unauthorized.InvalidUserCredentials
import com.tosak.authos.pojo.LoginTokenStrategy
import com.tosak.authos.repository.AppGroupRepository
import com.tosak.authos.repository.UserRepository
import com.tosak.authos.common.utils.JwtTokenFactory
import com.tosak.authos.exceptions.base.AuthosException
import com.tosak.authos.exceptions.base.HttpUnauthorizedException
import com.tosak.authos.exceptions.demand
import jakarta.servlet.http.HttpServletRequest
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.http.HttpHeaders
import java.time.Duration
import java.util.Optional

@Service
open class UserService @Autowired constructor(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val appGroupRepository: AppGroupRepository,
    private val ppidService: PPIDService,
    private val tokenFactory: JwtTokenFactory,
    private val appGroupService: AppGroupService,
    private val tokenService: TokenService,
    private val mailService: MailService
) {

    @Value("\${authos.cookie.domain}")
    lateinit var cookieDomain: String

    @Value("\${authos.api.host}")
    lateinit var apiHost: String

    open fun verifyCredentials(email: String, password: String): User {
        val userOpt: Optional<User> = userRepository.findByEmail(email)

        //todo proverka dali e active user acc

        demand(userOpt.isPresent && passwordEncoder.matches(password, userOpt.get().password))
        { AuthosException("Bad credentials", InvalidUserCredentials()) }


        return userOpt.get();
    }


    //    @Cacheable(value = ["users"], key = "#id")
    open fun getById(id: Int): User {
        return userRepository.findUserById(id) ?: throw AuthosException("Bad credentials", InvalidUserCredentials())
    }

    open fun getUserFromAuthentication(authentication: Authentication?): User {

        demand(authentication != null && authentication.principal != null && authentication.principal is User)
        { AuthosException("Unauthorized", HttpUnauthorizedException()) }

        return authentication!!.principal as User
    }

    open fun generateLoginCredentials(
        user: User,
        request: HttpServletRequest,
        group: AppGroup? = null,
        clear: Boolean = false
    ): HttpHeaders {
        val maxAge = if (clear) Duration.ZERO else Duration.ofHours(1);
        val token =
            tokenFactory.createToken(LoginTokenStrategy(user, ppidService, request, group, appGroupService, apiHost))
        val jwtCookie = ResponseCookie
            .from("AUTH_TOKEN", token.serialize())
            .httpOnly(true)
            .secure(true)
            .path("/")
            .domain(cookieDomain)
            .sameSite("None")
            .maxAge(maxAge)
            .build()

        val xsrfCookie = ResponseCookie.from("XSRF-TOKEN", token.jwtClaimsSet.getStringClaim("xsrf_token"))
            .httpOnly(false)
            .secure(true)
            .path("/")
            .domain(cookieDomain)
            .sameSite("None")
            .maxAge(maxAge)
            .build()
        val headers = HttpHeaders()
        headers.add("Set-Cookie", jwtCookie.toString())
        headers.add("Set-Cookie", xsrfCookie.toString())
        return headers;
    }


    @Transactional
    open fun createAccount(
        dto: CreateUserAccountDTO
    ): User {

        if (userRepository.existsByEmail(dto.email.trim().lowercase())) {
            throw IllegalArgumentException("Email already in use")
        }

        val user = userRepository.save(
            User(
                null,
                dto.email,
                password = passwordEncoder.encode(dto.password),
                dto.number,
                givenName = dto.firstName,
                familyName = dto.lastName
            )
        )
        appGroupRepository.save(AppGroup(name = "Default Group", user = user, isDefault = true))
        val token = tokenService.generateRegistrationConfirmationToken(user)
        mailService.sendRegistrationConfirmationEmail(user, token)
        return user

    }

    open fun activateAccount(user: User) {
        user.isActive = true
        userRepository.save(user)
    }

}
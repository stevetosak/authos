package com.tosak.authos.service

import com.tosak.authos.dto.CreateUserAccountDTO
import com.tosak.authos.entity.AppGroup
import com.tosak.authos.entity.User
import com.tosak.authos.exceptions.AuthenticationNotPresentException
import com.tosak.authos.exceptions.unauthorized.UserNotFoundException
import com.tosak.authos.pojo.LoginTokenStrategy
import com.tosak.authos.repository.AppGroupRepository
import com.tosak.authos.repository.UserRepository
import com.tosak.authos.common.utils.JwtTokenFactory
import jakarta.servlet.http.HttpServletRequest
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseCookie
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.http.HttpHeaders
import java.security.InvalidParameterException
import java.time.Duration
import java.util.Optional

@Service
open class UserService @Autowired constructor(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val appGroupRepository: AppGroupRepository,
    private val ppidService: PPIDService,
    private val tokenFactory: JwtTokenFactory,
    private val appGroupService: AppGroupService
) {

    open fun verifyCredentials(email: String, password: String): User {
        val userOpt: Optional<User> = userRepository.findByEmail(email)
        if (!userOpt.isPresent) {
            throw UserNotFoundException("Mail does not exist")
        }
        if (!passwordEncoder.matches(
                password,
                userOpt.get().password
            )
        ) throw InvalidParameterException("Credentials do not match")

        return userOpt.get();
    }


//    @Cacheable(value = ["users"], key = "#id")
    open fun getById(id: Int): User {
        return userRepository.findUserById(id) ?: throw UserNotFoundException(
            "User with id $id not found"
        )
    }
    open fun getUserFromAuthentication(authentication: Authentication?): User {
        println("$authentication AUTHENTICATION")
        if(authentication == null || authentication.principal == null || authentication.principal !is User)
            throw AuthenticationNotPresentException("Authentication not present")

        return authentication.principal as User
    }

    open fun generateLoginCredentials(user:User,request: HttpServletRequest,group:AppGroup? = null): HttpHeaders {
        val token = tokenFactory.createToken(LoginTokenStrategy(user,ppidService,request,group,appGroupService))
        val jwtCookie = ResponseCookie
            .from("AUTH_TOKEN", token.serialize())
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("None")
            .maxAge(Duration.ofHours(1))
            .build()

        val xsrfCookie = ResponseCookie.from("XSRF-TOKEN", token.jwtClaimsSet.getStringClaim("xsrf_token"))
            .httpOnly(false)
            .secure(true)
            .path("/")
            .sameSite("None")
            .maxAge(Duration.ofHours(1))
            .build()
        val headers = HttpHeaders()
        headers.add("Set-Cookie",jwtCookie.toString())
        headers.add("Set-Cookie",xsrfCookie.toString())
        return headers;
    }



    @Transactional
    open fun register(
        dto: CreateUserAccountDTO
    ): User {

        if (userRepository.existsByEmail(dto.email.trim().lowercase())) {
            throw IllegalArgumentException("Email already in use")
        }

        val user = User(null,dto.email, password = passwordEncoder.encode(dto.password),dto.number, givenName = dto.firstName, familyName = dto.lastName)

        //todo procedura vo bazava za da ne mozit da imat dve default grupi za eden user
        val defaultGroup = AppGroup(name = "Default Group", user = user, isDefault = true)
        val savedUser = userRepository.save(user)
        appGroupRepository.save(defaultGroup)
        return savedUser



    }

}
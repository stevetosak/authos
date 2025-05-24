package com.tosak.authos.service

import com.sun.security.auth.UserPrincipal
import com.tosak.authos.entity.User
import com.tosak.authos.exceptions.unauthorized.UserNotFoundException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service


@Service
class CachingUserDetailsService (
    private val userService: UserService,
    private val appService: AppService,
    private val ppidService: PPIDService,
) : UserDetailsService {

    private var user: User? = null

    override fun loadUserByUsername(sub: String): UserDetails {
        val userId = ppidService.getUserIdByHash(sub)
        val u = userService.getById(userId)
        this.user = u;
        return u;
    }
    fun loadById(sub: String): UserDetails {
        return loadUserByUsername(sub)
    }
    fun getUser() : User{
        return user ?: throw UserNotFoundException("User obj not present")
    }
}
package com.tosak.authos.oidc.service

import com.tosak.authos.oidc.entity.User
import com.tosak.authos.oidc.exceptions.unauthorized.InvalidUserCredentials
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service


// todo ova vo da ojt vo user service nemat potreba posebno da e

@Service
class CachingUserDetailsService (
    private val userService: UserService,
    private val ppidService: PPIDService,
) : UserDetailsService {

    private var user: User? = null

    override fun loadUserByUsername(sub: String): UserDetails {
        val ppid = ppidService.getPPIDBySub(sub)
        val u = userService.getById(ppid.key.userId!!)
        this.user = u;
        return u;
    }
    fun loadById(sub: String): UserDetails {
        return loadUserByUsername(sub)
    }
    fun getUser() : User{
        return user ?: throw InvalidUserCredentials()
    }
}
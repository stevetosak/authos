package com.tosak.authos.service

import com.tosak.authos.entity.User
import com.tosak.authos.exceptions.unauthorized.InvalidUserCredentials
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
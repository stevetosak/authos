package com.tosak.authos.web.filter

import com.tosak.authos.exceptions.NoTokenPresentException
import com.tosak.authos.service.CachingUserDetailsService
import com.tosak.authos.service.JwtService
import jakarta.annotation.PostConstruct
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
open class JwtFilter(private val jwtService: JwtService, private val userDetailsService: CachingUserDetailsService) : OncePerRequestFilter() {
    private val excludedPaths = ArrayList<String>()

    @PostConstruct
    open fun initMap(){
        excludedPaths.add("/native-login")
        excludedPaths.add("/oauth-login")
        excludedPaths.add("/register")
        excludedPaths.add("/authorize")
        excludedPaths.add("/approve")
        excludedPaths.add("/token")
        excludedPaths.add("/.well-known/jwks.json")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if(excludedPaths.contains(request.requestURI)){
            println("JWT FILTER EXCLUDED: Request uri: " + request.requestURI)
            filterChain.doFilter(request, response)
            return
        }
        println("VLEZE FILTER")
        try{
            val token = getJwtFromRequest(request)
            val jwt = jwtService.verifyToken(token);
            val userDetails = userDetailsService.loadById(jwt.jwtClaimsSet.subject)
            val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authentication
            println("VO RED E")
        } catch (err : Exception){
            SecurityContextHolder.clearContext()
        }

        filterChain.doFilter(request, response)
    }

    private fun getJwtFromRequest(request: HttpServletRequest): String {
        request.cookies?.firstOrNull { it.name == "AUTH_TOKEN" }?.value?.let { return it }
        val bearerToken = request.getHeader("Authorization")
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7)
        }
        throw NoTokenPresentException("")
    }
}
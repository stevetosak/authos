package com.tosak.authos.oidc.api.filter

import com.nimbusds.jwt.SignedJWT
import com.tosak.authos.oidc.exceptions.base.AuthosException
import com.tosak.authos.oidc.exceptions.base.HttpForbiddenException
import com.tosak.authos.oidc.exceptions.base.HttpUnauthorizedException
import com.tosak.authos.oidc.common.utils.demand
import com.tosak.authos.oidc.service.CachingUserDetailsService
import com.tosak.authos.oidc.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.PatternMatchUtils
import org.springframework.web.filter.OncePerRequestFilter

@Component
open class JwtFilter(private val jwtService: JwtService, private val userDetailsService: CachingUserDetailsService) : OncePerRequestFilter() {
    private val excludedPaths = listOf(
        "/native-login",
        "/oauth-login",
        "/register",
        "/oauth/*",
        "/.well-known/*",
        "/test/*",
        "/duster/pull",
        "/duster/validate-token",
        "/verify-sub"
    )


    private fun isExcluded(path: String): Boolean{
        println("REQUEST URI: $path")
       return excludedPaths.any { PatternMatchUtils.simpleMatch(it,path) }
    }


    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if(isExcluded(request.requestURI)){
            println("JWT FILTER EXCLUDED: Request uri: " + request.requestURI)
            filterChain.doFilter(request, response)
            return
        }
        println("Filter Applied. Request URI: " + request.requestURI)
        try{
            val token = getJwtFromRequest(request)
            val jwt = jwtService.verifyToken(token);
            verifyXsrf(jwt, request)
            val userDetails = userDetailsService.loadById(jwt.jwtClaimsSet.subject)
            val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authentication
            println("VO RED E")
        } catch (ex: AuthosException) {
            SecurityContextHolder.clearContext()
            println(ex.message)
            println("Not Authenticated. Cleared Security Context.")
        }
        catch(ex : Exception){
            println(ex.message)
            ex.printStackTrace()
            throw ex
        }
        filterChain.doFilter(request, response)
    }

    private fun verifyXsrf(jwt: SignedJWT,request: HttpServletRequest){
        val xsrfCookie = jwt.jwtClaimsSet.getStringClaim("xsrf_token");
        val xsrfHeader = request.getHeader("X-XSRF-TOKEN");

        demand(xsrfCookie != null && xsrfHeader != null && xsrfHeader == xsrfCookie)
        { AuthosException("bad xsrf", HttpForbiddenException()) }

    }

    private fun getJwtFromRequest(request: HttpServletRequest): String {
        request.cookies?.firstOrNull { it.name == "AUTH_TOKEN" }?.value?.let { return it }
        val bearerToken = request.getHeader("Authorization")
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7) }
        throw AuthosException("bad token", HttpUnauthorizedException())
    }
}
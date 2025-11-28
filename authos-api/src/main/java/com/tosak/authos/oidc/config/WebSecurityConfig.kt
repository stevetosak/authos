package com.tosak.authos.oidc.config

import com.tosak.authos.oidc.api.filter.JwtFilter
import com.tosak.authos.oidc.service.LogoutSuccessHandler
import com.tosak.authos.oidc.service.SSOSessionService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@EnableWebSecurity
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
open class WebSecurityConfig (private val jwtFilter: JwtFilter, private val userDetailsService: UserDetailsService,private val ssoSessionService: SSOSessionService) {


    @Bean
    open fun logoutSuccessHandler() = LogoutSuccessHandler(ssoSessionService = ssoSessionService)


    @Bean
    open fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
       return http
           .csrf { csrf -> csrf.disable() }
           .cors { cors -> cors.configurationSource(corsConfigurationSource()) }
           .authorizeHttpRequests { req -> req.anyRequest().permitAll() }
           .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
           .logout{logout ->
               logout.logoutUrl("/logout")
                   .deleteCookies("AUTH_TOKEN")
                   .deleteCookies("XSRF_TOKEN")
                   .deleteCookies("AUTHOS_SESSION")
                   .clearAuthentication(true)
                   .logoutSuccessHandler(logoutSuccessHandler())
           }
           .build();

    }

    @Bean
    open fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("http://localhost:5173","https://authos.imaps.mk","https://www.authos.imaps.mk")
        configuration.allowedMethods = listOf("GET", "POST", "OPTIONS")
        configuration.addAllowedHeader("*")
        configuration.exposedHeaders = listOf("Location")
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)

        return source
    }

    @Bean
    @Throws(Exception::class)
    open fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }
    @Bean
    open fun authenticationProvider(passwordEncoder: PasswordEncoder): AuthenticationProvider {
        val provider = DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService)
        provider.setPasswordEncoder(passwordEncoder)
        return provider;
    }
}
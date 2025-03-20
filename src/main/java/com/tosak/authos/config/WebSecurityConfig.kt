package com.tosak.authos.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@EnableWebSecurity
@Configuration
open class WebSecurityConfig {
    @Bean
    open fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
       return http
           .csrf { csrf -> csrf.disable() }
           .cors { cors -> cors.configurationSource(corsConfigurationSource()) }
           .authorizeHttpRequests { req -> req.anyRequest().permitAll() }
           .sessionManagement{session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)}
           .build();

    }

    @Bean
    open fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.addAllowedOrigin("http://localhost:5173")
        configuration.addAllowedOrigin("https://imaps.mk")
        configuration.addAllowedMethod("*")
        configuration.addAllowedHeader("*")
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
}
package com.tosak.authos.oidc.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.tosak.authos.oidc.api.filter.JwtFilter
import com.tosak.authos.oidc.api.filter.RateLimitFilter
import com.tosak.authos.oidc.service.LogoutSuccessHandler
import com.tosak.authos.oidc.service.SSOSessionService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate
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
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@EnableWebSecurity
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
open class WebSecurityConfig(
    private val jwtFilter: JwtFilter,
    private val userDetailsService: UserDetailsService,
    private val ssoSessionService: SSOSessionService
) {


    @Bean
    open fun logoutSuccessHandler() = LogoutSuccessHandler(ssoSessionService = ssoSessionService)


    @Bean
    open fun securityFilterChain(http: HttpSecurity, rateLimitFilter: RateLimitFilter): SecurityFilterChain {
        return http
            .csrf { csrf -> csrf.disable() }
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) }
            .authorizeHttpRequests { req -> req.anyRequest().permitAll() }
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(rateLimitFilter, JwtFilter::class.java)
            .logout { logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessHandler(logoutSuccessHandler())
            }

            .build();

    }

    /**
     * Rate limiter for the credential-facing endpoints. Built here (not a `@Component`) so it lives
     * only in the security chain, ahead of [JwtFilter]; [rateLimitFilterRegistration] disables the
     * stand-alone servlet registration Spring Boot would otherwise add.
     */
    @Bean
    open fun rateLimitFilter(
        @Qualifier("stringAuthosRedisTemplate") redis: RedisTemplate<String, String>,
        objectMapper: ObjectMapper,
        @Value("\${authos.ratelimit.enabled:true}") enabled: Boolean,
        @Value("\${authos.ratelimit.window-seconds:60}") windowSeconds: Long,
        @Value("\${authos.ratelimit.trusted-proxies:127.0.0.1/32,::1/128,10.0.0.0/8,172.16.0.0/12,192.168.0.0/16}")
        trustedProxies: List<String>,
        @Value("\${authos.ratelimit.limit.register:10}") registerLimit: Int,
        @Value("\${authos.ratelimit.limit.login:20}") loginLimit: Int,
        @Value("\${authos.ratelimit.limit.token:120}") tokenLimit: Int,
    ): RateLimitFilter = RateLimitFilter(
        redis, objectMapper, enabled, windowSeconds, trustedProxies, registerLimit, loginLimit, tokenLimit,
    )

    @Bean
    open fun rateLimitFilterRegistration(filter: RateLimitFilter): FilterRegistrationBean<RateLimitFilter> =
        FilterRegistrationBean(filter).apply { isEnabled = false }

    @Bean
    open fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns = listOf("*");
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
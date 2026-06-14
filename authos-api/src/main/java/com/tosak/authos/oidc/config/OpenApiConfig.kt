package com.tosak.authos.oidc.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class OpenApiConfig {
    @Bean
    open fun openAPI(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Authos API")
                .description("OpenID Connect Identity Provider")
                .version("1.0.0-alpha")
        )
}

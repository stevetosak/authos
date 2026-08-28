package com.tosak.authos.oidc.api.rest

import com.tosak.authos.oidc.common.dto.ErrorResponse
import com.tosak.authos.oidc.exceptions.AuthorizationEndpointException
import com.tosak.authos.oidc.exceptions.AuthorizationErrorCode
import com.tosak.authos.oidc.exceptions.TokenEndpointException
import com.tosak.authos.oidc.exceptions.TokenErrorCode
import com.tosak.authos.oidc.exceptions.base.AuthosException
import com.tosak.authos.oidc.exceptions.base.HttpBadRequestException
import com.tosak.authos.oidc.exceptions.base.HttpForbiddenException
import com.tosak.authos.oidc.exceptions.base.HttpUnauthorizedException
import com.tosak.authos.oidc.exceptions.buildErrorRedirect
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponse as SpringErrorResponse
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.net.URI

//@Profile("prod")
@RestControllerAdvice
class ExceptionHandler {

    @Value("\${authos.frontend.host}")
    private lateinit var frontendHost: String


    @ExceptionHandler(AuthosException::class)
    fun handleInternalExceptions(ex: AuthosException): ResponseEntity<HashMap<String, String>> {
        val map = HashMap<String, String>()
        map.put("errorMessage", ex.message)
        map.put("description", ex.description)
        map.put("redirect", ex.redirect.toString())


        val status = when (ex.cause) {
            is HttpUnauthorizedException -> {
                HttpStatus.UNAUTHORIZED.value()
            }

            is HttpBadRequestException -> {
                HttpStatus.BAD_REQUEST.value()
            }

            is HttpForbiddenException -> {
                HttpStatus.FORBIDDEN.value()
            }

            else -> {
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            }

        }

        return ResponseEntity.status(status).body(map)
    }


    @ExceptionHandler(AuthorizationEndpointException::class)
    fun handleAuthorizeEndpointExceptions(ex: AuthorizationEndpointException): ResponseEntity<Void> {
        ex.printStackTrace()

        val redirectUri = ex.redirectUri ?: "$frontendHost/error"
        val url = buildErrorRedirect(redirectUri, ex.error as AuthorizationErrorCode, ex.errorDescription, ex.state)
        return ResponseEntity.status(302).location(URI(url)).build()
    }

    @ExceptionHandler(TokenEndpointException::class)
    fun handleTokenEndpointExceptions(ex: TokenEndpointException): ResponseEntity<ErrorResponse> {
        ex.printStackTrace()
        // RFC 6749 §5.2: invalid_client MAY be answered with 401 + WWW-Authenticate; everything else is 400.
        val unauthorized = ex.error == TokenErrorCode.INVALID_CLIENT
        val builder = ResponseEntity.status(if (unauthorized) HttpStatus.UNAUTHORIZED else HttpStatus.BAD_REQUEST)
        if (unauthorized) builder.header(HttpHeaders.WWW_AUTHENTICATE, "Basic")
        return builder.body(ErrorResponse(ex.error.code(), ex.errorDescription))
    }

    /**
     * Safety net so nothing leaves an OAuth endpoint as an unshaped 500 / whitelabel page.
     *  - `/oauth/token`      -> RFC 6749 §5.2 JSON
     *  - `/oauth/authorize` + `/oauth/approve` -> redirect to the frontend error page (server errors only;
     *    a bad request param has no trusted redirect target, so it stays JSON)
     *  - anything else       -> keep Spring's status, just give it a small consistent JSON body
     *
     * Spring's own routing exceptions (404/405/415/missing-param ...) implement
     * [org.springframework.web.ErrorResponse]; their status is preserved.
     */
    @ExceptionHandler(Exception::class)
    fun handleUnhandled(ex: Exception, request: HttpServletRequest): ResponseEntity<Any> {
        val status: HttpStatusCode = (ex as? SpringErrorResponse)?.statusCode ?: HttpStatus.INTERNAL_SERVER_ERROR
        if (status.is5xxServerError) ex.printStackTrace()

        val path = request.requestURI
        if (status.is5xxServerError &&
            (path.startsWith("/oauth/authorize") || path.startsWith("/oauth/approve"))
        ) {
            return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI("$frontendHost/error?error=server_error"))
                .build()
        }

        val body = if (status.is5xxServerError) {
            ErrorResponse("server_error", "the authorization server encountered an unexpected error")
        } else {
            ErrorResponse("invalid_request", ex.message ?: "")
        }
        return ResponseEntity.status(status).body(body)
    }
}

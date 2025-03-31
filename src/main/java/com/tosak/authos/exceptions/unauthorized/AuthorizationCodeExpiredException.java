package com.tosak.authos.exceptions.unauthorized;

public class AuthorizationCodeExpiredException extends HttpUnauthorizedException {
    public AuthorizationCodeExpiredException(String message) {
        super(message);
    }
}

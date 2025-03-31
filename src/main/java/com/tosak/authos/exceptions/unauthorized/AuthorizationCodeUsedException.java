package com.tosak.authos.exceptions.unauthorized;

public class AuthorizationCodeUsedException extends HttpUnauthorizedException {
    public AuthorizationCodeUsedException(String message) {
        super(message);
    }
}

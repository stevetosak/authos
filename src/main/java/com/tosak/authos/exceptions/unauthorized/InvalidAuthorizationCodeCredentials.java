package com.tosak.authos.exceptions.unauthorized;

public class InvalidAuthorizationCodeCredentials extends HttpUnauthorizedException {
    public InvalidAuthorizationCodeCredentials(String message) {
        super(message);
    }
}

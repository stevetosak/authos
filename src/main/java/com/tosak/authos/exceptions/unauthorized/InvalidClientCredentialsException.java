package com.tosak.authos.exceptions.unauthorized;

public class InvalidClientCredentialsException extends HttpUnauthorizedException {
    public InvalidClientCredentialsException(String message) {
        super(message);
    }
}

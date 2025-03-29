package com.tosak.authos.exceptions;

public class AuthorizationCodeExpiredException extends RuntimeException {
    public AuthorizationCodeExpiredException(String message) {
        super(message);
    }
}

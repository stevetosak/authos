package com.tosak.authos.exceptions;

public class AuthorizationCodeUsedException extends RuntimeException {
    public AuthorizationCodeUsedException(String message) {
        super(message);
    }
}

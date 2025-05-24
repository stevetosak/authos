package com.tosak.authos.exceptions;

public class AuthenticationNotPresentException extends RuntimeException {
    public AuthenticationNotPresentException(String message) {
        super(message);
    }
}

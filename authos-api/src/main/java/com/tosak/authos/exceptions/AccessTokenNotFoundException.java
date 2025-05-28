package com.tosak.authos.exceptions;

public class AccessTokenNotFoundException extends RuntimeException {
    public AccessTokenNotFoundException(String message) {
        super(message);
    }
}

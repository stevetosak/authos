package com.tosak.authos.exceptions;

public class AccessTokenRevokedException extends RuntimeException {
    public AccessTokenRevokedException(String message) {
        super(message);
    }
}

package com.tosak.authos.oidc.exceptions.internal;

public class NoTokenPresentException extends RuntimeException {
    public NoTokenPresentException(String message) {
        super(message);
    }
}

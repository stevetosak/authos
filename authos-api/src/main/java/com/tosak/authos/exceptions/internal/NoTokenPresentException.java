package com.tosak.authos.exceptions.internal;

public class NoTokenPresentException extends RuntimeException {
    public NoTokenPresentException(String message) {
        super(message);
    }
}

package com.tosak.authos.exceptions;

public class NoTokenPresentException extends RuntimeException {
    public NoTokenPresentException(String message) {
        super(message);
    }
}

package com.tosak.authos.exceptions;

public class InvalidClientCredentialsException extends RuntimeException {
    public InvalidClientCredentialsException(String message) {
        super(message);
    }
}
